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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is an ugly data generator for one exercise.
 * Please make sure you never write production code like this.
 */
public class UglyDataGenerator {
    public static void main(String[] args) throws IOException {
        var directory = "./tmp/weather";
        var numberOfFiles = 16;
        var readingsPerFile = 1_000;
        var numberOfStations = 42;
        var minTemperatureCelsius = -100.0;
        var maxTemperatureCelsius = 60.0;
        var minWindMeters = 0;
        var maxWindMeters = 120;
        var random = new Random(555);
        var errorChance = 0.000;
        var decimalFormat = new DecimalFormat("#.#");

        var speedUnits = List.of("km/h", "mph", "m/s");
        var stationIds = IntStream.range(0, numberOfStations).mapToObj(i -> UUID.randomUUID()).collect(Collectors.toUnmodifiableList());

        Map<UUID, List<String>> stations = stationIds.stream()
            .collect(Collectors.toMap(
                id -> id,
                id -> List.of(random.nextInt(2) == 0 ? "C" : "F", speedUnits.get(random.nextInt(speedUnits.size())))));

        var parentDir = new File(directory);
        if (!parentDir.exists()) {
            throw new IllegalArgumentException("Target directory does not exist");
        }
        if (!parentDir.isDirectory()) {
            throw new IllegalArgumentException("Target directory is not a directory.");
        }
        if (!parentDir.canWrite()) {
            throw new IllegalArgumentException("Cannot write to the directory.");
        }

        var stationsFile = new File(parentDir, "stations.xml");
        if (stationsFile.exists()) {
            throw new IllegalStateException(String.format("File %s already exists!", stationsFile.getAbsolutePath()));
        }
        try (var writer = new PrintWriter((new FileWriter(stationsFile)))) {
            writer.println("<?xml version='1.0' encoding='UTF-8'?>");
            writer.println("<stations>");
            stations.forEach((key, units) -> {
                writer.println("  <station>");
                writer.println("    <id>" + key + "</id>");
                writer.println("    <tempUnit>" + units.get(0) + "</tempUnit>");
                writer.println("    <speedUnit>" + units.get(1) + "</speedUnit>");
                writer.println("    <description>Weather station of id "+key + "</description>");
                writer.println("  </station>");
            });
            writer.println("</stations>");
        }
        System.out.printf("Generated file %s.%n", stationsFile.getAbsolutePath());


        for (int f = 0; f < numberOfFiles; f++) {
            var file = new File(parentDir, String.format("%03d.csv", f));
            if (file.exists()) {
                throw new IllegalStateException(String.format("File %s already exists!", file.getAbsolutePath()));
            }
            try (var writer = new PrintWriter((new FileWriter(file)))) {
                writer.println("station;time;measurement type;measurement value");
                for (int r = 0; r < readingsPerFile; r++) {
                    var stationId = stationIds.get(random.nextInt(numberOfStations));
                    var units = stations.get(stationId);
                    var measurementType = random.nextBoolean()?"TEMP":"WIND";
                    var value = "";
                    if (measurementType.equals("TEMP")) {
                        if (random.nextDouble() < errorChance) {
                            value = "NaN";
                        } else {
                            double temp = minTemperatureCelsius + (maxTemperatureCelsius - minTemperatureCelsius) * random.nextDouble();
                            if (units.get(0).equals("F")) {
                                temp = temp * 1.8 + 32;
                            }
                            value = decimalFormat.format(temp);
                        }
                    } else {
                        if (random.nextDouble() < errorChance) {
                            value = "NaN";
                        } else {
                            double windSpeed = minWindMeters + (maxWindMeters - minWindMeters) * random.nextDouble();
                            switch (units.get(1)) {
                                case "m/s":
                                    break;
                                case "km/h":
                                    windSpeed *= 3.6;
                                    break;
                                case "mph":
                                    windSpeed *= 2.2369362920544;
                                    break;
                            }
                            int windDir = random.nextInt(360);
                            value = decimalFormat.format(windSpeed) + ";"+ windDir;
                        }
                    }
                    writer.println(String.format("%s;%s;%s;%s", stationId, Instant.now(), measurementType, value));
                }
                System.out.printf("Generated file %s.%n", file.getAbsolutePath());
            }
        }
    }
}
