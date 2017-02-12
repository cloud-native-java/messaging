package partition;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collections;

@Configuration
class WorkerStepConfiguration {

	// <1>
	@Value("${partition.chunk-size}")
	private int chunk;

	// <2>
	@Bean
	@StepScope
	JdbcPagingItemReader<Person> reader(
			DataSource dataSource,
			@Value("#{stepExecutionContext['minValue']}") Long min,
			@Value("#{stepExecutionContext['maxValue']}") Long max) {

		MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
		queryProvider.setSelectClause("id as id, email as email, age as age, first_name as firstName");
		queryProvider.setFromClause("from PEOPLE");
		queryProvider.setWhereClause("where id >= " + min + " and id <= " + max);
		queryProvider.setSortKeys(Collections.singletonMap("id", Order.ASCENDING));

		JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<>();
		reader.setDataSource(dataSource);
		reader.setFetchSize(this.chunk);
		reader.setQueryProvider(queryProvider);
		reader.setRowMapper((rs, i) -> new Person(
				rs.getInt("id"),
				rs.getInt("age"),
				rs.getString("firstName"),
				rs.getString("email")));
		return reader;
	}

	// <3>
	@Bean
	JdbcBatchItemWriter<Person> writer(DataSource ds) {
		return new JdbcBatchItemWriterBuilder<Person>()
				.beanMapped()
				.dataSource(ds)
				.sql("INSERT INTO NEW_PEOPLE(age,first_name,email) VALUES(:age, :firstName, :email )")
				.build();
	}

	// <4>
	@Bean
	Step workerStep(StepBuilderFactory sbf) {
		return sbf
				.get("workerStep")
				.<Person, Person>chunk(this.chunk)
				.reader(reader(null, null, null))
				.writer(writer(null))
				.build();
	}
}
