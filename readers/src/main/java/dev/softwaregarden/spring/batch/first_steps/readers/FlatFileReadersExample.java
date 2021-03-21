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

import dev.softwaregarden.spring.batch.first_steps.readers.util.FullNameAndEmail;
import dev.softwaregarden.spring.batch.first_steps.readers.util.NameAndEmail;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@Configuration
public class FlatFileReadersExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public FlatFileReadersExample(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job flatFilesReaderJob() throws Exception {
        return jobBuilderFactory.get("flatFilesReadersJob")
            .start(csvReaderStep())
            .next(csvReaderWithMapperStep())
            .next(csvReaderWithTokenizerStep())
            .next(fixedRecordStep())
            .build();
    }

    @Bean
    public Step csvReaderStep() throws Exception {
        return stepBuilderFactory.get("csvReader")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(csvFileReader(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
//    @StepScope
    public FlatFileItemReader<NameAndEmail> csvFileReader(@Value("classpath:namesAndEmails.csv") Resource namesAndEmailsFile) {
        return new FlatFileItemReaderBuilder<NameAndEmail>()
            .name("csvReader")
            .resource(namesAndEmailsFile)
            .delimited()
            .delimiter(";")
            .names("firstName", "lastName", "email")
            .targetType(NameAndEmail.class)
            .linesToSkip(1).build();
    }

//    @Bean
//    @Lazy
//    public FlatFileItemReader<NameAndEmail> csvFileReaderConstructedManually(@Value("classpath:namesAndEmails.csv") Resource namesAndEmailsFile) throws Exception {
//        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer(";");
//        lineTokenizer.setNames("firstName", "lastName", "email");
//        lineTokenizer.afterPropertiesSet();
//        BeanWrapperFieldSetMapper<NameAndEmail> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//        fieldSetMapper.setTargetType(NameAndEmail.class);
//        fieldSetMapper.afterPropertiesSet();
//        DefaultLineMapper<NameAndEmail> lineMapper = new DefaultLineMapper<>();
//        lineMapper.setLineTokenizer(lineTokenizer);
//        lineMapper.setFieldSetMapper(fieldSetMapper);
//        return new FlatFileItemReaderBuilder<NameAndEmail>()
//            .name("check")
//            .resource(namesAndEmailsFile)
//            .lineMapper(lineMapper)
//            .build();
//    }

    @Bean
    public Step csvReaderWithMapperStep() throws Exception {
        return stepBuilderFactory.get("csvReaderWithMapper")
            .<FullNameAndEmail, FullNameAndEmail>chunk(3)
            .reader(csvFileReaderWithMapper(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
//    @StepScope
    public FlatFileItemReader<FullNameAndEmail> csvFileReaderWithMapper(@Value("classpath:namesAndEmails.csv") Resource namesAndEmailsFile) {
        return new FlatFileItemReaderBuilder<FullNameAndEmail>()
            .name("csvReader")
            .resource(namesAndEmailsFile)
            .delimited()
            .delimiter(";")
            .names("firstName", "lastName", "email")
            .fieldSetMapper(fieldSet -> new FullNameAndEmail(
                fieldSet.readString("firstName") + " "+fieldSet.readString("lastName"),
                fieldSet.readString("email")))
            .linesToSkip(1)
            .build();
    }

    @Bean
    public Step csvReaderWithTokenizerStep() throws Exception {
        return stepBuilderFactory.get("csvReaderWithTokenizer")
            .<FullNameAndEmail, FullNameAndEmail>chunk(3)
            .reader(csvFileReaderWithTokenizer(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
//    @StepScope
    public FlatFileItemReader<FullNameAndEmail> csvFileReaderWithTokenizer(@Value("classpath:namesAndEmails.csv") Resource namesAndEmailsFile) {
        return new FlatFileItemReaderBuilder<FullNameAndEmail>()
            .name("csvReader")
            .resource(namesAndEmailsFile)
            .lineTokenizer(new FullNameLineTokenizer())
            .targetType(FullNameAndEmail.class)
            .linesToSkip(1)
            .build();
    }

    @Bean
    public Step fixedRecordStep() {
        return stepBuilderFactory.get("fixedRecordReader")
            .<NameAndEmail, NameAndEmail>chunk(3)
            .reader(fixedRecordFileReader(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    public FlatFileItemReader<? extends NameAndEmail> fixedRecordFileReader(@Value("classpath:namesAndEmails.fixed") Resource namesAndEmailsFile) {
        return new FlatFileItemReaderBuilder<NameAndEmail>()
            .name("fixedRecordReader")
            .resource(namesAndEmailsFile)
            .fixedLength()
            .columns(new Range(1, 20), new Range(21, 40), new Range(41))
            .names("firstName", "lastName", "email")
            .targetType(NameAndEmail.class)
            .build();
    }

}

class FullNameLineTokenizer implements LineTokenizer {

    private final String[] names = {"fullName", "email"};

    @Override
    public FieldSet tokenize(String line) {
        var fields = line.split(";");
        var parsed = List.of(fields[0] + " " + fields[1], fields[2]);
        return new DefaultFieldSetFactory().create(parsed.toArray(String[]::new), names);
    }
}

