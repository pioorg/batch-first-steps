///*
// *
// *  Copyright (C) 2021 Piotr Przybył
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// *
// */
//
//package dev.softwaregarden.spring.batch.first_steps.weather;
//
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.DoubleUnaryOperator;
//import java.util.stream.Collectors;
//
//import javax.sql.DataSource;
//
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class ReadingNormalizingItemProcessor implements ItemProcessor<SensorReading, SensorReading> {
//
//    @SuppressWarnings("SqlResolve")
//    public static final String UNITS_QUERY = "SELECT temp_unit AS TEMP, speed_unit as SPEED FROM station WHERE id = :id";
//    private final ConcurrentHashMap<UUID, Map<String, DoubleUnaryOperator>> stationUnits = new ConcurrentHashMap<>();
//
//    private final NamedParameterJdbcTemplate jdbcTemplate;
//
//    public ReadingNormalizingItemProcessor(DataSource dataSource) {
//        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
//    }
//
//    // avoid code like that, instead go for ClassifierCompositeItemProcessor
//    @Override
//    public SensorReading process(SensorReading item) throws Exception {
//        var stationId = item.getStationId();
//        var conversionOperators = stationUnits.computeIfAbsent(stationId, this::fetchConversionOperationsForStation);
//        double originalValue;
//        if (item instanceof TemperatureReading) {
//            originalValue = ((TemperatureReading) item).getTemperature();
//        } else if (item instanceof WindReading) {
//            originalValue = ((WindReading) item).getSpeed();
//        } else {
//            throw new IllegalArgumentException("Unknown type of reading ["+item + "]");
//        }
//        var conversionOperator = conversionOperators.get( (item instanceof TemperatureReading)? "TEMP": "SPEED");
//        var convertedValue = conversionOperator.applyAsDouble(originalValue);
//        if (item instanceof TemperatureReading) {
//            ((TemperatureReading) item).setTemperature(convertedValue);
//        } else {
//            ((WindReading) item).setSpeed(convertedValue);
//        }
//        return item;
//    }
//
//    private Map<String, DoubleUnaryOperator> fetchConversionOperationsForStation(UUID uuid) {
//        return jdbcTemplate.queryForMap(UNITS_QUERY, Map.of("id", uuid)).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, this::getConversionOperator));
//    }
//
//    private DoubleUnaryOperator getConversionOperator(Map.Entry<String, Object> e) {
//        switch ((String) e.getValue()) {
//            case "m/s":
//            case "C":
//                return value -> value;
//            case "F":
//                return value -> (value - 32) / 1.8;
//            case "km/h":
//                return value -> value * 0.27778;
//            case "mph":
//                return value -> value * 0.44704;
//            default:
//                throw new IllegalArgumentException("No conversion supported for " + e.getValue());
//        }
//    }
//}
