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

import javax.sql.DataSource;

import dev.softwaregarden.spring.batch.first_steps.readers.util.JdbcCharacter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;

@EnableBatchProcessing
@Configuration
public class JdbcReadersExample {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public JdbcReadersExample(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job jdbcReaderJob() throws Exception {
        return jobBuilderFactory.get("jdbcReadersJob")
            .start(jdbcCursorReaderStep())
            .next(jdbcPagingReaderStep())
            .build();
    }

    @Bean
    public Step jdbcCursorReaderStep() throws Exception {
        return stepBuilderFactory.get("jdbcCursorReader")
            .<JdbcCharacter, JdbcCharacter>chunk(3)
            .reader(jdbcCursorReader())
            .writer(System.out::println)
            .build();
    }

    @Bean
    public JdbcCursorItemReader<JdbcCharacter> jdbcCursorReader() {

        return new JdbcCursorItemReaderBuilder<JdbcCharacter>()
            .name("jdbcCursorReader")
            .dataSource(dataSource)
            .sql("SELECT id, first_name, last_name, email FROM characters WHERE email like ?")
            .preparedStatementSetter(emailMaskSetter(null))
            .rowMapper((rs, i) -> new JdbcCharacter(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email")))
            .build();
    }

    @Bean
    @StepScope
    public PreparedStatementSetter emailMaskSetter(@Value("#{jobParameters['emailMask'] ?: '%example%'}") String emailMask) {
        // SqlParameterValue can also be used
        return new ArgumentPreparedStatementSetter(new Object[]{emailMask});
    }

    @Bean
    public Step jdbcPagingReaderStep() throws Exception {
        return stepBuilderFactory.get("jdbcPagingReader")
            .<JdbcCharacter, JdbcCharacter>chunk(3)
            .reader(jdbcPagingReader(null, null))
            .writer(System.out::println)
            .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<JdbcCharacter> jdbcPagingReader(PagingQueryProvider queryProvider,
                                                                @Value("#{jobParameters['emailMask'] ?: '%example%'}") String emailMask) {
        return new JdbcPagingItemReaderBuilder<JdbcCharacter>()
            .name("jdbcPagedReader")
            .dataSource(dataSource)
            .beanRowMapper(JdbcCharacter.class)
            .pageSize(2)
            .queryProvider(queryProvider)
            .parameterValues(Map.of("emailMask", emailMask))
            .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean pagingQueryProviderFactoryBean(DataSource dataSource) {
        var factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT id, first_name, last_name, email");
        factoryBean.setFromClause("from characters");
        factoryBean.setWhereClause("where email like :emailMask");
        factoryBean.setSortKeys(Map.of("id", Order.DESCENDING));
        return factoryBean;
    }
}


