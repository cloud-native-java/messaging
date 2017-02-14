package edabatch;

import edabatch.email.EmailValidationService;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
			JdbcTemplate template, ItemReader<Contact> fileReader,
			ItemProcessor<Contact, Contact> emailProcessor, ItemWriter<Contact> jdbcWriter) {

		Step setup = stepBuilderFactory.get("clean-contact-table")
				.tasklet((contribution, chunkContext) -> {
					template.update("delete from CONTACT");
					return RepeatStatus.FINISHED;
				}).build();

		Step fileToJdbc = stepBuilderFactory.get("file-to-jdbc-fileToJdbc")
				.<Contact, Contact>chunk(5)
				// <1>
				.reader(fileReader).processor(emailProcessor).writer(jdbcWriter).faultTolerant()
				.skip(InvalidEmailException.class)
				// <2>
				.skipPolicy((Throwable t, int skipCount) -> {
					LogFactory.getLog(getClass()).info("skipping ");
					return t.getClass().isAssignableFrom(InvalidEmailException.class);
				}).retry(HttpStatusCodeException.class) // <3>
				.retryLimit(2).build();

		return jobBuilderFactory.get("etl") // <4>
				.start(setup).next(fileToJdbc).build();
	}

	// <5>
	@Bean
	@StepScope
	FlatFileItemReader<Contact> fileReader(
			@Value("file://#{jobParameters['file']}") Resource pathToFile) throws Exception {
		return new FlatFileItemReaderBuilder<Contact>().name("file-reader")
				.resource(pathToFile).targetType(Contact.class).delimited()
				.names("fullName,email".split(",")).build();
	}

	// <6>
	@Bean
	ItemProcessor<Contact, Contact> validatingProcessor(
			EmailValidationService emailValidationService) {
		return item -> {
			boolean valid = emailValidationService.isEmailValid(item.getEmail());
			item.setValidEmail(valid);
			if (!valid)
				throw new InvalidEmailException(item.getEmail());
			return item;
		};
	}

	// <7>
	@Bean
	JdbcBatchItemWriter<Contact> jdbcWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Contact>()
				.dataSource(dataSource)
				.beanMapped()
				.sql(
						"insert into CONTACT( full_name, email, valid_email ) values ( :fullName, :email, :validEmail )")
				.build();
	}

	public static class InvalidEmailException extends Exception {
		public InvalidEmailException(String email) {
			super(String.format("the email %s isn't valid", email));
		}
	}
}