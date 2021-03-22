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

import java.util.Map;

import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmail;
import dev.softwaregarden.spring.batch.first_steps.writers.util.NameAndEmailDummyReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.oxm.xstream.XStreamMarshaller;

@EnableBatchProcessing
@Configuration
public class XmlFileWriterExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public XmlFileWriterExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }


    @Bean
    public Job xmlFileWriterJob() throws Exception {
        return jobBuilderFactory.get("xmlFileWriterJob")
            .start(xmlWriterStep())
            .build();
    }

    @Bean
    public Step xmlWriterStep() throws Exception {
        return stepBuilderFactory.get("xmlWriter")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(new NameAndEmailDummyReader())
            .writer(xmlFileWriter(null))
            .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<NameAndEmail> xmlFileWriter(@Value("file:tmp/namesAndEmails.xml") Resource namesAndEmailsFile) {
        return new StaxEventItemWriterBuilder<NameAndEmail>()
            .name("xmlWriter")
            .resource(namesAndEmailsFile)
            .marshaller(characterMarshaller())
            .rootTagName("characters")
            .version("1.0")
            .overwriteOutput(true)
            .build();
    }

//    @Bean
//    public Jaxb2Marshaller jaxb2characterMarshaller() {
//        var jaxb2Marshaller = new Jaxb2Marshaller();
//        jaxb2Marshaller.setClassesToBeBound(NameAndEmail.class);
//        jaxb2Marshaller.setMarshallerProperties(Map.of(Marshaller.JAXB_FORMATTED_OUTPUT, true));
//        return jaxb2Marshaller;
//    }

    @Bean
    @StepScope
    public XStreamMarshaller characterMarshaller() {
        var marshaller = new XStreamMarshaller();
        marshaller.setAliases(Map.of("character", NameAndEmail.class));
        return marshaller;
    }


}


