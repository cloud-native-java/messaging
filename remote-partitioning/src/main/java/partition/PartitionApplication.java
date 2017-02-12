package partition;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@EnableBatchProcessing // <1>
@IntegrationComponentScan
@SpringBootApplication
public class PartitionApplication {

	public static void main(String args[]) {
		SpringApplication.run(PartitionApplication.class, args);
	}

	// <2>
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	PollerMetadata defaultPoller() {
		return Pollers.fixedRate(10, TimeUnit.SECONDS).get();
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}