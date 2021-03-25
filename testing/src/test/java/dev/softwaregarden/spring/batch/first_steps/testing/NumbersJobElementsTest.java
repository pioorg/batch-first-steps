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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

//@ExtendWith(SpringExtension.class) // Spring support for JUnit 5
//@ContextConfiguration(classes = NumbersJobConfiguration.class) //needed to build ApplicationContext
@SpringJUnitConfig(classes = NumbersJobConfiguration.class)
@JdbcTest //creates the in-memory DB and initializes it, initializes JdbcTemplate
@SpringBatchTest
public class NumbersJobElementsTest {

    @Autowired
    private FlatFileItemReader<Long> numbersReader;

    @Autowired
    private JdbcBatchItemWriter<Long> numbersWriter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Called by {@link org.springframework.batch.test.StepScopeTestExecutionListener}
     */
    public StepExecution getStepExecution() {
        var jobParams = new JobParametersBuilder()
            .addString("numbersFile", "classpath:testNumbers.csv")
            .toJobParameters();
        return MetaDataInstanceFactory.createStepExecution(jobParams);
    }

    @Test
    void readerShouldReadNumbers() throws Exception {
        var executionContext = new ExecutionContext();
        numbersReader.open(executionContext);
        var readNumbers = new ArrayList<Long>();
        Long number;
        do {
            number = numbersReader.read();
            readNumbers.add(number);
        } while (number != null);
        readNumbers.remove(readNumbers.size() - 1);
        Assertions.assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L), readNumbers);
    }

    @Test
    void writerShouldSaveNumbers() throws Exception {
        var numbers = List.of(11L, 12L, 13L, 42L);
        numbersWriter.write(numbers);
        var savedNumbers = jdbcTemplate.query("SELECT * FROM numbers ORDER BY value;", (rs, rowNum) -> rs.getLong(1));
        Assertions.assertEquals(numbers, savedNumbers);
    }


}