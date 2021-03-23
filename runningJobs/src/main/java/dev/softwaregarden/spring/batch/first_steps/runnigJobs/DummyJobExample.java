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

package dev.softwaregarden.spring.batch.first_steps.runnigJobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class DummyJobExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public DummyJobExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job dummyJob() {
        return jobBuilderFactory.get("dummyJob")
            .incrementer(new RunIdIncrementer())
            .start(dummyStep())
            .build();
    }

    @Bean
    public TaskletStep dummyStep() {
        return stepBuilderFactory.get("dummyStep")
            .tasklet(dummyTasklet(null))
            .build();
    }

    @Bean
    @StepScope
    public Tasklet dummyTasklet(@Value("#{jobParameters['message'] ?: 'nothing'}") String message) {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.err.println("I want to tell you: " + message);
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    public Job dummyLongJob() {
        return jobBuilderFactory.get("dummyLongJob")
            .incrementer(new RunIdIncrementer())
            .start(dummyLongStep())
            .build();
    }

    @Bean
    public TaskletStep dummyLongStep() {
        return stepBuilderFactory.get("dummyLongStep")
            .<Long, Long>chunk(1)
            .reader(dummyLongReader(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<Long> dummyLongReader(@Value("#{jobParameters['repeats'] ?: 0}") Long repeats) {
        return new ItemReader<Long>() {
            long repeatsLeft = repeats;

            @Override
            public Long read() throws Exception {
                System.err.printf("I will annoy you %d times more!%n", repeatsLeft);
                Thread.sleep(1000L);
                if (repeatsLeft-- > 0) {
                    return repeatsLeft;
                }
                return null;
            }
        };
    }

    @Bean
    public Job dummyFailingJob() {
        return jobBuilderFactory.get("dummyFailingJob")
            .start(dummyFailingStep())
//            .preventRestart()
            .build();
    }

    @Bean
    public Step dummyFailingStep() {
        return stepBuilderFactory.get("dummyFailingStep")
            .startLimit(2)
            .<Long, Long>chunk(3)
            .reader(dummyFailingReader(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    @StepScope
    public FailingItemReader dummyFailingReader(@Value("#{jobParameters['failWith'] ?: 5}") Long failWith) {
        return new FailingItemReader(failWith);
    }


}


