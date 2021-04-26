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

package dev.softwaregarden.spring.batch.first_steps.testing.exercise;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import dev.softwaregarden.spring.batch.first_steps.testing.excercise.CopyPasteNumbersJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchDataSourceInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {CopyPasteNumbersJob.class, BatchAutoConfiguration.class, BatchDataSourceInitializer.class})
public class CopyPasteNumbersTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    void shouldRestartJob(@TempDir File tempDir) throws Exception {
        var numbersFile = new File(tempDir, "numbers.txt");
        var parsedNumbersFile = new File(tempDir, "parsed.txt");

        Files.writeString(numbersFile.toPath(), generateBrokenFileContent(), StandardOpenOption.CREATE_NEW);

        //TODO finish this test to ensure the CopyPasteNumbersJob can restart and continue
    }

    private String generateBrokenFileContent() {
        return "1\n2\n3\n4\n5\n6\nseven\n8\n9";
    }

    private String generateCorrectFileContent() {
        return "1\n2\n3\n4\n5\n6\n7\n8\n9";
    }

}
