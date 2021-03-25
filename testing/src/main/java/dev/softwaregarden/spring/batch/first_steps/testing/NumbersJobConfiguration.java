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

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@Configuration
public class NumbersJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public NumbersJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job numbersJob() {
        return jobBuilderFactory.get("numbers")
            .start(importNumbersStep())
            .next(infoStep())
            .build();
    }

    @Bean
    public Step importNumbersStep() {
        return stepBuilderFactory.get("importNumbersStep")
            .<Long, Long>chunk(2)
            .reader(numbersReader(null))
            .processor(doublingProcessor())
            .writer(numbersWriter())
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Long> numbersReader(@Value("#{jobParameters['numbersFile']}") Resource numbersFile) {
        return new FlatFileItemReaderBuilder<Long>()
            .name("numbersReader")
            .resource(numbersFile)
            .lineMapper((line, lineNumber) -> Long.valueOf(line))
            .build();
    }

    @Bean
    public ItemProcessor<Long, Long> doublingProcessor() {
        return aLong -> aLong + aLong;
    }

    @Bean
    public JdbcBatchItemWriter<Long> numbersWriter() {
        return new JdbcBatchItemWriterBuilder<Long>()
            .dataSource(dataSource)
            .sql("INSERT INTO numbers (value) VALUES (?)")
            .itemPreparedStatementSetter((item, ps) -> ps.setLong(1, item))
            .assertUpdates(true)
            .build();
    }

    @Bean
    public TaskletStep infoStep() {
        return stepBuilderFactory.get("infoStep")
            .tasklet((contribution, chunkContext) -> {
                System.out.println("FIN");
                return RepeatStatus.FINISHED;
            }).build();
    }


}
