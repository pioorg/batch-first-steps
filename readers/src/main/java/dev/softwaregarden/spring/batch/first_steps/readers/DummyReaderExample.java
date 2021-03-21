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

package dev.softwaregarden.spring.batch.first_steps.readers;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class DummyReaderExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public DummyReaderExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job dummyJob() {
        return jobBuilderFactory.get("sample")
            .start(aStep())
            .build();
    }

    @Bean
    public Step aStep() {
        return stepBuilderFactory.get("aStep")
            .<Integer, Integer>chunk(3)
            .reader(new DummyIntegerItemReader())
            .writer(System.out::println)
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

