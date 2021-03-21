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

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import dev.softwaregarden.spring.batch.first_steps.readers.util.JpaCharacter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class JpaReaderExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public JpaReaderExample(JobBuilderFactory jobBuilderFactory,
                            StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job jpaReaderJob() throws Exception {
        return jobBuilderFactory.get("jpaReaderJob")
            .start(jpaPagingReaderStep())
            .build();
    }

    @Bean
    public Step jpaPagingReaderStep() throws Exception {
        return stepBuilderFactory.get("jpaPagingReader")
            .<JpaCharacter, JpaCharacter>chunk(3)
            .reader(jpaPagingReaderWithQueryString(null))
//            .reader(jpaPagingReaderWithQueryProvider(null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<JpaCharacter> jpaPagingReaderWithQueryString(@Value("#{jobParameters['emailMask'] ?: '%example%'}") String emailMask) {
        return new JpaPagingItemReaderBuilder<JpaCharacter>()
            .name("jpaPagedReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(3)
            .queryString("select c from JpaCharacter c where c.email like :emailMask")
            .parameterValues(Map.of("emailMask", emailMask))
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<JpaCharacter> jpaPagingReaderWithQueryProvider(@Value("#{jobParameters['emailMask'] ?: '%example%'}") String emailMask) {
        var queryProvider = new CharacterQueryProvider(emailMask);
        return new JpaPagingItemReaderBuilder<JpaCharacter>()
            .name("jpaPagedReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(3)
            .queryProvider(queryProvider)
            .build();
    }

    private static class CharacterQueryProvider extends AbstractJpaQueryProvider {
        private final String emailMask;

        public CharacterQueryProvider(String emailMask) {
            this.emailMask = emailMask;
        }

        @Override
        public Query createQuery() {
            var manager = getEntityManager();
            var builder = manager.getCriteriaBuilder();
            var query = builder.createQuery(JpaCharacter.class);
            var character = query.from(JpaCharacter.class);
            query.select(character).where(builder.like(character.get("email"), emailMask));

            return manager.createQuery(query);
        }

        @Override
        public void afterPropertiesSet() {
        }
    }
}


