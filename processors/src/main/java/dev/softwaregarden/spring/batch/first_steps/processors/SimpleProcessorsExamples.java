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

import java.util.List;

import dev.softwaregarden.spring.batch.first_steps.processors.util.LongService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class SimpleProcessorsExamples {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public SimpleProcessorsExamples(JobBuilderFactory jobBuilderFactory,
                                    StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job simpleProcessorsExamplesJob() throws Exception {
        return jobBuilderFactory.get("simpleProcessorsJob")
//            .start(simpleTransformingStep())
//            .next(simpleFilteringStep())
//            .next(simpleAdapterStep())
//            .next(simpleAdapterStep())
            .start(compositeStep())
            .build();
    }

    @Bean
    public Step simpleTransformingStep() throws Exception {
        return stepBuilderFactory.get("simpleTransformingProcessor")
            .<Long, String>chunk(3)
            .reader(new ListItemReader<>(List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)))
//            .processor((ItemProcessor<? super Long, ? extends String>) elem ->  String.valueOf(elem) + elem)
            .processor((java.util.function.Function<Long, String>) elem -> "" + elem + elem)
            .writer(System.out::println)
            .build();
    }


    @Bean
    public Step simpleFilteringStep() {
        return stepBuilderFactory.get("simpleFilteringProcessor")
            .<Long, Long>chunk(3)
            .reader(new ListItemReader<>(List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)))
            .processor(passingOnlyOddProcessor())
            .writer(System.out::println)
            .build();
    }

    public ItemProcessor<Long, Long> passingOnlyOddProcessor() {
        return item -> {
            if (item % 2 == 0) {
                return null;
            }
            return item;
        };
    }

    @Bean
    public Step simpleAdapterStep() {
        return stepBuilderFactory.get("simpleAdapterProcessor")
            .<Long, Long>chunk(3)
            .reader(new ListItemReader<>(List.of(0L, -1L, 2L, -3L, 4L, -5L, 6L, -7L, 8L, -9L, 10L)))
            .processor(adapterProcessor(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    public ItemProcessor<Long, Long> adapterProcessor(LongService longService) {
        var adapter = new ItemProcessorAdapter<Long, Long>();
        adapter.setTargetObject(longService);
        adapter.setTargetMethod("absolute");
        return adapter;
    }

    @Bean
    public Step compositeStep() {
        return stepBuilderFactory.get("simpleCompositeProcessor")
            .<Long, Long>chunk(3)
            .reader(new ListItemReader<>(List.of(0L, -1L, 2L, -3L, 4L, -5L, 6L, -7L, 8L, -9L, 10L)))
            .processor(compositeProcessor())
            .writer(System.out::println)
            .build();
    }

    @Bean
    public ItemProcessor<? super Long, Long> compositeProcessor() {
        var processor = new CompositeItemProcessor<Long, Long>();
        processor.setDelegates(List.of(passingOnlyOddProcessor(), adapterProcessor(null)));
        return processor;
    }


}


