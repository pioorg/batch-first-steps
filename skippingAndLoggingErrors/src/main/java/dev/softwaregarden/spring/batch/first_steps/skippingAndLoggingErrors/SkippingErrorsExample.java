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

import java.math.BigDecimal;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@EnableBatchProcessing
@Configuration
public class SkippingErrorsExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public SkippingErrorsExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job skippingErrorsExampleJob() throws Exception {
        return jobBuilderFactory.get("skippingErrorsJob")
            .start(skippingErrorStep())
            .build();
    }

    @Bean
    public Step skippingErrorStep() throws Exception {
        return stepBuilderFactory.get("skippingErrorsStep")
            .<BigDecimal, BigDecimal>chunk(3)
            .reader(accountOperationsReader(null))
            .writer(System.out::println)
            .faultTolerant()
            .skipLimit(1)
            .skip(FlatFileParseException.class)
//            .skipPolicy(amountSkipPolicy())
            .listener(new AmountItemListener())
//            .listener((ItemReadListener<? super BigDecimal>) new DefaultItemFailureHandler())
            .build();
    }


    @Bean
//    @StepScope
    public FlatFileItemReader<BigDecimal> accountOperationsReader(@Value("classpath:accountOperations.csv") Resource file) {
        return new FlatFileItemReaderBuilder<BigDecimal>()
            .name("csvReader")
            .resource(file)
            .delimited()
            .names("amount")
            .fieldSetMapper(fieldSet -> new BigDecimal(fieldSet.readString("amount")))
            .build();
    }

    @Bean
    public SkipPolicy amountSkipPolicy() {
        return (t, skipCount) -> (t instanceof FlatFileParseException) && skipCount < 1;
    }

}

@Component
class AmountItemListener {

    @OnReadError
    public void onReadError(Exception e) {
        System.out.println("Detected the following exception: ["+e.getMessage()+"].");
    }
}


