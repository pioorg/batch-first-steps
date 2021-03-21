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

import dev.softwaregarden.spring.batch.first_steps.readers.util.NameAndEmail;
import dev.softwaregarden.spring.batch.first_steps.readers.util.NameAndEmailService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class AdapterReaderExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public AdapterReaderExample(JobBuilderFactory jobBuilderFactory,
                                StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job adapterReaderJob() throws Exception {
        return jobBuilderFactory.get("adapterReaderJob")
            .start(adapterReaderStep())
            .build();
    }

    @Bean
    public Step adapterReaderStep() throws Exception {
        return stepBuilderFactory.get("adapterReader")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(adapterReader(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    public ItemReaderAdapter<NameAndEmail> adapterReader(NameAndEmailService nameAndEmailService) {
        var adapter = new ItemReaderAdapter<NameAndEmail>();
        adapter.setTargetObject(nameAndEmailService);
        adapter.setTargetMethod("fetchNameAndEmail");
        return adapter;
    }
}



