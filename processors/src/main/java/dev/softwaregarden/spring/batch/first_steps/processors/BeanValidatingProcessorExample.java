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

package dev.softwaregarden.spring.batch.first_steps.processors;

import dev.softwaregarden.spring.batch.first_steps.processors.util.NameAndEmail;
import dev.softwaregarden.spring.batch.first_steps.processors.util.NameAndEmailDummyReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class BeanValidatingProcessorExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BeanValidatingProcessorExample(JobBuilderFactory jobBuilderFactory,
                                          StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job beanValidatingExampleJob() throws Exception {
        return jobBuilderFactory.get("beanValidatingJob")
            .start(beanValidatingStep())
            .build();
    }

    @Bean
    public Step beanValidatingStep() throws Exception {
        return stepBuilderFactory.get("beanValidatingProcessor")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(new NameAndEmailDummyReader())
            .processor(beanValidatingItemProcessor())
            .writer(System.out::println)
            .build();
    }

    @Bean
    public BeanValidatingItemProcessor<NameAndEmail> beanValidatingItemProcessor() {
        var processor = new BeanValidatingItemProcessor<NameAndEmail>();
        processor.setFilter(true);
        return processor;
    }

}


