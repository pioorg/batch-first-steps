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

package dev.softwaregarden.spring.batch.first_steps.testing;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(initializers = NumbersJobTestcontainers.PropertiesInitializer.class)
@Testcontainers
public class NumbersJobTestcontainers {

    @Container
    private final static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2");

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                .of("spring.datasource.driver-class-name=" + postgreSQLContainer.getDriverClassName(),
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword())
                .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    @Qualifier("numbersJob")
    private Job numbersJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private DataSource dataSource;

    @Test
    void importNumbersStepShouldImportNumbersToDb() throws Exception {
        //given
        var jobParams = new JobParametersBuilder()
            .addString("numbersFile", "classpath:testNumbers.csv")
            .toJobParameters();

        //when
        var jobExecution = jobLauncher.run(numbersJob, jobParams);

        //then
        var stepExecutions = List.copyOf(jobExecution.getStepExecutions());
        assertAll(() -> {
            assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
            assertEquals(2, stepExecutions.size());
            assertEquals(8, stepExecutions.get(0).getReadCount());
            assertEquals(8, stepExecutions.get(0).getWriteCount());

        });
        var execResult = runPsqlQuery("SELECT * FROM numbers ORDER BY value;");
        var savedNumbers = execResult.getStdout().lines().map(Long::valueOf).collect(Collectors.toList());

        assertEquals(List.of(2L, 4L, 6L, 8L, 10L, 12L, 14L, 16L), savedNumbers);

    }

    @Test
    void shouldRunUsingPostgreSql() throws Exception {
        //this is not a real test; it's only to demonstrate the DB used
        var execResult = runPsqlQuery("select version();");
        Assertions.assertTrue(execResult.getStdout().startsWith("PostgreSQL 13.2"));
    }

    private ExecResult runPsqlQuery(String query) throws IOException, InterruptedException {
        return postgreSQLContainer.execInContainer("psql", "-U", postgreSQLContainer.getDatabaseName(), "-Atc", query);
    }
}