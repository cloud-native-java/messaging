package edabatch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.io.File;

import static org.springframework.integration.file.FileHeaders.ORIGINAL_FILE;

@Configuration
class EtlFlowConfiguration {

	// <1>
	@Bean
	IntegrationFlow etlFlow(@Value("${input-directory:${HOME}/Desktop/in}") File directory,
			BatchChannels c, JobLauncher launcher, Job job) {

		// @formatter:off
		return
			IntegrationFlows
				.from(
						Files.inboundAdapter(directory).autoCreateDirectory(true),
						cs -> cs.poller(p -> p.fixedRate(1000)))
				.handle(File.class, (file, headers) -> {

					String absolutePath = file.getAbsolutePath();

					JobParameters params = new JobParametersBuilder()
							.addString("file", absolutePath)
							.toJobParameters();

					return MessageBuilder
						.withPayload(new JobLaunchRequest(job, params))
						.setHeader(ORIGINAL_FILE, absolutePath)
						.copyHeadersIfAbsent(headers)
						.build();
				})
				.handle(new JobLaunchingGateway(launcher))
				.routeToRecipients(
					spec -> spec
						.recipient(c.invalid(), this::notFinished)
						.recipient(c.completed(), this::finished))
				.get();

		// @formatter:on
	}

	private boolean finished(Message<?> msg) {
		Object payload = msg.getPayload();
		return JobExecution.class.cast(payload).getExitStatus().equals(ExitStatus.COMPLETED);
	}

	private boolean notFinished(Message<?> msg) {
		return !this.finished(msg);
	}

}
