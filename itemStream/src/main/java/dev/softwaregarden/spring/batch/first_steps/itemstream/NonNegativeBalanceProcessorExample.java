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

package dev.softwaregarden.spring.batch.first_steps.itemstream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class NonNegativeBalanceProcessorExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public NonNegativeBalanceProcessorExample(JobBuilderFactory jobBuilderFactory,
                                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job cumulativeValidatingExampleJob() throws Exception {
        return jobBuilderFactory.get("cumulativeValidatingJob")
            .start(balanceValidatingStep())
            .build();
    }

    @Bean
    public Step balanceValidatingStep() throws Exception {
        return stepBuilderFactory.get("cumulativeValidatingProcessor")
            .<Long, Long>chunk(3)
//            .reader(new ListItemReader<>(List.of(0L, 1L, 2L, -5L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)))
            .reader(dummyLongReader())
            .processor(cumulativeValidatingItemProcessor())
            .writer(System.out::println)
            .stream(cumulativeValidator())
            .build();
    }

    @Bean
    public DummyLongItemReader dummyLongReader() {
        var reader = new DummyLongItemReader();
        reader.setName("dummyLongReader");
        return reader;
    }

    @Bean
    public ItemProcessor<Long, Long> cumulativeValidatingItemProcessor() {
        var processor = new ValidatingItemProcessor<Long>();
        processor.setFilter(false);
        processor.setValidator(cumulativeValidator());
        return processor;
    }

    @Bean
    public CumulativeValidator cumulativeValidator(){
        var cumulativeValidator = new CumulativeValidator();
        cumulativeValidator.setName("cumulativeValidator");
        return cumulativeValidator;
    }

}


