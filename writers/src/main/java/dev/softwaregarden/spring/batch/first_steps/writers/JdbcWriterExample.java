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

package dev.softwaregarden.spring.batch.first_steps.writers;

import javax.sql.DataSource;

import dev.softwaregarden.spring.batch.first_steps.writers.util.JdbcCharacter;
import dev.softwaregarden.spring.batch.first_steps.writers.util.JdbcCharacterDummyReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class JdbcWriterExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public JdbcWriterExample(JobBuilderFactory jobBuilderFactory,
                             StepBuilderFactory stepBuilderFactory,
                             DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job jdbcWriterJob() throws Exception {
        return jobBuilderFactory.get("jdbcWritersJob")
            .start(jdbcBatchWriterStep())
            .build();
    }

    @Bean
    public Step jdbcBatchWriterStep() throws Exception {
        return stepBuilderFactory.get("jdbcBatchWriter")
            .<JdbcCharacter, JdbcCharacter>chunk(3)
            .reader(new JdbcCharacterDummyReader())
            .writer(jdbcBatchWriter())
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<JdbcCharacter> jdbcBatchWriter() {
        return new JdbcBatchItemWriterBuilder<JdbcCharacter>()
            .dataSource(dataSource)
            .sql("INSERT INTO characters (first_name, last_name, email) VALUES (:firstName, :lastName, :email)")
            .beanMapped()
            .assertUpdates(true)
            .build();
    }

}


