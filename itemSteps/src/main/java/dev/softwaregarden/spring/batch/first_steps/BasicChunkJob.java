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

package dev.softwaregarden.spring.batch.first_steps;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class BasicChunkJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BasicChunkJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job basicChunksJob() {
        return jobBuilderFactory.get("basicChunks")
            .start(basicChunkStep())
            .build();
    }

    @Bean
    public Step basicChunkStep() {
        return stepBuilderFactory.get("basicChunkStep")
            .<Integer, String>chunk(5)
            .reader(new DummyIntegerItemReader())
            .processor(new IntegerToStringProcessor())
            .writer(new DummyObjectItemWriter())
            .build();
    }
}

class DummyIntegerItemReader implements ItemReader<Integer> {
    private final List<Integer> source = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private int position = 0;

    @Override
    public Integer read() {
        if (position >= source.size()) {
            System.out.println("No more items to read, returning null.");
            return null;
        }
        Integer nextItem = source.get(position++);
        System.out.println("Read item [" + nextItem + "].");
        return nextItem;
    }
}


class IntegerToStringProcessor implements ItemProcessor<Integer, String> {

    @Override
    public String process(Integer item) {
        System.out.println("Processing [" + item + "].");
        if (item == 6) {
            System.out.println("Filtering out [" + item + "].");
            return null;
        }
        return item.toString();
    }
}

class DummyObjectItemWriter implements ItemWriter<Object> {

    @Override
    public void write(List<?> items) {
        System.out.println("Received the following list of items to write: " + items + ".");
    }

}

