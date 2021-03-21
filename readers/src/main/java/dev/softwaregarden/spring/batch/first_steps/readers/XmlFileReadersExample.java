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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@EnableBatchProcessing
@Configuration
public class XmlFileReadersExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public XmlFileReadersExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job xmlFilesReaderJob() throws Exception {
        return jobBuilderFactory.get("xmlFilesReadersJob")
            .start(xmlReaderStep())
            .build();
    }

    @Bean
    public Step xmlReaderStep() throws Exception {
        return stepBuilderFactory.get("xmlReader")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(xmlFileReader(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
//    @StepScope
    public StaxEventItemReader<NameAndEmail> xmlFileReader(@Value("classpath:namesAndEmails.xml") Resource namesAndEmailsFile) {
        return new StaxEventItemReaderBuilder<NameAndEmail>()
            .name("xmlReader")
            .resource(namesAndEmailsFile)
            .addFragmentRootElements("character")
            .unmarshaller(characterMarshaller())
            .build();
    }

    @Bean
    public Jaxb2Marshaller characterMarshaller() {
        var jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(NameAndEmail.class);
        return jaxb2Marshaller;
    }
}


