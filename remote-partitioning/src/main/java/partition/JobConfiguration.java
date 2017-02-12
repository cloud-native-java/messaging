package partition;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(Profiles.LEADER_PROFILE) // <1>
class JobConfiguration {

	@Bean
	Job job(JobBuilderFactory jbf,
	        LeaderStepConfiguration lsc) {
		return jbf
				.get("job")
				.incrementer(new RunIdIncrementer())
				.start(lsc.stagingStep(null, null)) // <2>
				.next(lsc.partitionStep(null, null, null, null)) // <3>
				.build();
	}
}
