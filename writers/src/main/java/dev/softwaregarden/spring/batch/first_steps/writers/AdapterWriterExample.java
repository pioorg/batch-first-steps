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

import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmail;
import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmailDummyReader;
import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmailService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class AdapterWriterExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public AdapterWriterExample(JobBuilderFactory jobBuilderFactory,
                                StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job adapterWriterJob() throws Exception {
        return jobBuilderFactory.get("adapterWriterJob")
            .start(adapterWriterStep())
            .build();
    }

    @Bean
    public Step adapterWriterStep() throws Exception {
        return stepBuilderFactory.get("adapterWriter")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(new NameAndEmailDummyReader())
            .writer(adapterWriter(null))
            .build();
    }

    @Bean
    public ItemWriterAdapter<NameAndEmail> adapterWriter(NameAndEmailService nameAndEmailService) {
        var adapter = new ItemWriterAdapter<NameAndEmail>();
        adapter.setTargetObject(nameAndEmailService);
        adapter.setTargetMethod("save");
        return adapter;
    }
}



