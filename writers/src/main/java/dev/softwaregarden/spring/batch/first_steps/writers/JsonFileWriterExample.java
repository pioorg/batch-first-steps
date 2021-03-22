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
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@Configuration
public class JsonFileWriterExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final Resource namesAndEmailsFile;

    public JsonFileWriterExample(JobBuilderFactory jobBuilderFactory,
                                 StepBuilderFactory stepBuilderFactory,
                                 @Value("file:tmp/namesAndEmails.json") Resource namesAndEmailsFile) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.namesAndEmailsFile = namesAndEmailsFile;
    }

    @Bean
    public Job jsonFileWriterJob() throws Exception {
        return jobBuilderFactory.get("jsonFileWriterJob")
            .start(jsonWriterStep())
            .build();
    }

    @Bean
    public Step jsonWriterStep() throws Exception {
        return stepBuilderFactory.get("jsonWriter")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(new NameAndEmailDummyReader())
            .writer(jsonFileItemWriter())
            .build();
    }

    @Bean
    public JsonFileItemWriter<NameAndEmail> jsonFileItemWriter() {
        return new JsonFileItemWriterBuilder<NameAndEmail>()
            .name("jsonWriter")
            .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
            .resource(namesAndEmailsFile)
            .build();
    }
}


