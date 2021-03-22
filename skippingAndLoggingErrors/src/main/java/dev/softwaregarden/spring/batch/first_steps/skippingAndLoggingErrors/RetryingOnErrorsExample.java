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

package dev.softwaregarden.spring.batch.first_steps.skippingAndLoggingErrors;

import java.net.http.HttpTimeoutException;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class RetryingOnErrorsExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public RetryingOnErrorsExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job retryingOnErrorsExampleJob() throws Exception {
        return jobBuilderFactory.get("retryingOnErrorsJob")
            .start(retryingStep())
            .build();
    }


    @Bean
    public Step retryingStep() throws Exception {
        return stepBuilderFactory.get("retryingOnErrorsStep")
            .<Integer, Integer>chunk(3)
            .reader(dummyIntReader())
            .processor(networkServiceProcessor())
            .writer(System.out::println)
            .faultTolerant()
            .retryLimit(4)
            .retry(HttpTimeoutException.class)
            .build();
    }

    @Bean
    public ListItemReader<Integer> dummyIntReader() {
        return new ListItemReader<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Bean
    public <T> ItemProcessor<T, T> networkServiceProcessor() {
        return new ItemProcessor<>() {
            private int errors = 3;

            @Override
            public T process(T item) throws Exception {
                System.out.printf("Processing [%s]%n", item);
                if (errors <= 0) {
//                    errors = 2;
                    System.out.printf("Going to return [%s].%n", item);
                    return item;
                } else {
                    errors--;
                    System.out.println("Fake timeout");
                    throw new HttpTimeoutException("Fake timeout");
                }
            }
        };
    }

}

