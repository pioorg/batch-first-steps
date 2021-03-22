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

import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmail;
import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmailDummyReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@Configuration
public class FlatFileWritersExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public FlatFileWritersExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job flatFilesWriterJob() throws Exception {
        return jobBuilderFactory.get("flatFilesWritersJob")
            .start(csvWriterStep())
            .next(fixedRecordStep())
            .build();
    }

    @Bean
    public Step csvWriterStep() throws Exception {
        return stepBuilderFactory.get("csvWriter")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(new NameAndEmailDummyReader())
            .writer(csvFileWriter(null))
            .build();
    }

    @Bean
//    @StepScope
    public FlatFileItemWriter<NameAndEmail> csvFileWriter(@Value("file:tmp/namesAndEmails.csv") Resource namesAndEmailsFile) {
        return new FlatFileItemWriterBuilder<NameAndEmail>()
            .name("csvWriter")
            .resource(namesAndEmailsFile)
            .delimited()
            .delimiter(";")
            .names("firstName", "lastName", "email")
            .shouldDeleteIfExists(true)
            .headerCallback(writer -> writer.write("first name;last name;email"))
            .build();
    }


    @Bean
    public Step fixedRecordStep() {
        return stepBuilderFactory.get("fixedRecordWriter")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(new NameAndEmailDummyReader())
            .writer(fixedRecordFileWriter(null))
            .build();
    }

    @Bean
    public FlatFileItemWriter<? super NameAndEmail> fixedRecordFileWriter(@Value("file:tmp/namesAndEmails.fixed") Resource namesAndEmailsFile) {
        return new FlatFileItemWriterBuilder<NameAndEmail>()
            .name("fixedRecordWriter")
            .resource(namesAndEmailsFile)
            .shouldDeleteIfExists(true)
            .formatted()
            .format("%-20s%-20s%-30s")
            .fieldExtractor(item -> new Object[] {item.getFirstName(), item.getLastName(), item.getEmail()})
            .build();
    }

}


