/*
 *
 *  Copyright (C) 2021 Piotr Przybył
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package dev.softwaregarden.spring.batch.first_steps.weather;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@SpringBootApplication
@EnableBatchProcessing
@Configuration
public class WeatherReport {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public WeatherReport(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherReport.class, args);
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("weatherReportJob")
            .incrementer(new RunIdIncrementer())
            .start(importStations())
            .next(importSensorReadings())
            .build();
    }

    @Bean
    public Step importStations() {
        return stepBuilderFactory.get("importStationsStep")
            .<Station, Station>chunk(10)
            .reader(stationItemReader(null))
            .writer(stationItemWriter(null))
            .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<? extends Station> stationItemReader(@Value("#{jobParameters['stationsFile']}") Resource stationsFile) {
        var marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Station.class);
        return new StaxEventItemReaderBuilder<Station>()
            .name("stationItemReader")
            .resource(stationsFile)
            .addFragmentRootElements("station")
            .unmarshaller(marshaller)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<Station> stationItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Station>()
            .dataSource(dataSource)
            .beanMapped()
            .sql("INSERT INTO STATION(id, temp_unit, speed_unit) VALUES (:id, :tempUnit, :speedUnit)")
            .build();
    }


    @Bean
    public Step importSensorReadings() {
        return stepBuilderFactory.get("importSensorReadings")
            .<SensorReading, SensorReading>chunk(100)
            .reader(readingsReader(null))
            .processor(readingsNormalizer(null))
            .writer(sensorReadingItemWriter())
            .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<SensorReading> readingsReader(@Value("#{jobParameters['readingsFiles']}") Resource[] files) {
        var reader = new MultiResourceItemReader<SensorReading>();
        reader.setResources(files);
        reader.setDelegate(sensorReadingItemReader());
        return reader;
    }

    private FlatFileItemReader<SensorReading> sensorReadingItemReader() {
        return new FlatFileItemReaderBuilder<SensorReading>()
            .name("sensorReadingItemReader")
            .linesToSkip(1)
            .lineTokenizer(sensorReadingsLineTokenizer())
            .fieldSetMapper(sensorReadingsFieldSetMapper())
            .build();
    }

    private LineTokenizer sensorReadingsLineTokenizer() {
        var lineTokenizer = new PatternMatchingCompositeLineTokenizer();
        lineTokenizer.setTokenizers(Map.of("*WIND*", windLineTokenizer(), "*TEMP*", temperatureLineTokenizer()));
        return lineTokenizer;
    }

    @Bean
    public DelimitedLineTokenizer temperatureLineTokenizer() {
        var tokenizer = new DelimitedLineTokenizer(";");
        tokenizer.setNames("stationId", "taken", "type", "temperature");
        return tokenizer;
    }

    @Bean
    public DelimitedLineTokenizer windLineTokenizer() {
        var tokenizer = new DelimitedLineTokenizer(";");
        tokenizer.setNames("stationId", "taken", "type", "speed", "direction");
        return tokenizer;
    }

    @Bean
    public FieldSetMapper<SensorReading> sensorReadingsFieldSetMapper() {
        return fieldSet -> {
            var stationId = UUID.fromString(fieldSet.readString("stationId"));
            var taken = Instant.parse(fieldSet.readString("taken"));
            var measurementType = fieldSet.readString("type");
            switch (measurementType) {
                case "WIND":
                    return new WindReading(stationId, taken, fieldSet.readDouble("speed"), fieldSet.readInt("direction"));
                case "TEMP":
                    return new TemperatureReading(stationId, taken, fieldSet.readDouble("temperature"));
                default:
                    throw new IllegalArgumentException("Unknown type of measurement: [" + measurementType + "]");
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<? super SensorReading, ? extends SensorReading> readingsNormalizer(DataSource dataSource) {
//		return new ReadingNormalizingItemProcessor(dataSource);
        var processor = new ClassifierCompositeItemProcessor<SensorReading, SensorReading>();
        processor.setClassifier(getProcessorClassifier(null));
        return processor;
    }

    @Bean
    public SensorReadingItemProcessorClassifier getProcessorClassifier(NamedParameterJdbcTemplate jdbcTemplate) {
        return new SensorReadingItemProcessorClassifier(jdbcTemplate);
    }


    @Bean
    public ClassifierCompositeItemWriter<SensorReading> sensorReadingItemWriter() {
        var classifier = new InsertingReadingWriterClassifier(tempReadingItemWriter(null), windReadingItemWriter(null));
        var compositeItemWriter = new ClassifierCompositeItemWriter<SensorReading>();
        compositeItemWriter.setClassifier(classifier);
        return compositeItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter<SensorReading> tempReadingItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SensorReading>()
            .beanMapped()
            .sql("INSERT INTO TEMPERATURE_READING (station_id, taken, temperature) " +
                "VALUES (:stationId, :taken, :temperature)")
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<SensorReading> windReadingItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SensorReading>()
            .beanMapped()
            .sql("INSERT INTO WIND_READING (station_id, taken, speed, direction) " +
                "VALUES (:stationId, :taken, :speed, :direction)")
            .dataSource(dataSource)
            .build();
    }

}

@XmlRootElement(name = "station")
class Station {
    private UUID id;
    private String tempUnit;
    private String speedUnit;

    public Station() {
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    public void setSpeedUnit(String speedUnit) {
        this.speedUnit = speedUnit;
    }

    public UUID getId() {
        return id;
    }

    public String getTempUnit() {
        return tempUnit;
    }

    public String getSpeedUnit() {
        return speedUnit;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Station.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("tempUnit='" + tempUnit + "'")
            .add("speedUnit='" + speedUnit + "'")
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

interface SensorReading {
    UUID getStationId();
    Instant getTaken();
}

class TemperatureReading implements SensorReading {
    private UUID stationId;
    private Instant taken;
    private double temperature;

    public TemperatureReading(UUID stationId, Instant taken, double temperature) {
        assert(stationId != null);
        this.stationId = stationId;
        this.taken = taken;
        this.temperature = temperature;
    }

    @Override
    public UUID getStationId() {
        return stationId;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
    }

    @Override
    public Instant getTaken() {
        return taken;
    }

    public void setTaken(Instant taken) {
        this.taken = taken;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemperatureReading that = (TemperatureReading) o;
        return Double.compare(that.temperature, temperature) == 0 && Objects.equals(stationId, that.stationId) && Objects.equals(taken, that.taken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId, taken, temperature);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TemperatureReading.class.getSimpleName() + "[", "]")
            .add("stationId=" + stationId)
            .add("taken=" + taken)
            .add("measurementValue=" + temperature)
            .toString();
    }
}

class WindReading implements SensorReading {

    private UUID stationId;
    private Instant taken;
    private double speed;
    private int direction;

    public WindReading(UUID stationId, Instant taken, double speed, int direction) {
        assert (stationId != null);
        this.stationId = stationId;
        this.taken = taken;
        this.speed = speed;
        this.direction = direction;
    }

    @Override
    public UUID getStationId() {
        return stationId;
    }

    @Override
    public Instant getTaken() {
        return taken;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
    }

    public void setTaken(Instant taken) {
        this.taken = taken;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WindReading that = (WindReading) o;
        return Double.compare(that.speed, speed) == 0 && direction == that.direction && stationId.equals(that.stationId) && taken.equals(that.taken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId, taken, speed, direction);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WindReading.class.getSimpleName() + "[", "]")
            .add("stationId=" + stationId)
            .add("taken=" + taken)
            .add("speed=" + speed)
            .add("direction=" + direction)
            .toString();
    }
}

class SensorReadingItemProcessorClassifier implements Classifier<SensorReading, ItemProcessor<?, ? extends SensorReading>> {
    @SuppressWarnings("SqlResolve")
    public static final String UNITS_QUERY = "SELECT temp_unit AS TEMP, speed_unit as SPEED FROM station WHERE id = :id";
    private final ConcurrentHashMap<UUID, Map<String, DoubleUnaryOperator>> stationUnits = new ConcurrentHashMap<>();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SensorReadingItemProcessorClassifier(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ItemProcessor<?, ? extends SensorReading> classify(SensorReading item) {
        var stationId = item.getStationId();
        var conversionOperators = stationUnits.computeIfAbsent(stationId, this::fetchConversionOperationsForStation);
        if (item instanceof TemperatureReading) {
            return tempNormalizingProcessor(conversionOperators.get("TEMP"));
        } else if (item instanceof WindReading) {
            return windNormalizingProcessor(conversionOperators.get("SPEED"));
        } else {
            throw new IllegalArgumentException("Unknown reading [" + item + "]");
        }
    }

    private ItemProcessor<TemperatureReading,? extends SensorReading> tempNormalizingProcessor(DoubleUnaryOperator operator) {
        return (ItemProcessor<TemperatureReading, SensorReading>) item -> {
            item.setTemperature(operator.applyAsDouble(item.getTemperature()));
            return item;
        };
    }

    private ItemProcessor<WindReading,? extends SensorReading> windNormalizingProcessor(DoubleUnaryOperator operator) {
        return (ItemProcessor<WindReading, SensorReading>) item -> {
            item.setSpeed(operator.applyAsDouble(item.getSpeed()));
            return item;
        };
    }

    private Map<String, DoubleUnaryOperator> fetchConversionOperationsForStation(UUID uuid) {
        return jdbcTemplate.queryForMap(UNITS_QUERY, Map.of("id", uuid)).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, this::getConversionOperator));
    }

    private DoubleUnaryOperator getConversionOperator(Map.Entry<String, Object> e) {
        switch ((String) e.getValue()) {
            case "m/s":
            case "C":
                return value -> value;
            case "F":
                return value -> (value - 32) / 1.8;
            case "km/h":
                return value -> value * 0.27778;
            case "mph":
                return value -> value * 0.44704;
            default:
                throw new IllegalArgumentException("No conversion supported for " + e.getValue());
        }
    }
}

class InsertingReadingWriterClassifier implements Classifier<SensorReading, ItemWriter<? super SensorReading>> {
    private final JdbcBatchItemWriter<SensorReading> tempWriter;
    private final JdbcBatchItemWriter<SensorReading> windWriter;

    public InsertingReadingWriterClassifier(JdbcBatchItemWriter<SensorReading> tempWriter, JdbcBatchItemWriter<SensorReading> windWriter) {
        this.tempWriter = tempWriter;
        this.windWriter = windWriter;
    }

    @Override
    public ItemWriter<? super SensorReading> classify(SensorReading reading) {
        if (reading instanceof TemperatureReading) {
            return tempWriter;
        }
        if (reading instanceof WindReading) {
            return windWriter;
        }
        throw new IllegalArgumentException("Cannot classify [" + reading + "].");
    }

}