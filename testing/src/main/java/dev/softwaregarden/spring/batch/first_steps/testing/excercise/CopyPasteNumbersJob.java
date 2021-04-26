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

package dev.softwaregarden.spring.batch.first_steps.testing.excercise;

import java.util.Objects;
import java.util.StringJoiner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@Configuration
public class CopyPasteNumbersJob {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    public CopyPasteNumbersJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean()
    public Job numbersBatchJob() {
        return jobBuilderFactory.get("copyPasteNumbers")
            .start(importNumberStep())
//			.incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    public Step importNumberStep() {
        return stepBuilderFactory.get("importNumbersStep")
            .<ParsedNumber, ParsedNumber>chunk(3)
            .reader(numberParser(null))
            .writer(numberWriter(null))
            .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<ParsedNumber> numberParser(@Value("#{jobParameters['numbersFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<ParsedNumber>()
            .name("numberReader")
            .resource(inputFile)
            .lineTokenizer(new DelimitedLineTokenizer())
            .fieldSetMapper(parsedNumberFieldSetMapper())
            .build();
    }

    @Bean
    public FieldSetMapper<ParsedNumber> parsedNumberFieldSetMapper() {
        return fieldSet -> new ParsedNumber(fieldSet.readInt(0));
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ParsedNumber> numberWriter(@Value("#{jobParameters['parsedNumbersFile']}") Resource outputFile) {
        return new FlatFileItemWriterBuilder<ParsedNumber>()
            .name("numberWriter")
            .resource(outputFile)
            .delimited()
            .fieldExtractor(pn -> new Object[]{pn.getNumber()})
            .build();
    }

}

class ParsedNumber {

    private int number;

    public ParsedNumber() {
    }

    public ParsedNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedNumber that = (ParsedNumber) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ParsedNumber.class.getSimpleName() + "[", "]")
            .add("number=" + number)
            .toString();
    }
}