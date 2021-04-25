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

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
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
