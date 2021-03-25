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

import java.util.List;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchDataSourceInitializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {NumbersJobConfiguration.class, BatchDataSourceInitializer.class})
public class NumbersJobTest {

//    @Autowired
//    @Qualifier("numbersJob")
//    private Job numbersJob;
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Autowired
//    private JobRepository jobRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

//    @BeforeEach
//    void setUp() {
//        jobLauncherTestUtils = new JobLauncherTestUtils();
//        jobLauncherTestUtils.setJob(numbersJob);
//        jobLauncherTestUtils.setJobLauncher(jobLauncher);
//        jobLauncherTestUtils.setJobRepository(jobRepository);
//    }

    @Test
    void importNumbersStepShouldImportNumbersToDb() throws Exception {
        //given
        var jobParams = new JobParametersBuilder()
            .addString("numbersFile", "classpath:testNumbers.csv")
            .toJobParameters();

        //when
        var jobExecution = jobLauncherTestUtils.launchJob(jobParams);
//        var jobExecution = jobLauncher.run(numbersJob, jobParams);

        //then
        var stepExecutions = List.copyOf(jobExecution.getStepExecutions());
        assertAll(() -> {
            assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
            assertEquals(2, stepExecutions.size());
            assertEquals(8, stepExecutions.get(0).getReadCount());
            assertEquals(8, stepExecutions.get(0).getWriteCount());

        });

        var savedNumbers = new JdbcTemplate(dataSource)
            .query("SELECT * FROM numbers ORDER BY value;", (rs, rowNum) -> rs.getLong(1));
        assertEquals(List.of(2L, 4L, 6L, 8L, 10L, 12L, 14L, 16L), savedNumbers);
    }


}