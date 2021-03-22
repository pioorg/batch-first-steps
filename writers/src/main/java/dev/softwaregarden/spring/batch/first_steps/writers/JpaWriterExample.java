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

import javax.persistence.EntityManagerFactory;

import dev.softwaregarden.spring.batch.first_steps.writers.util.JpaCharacter;
import dev.softwaregarden.spring.batch.first_steps.writers.util.JpaCharacterDummyReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class JpaWriterExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public JpaWriterExample(JobBuilderFactory jobBuilderFactory,
                            StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job jpaWriterJob() throws Exception {
        return jobBuilderFactory.get("jpaWriterJob")
            .start(jpaPagingWriterStep())
            .build();
    }

    @Bean
    public Step jpaPagingWriterStep() throws Exception {
        return stepBuilderFactory.get("jpaPagingWriter")
            .<JpaCharacter, JpaCharacter>chunk(3)
            .reader(new JpaCharacterDummyReader())
            .writer(jpaPagingWriter())
            .build();
    }

    @Bean
    @StepScope
    public JpaItemWriter<JpaCharacter> jpaPagingWriter() {
        return new JpaItemWriterBuilder<JpaCharacter>()
            .entityManagerFactory(entityManagerFactory)
            .build();
    }

}


