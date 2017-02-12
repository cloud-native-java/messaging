package partition;

import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.messaging.MessageChannel;

@Configuration
@Profile(Profiles.WORKER_PROFILE)  // <1>
class WorkerConfiguration {

	// <2>
	@Bean
	StepLocator stepLocator() {
		return new BeanFactoryStepLocator();
	}

	// <3>
	@Bean
	StepExecutionRequestHandler stepExecutionRequestHandler(
			JobExplorer explorer, StepLocator stepLocator) {
		StepExecutionRequestHandler handler = new StepExecutionRequestHandler();
		handler.setStepLocator(stepLocator);
		handler.setJobExplorer(explorer);
		return handler;
	}

	// <4>
	@Bean
	IntegrationFlow stepExecutionRequestHandlerFlow(
			WorkerChannels channels,
			StepExecutionRequestHandler handler) {

		MessageChannel channel = channels.workerRequestsChannels();
		GenericHandler<StepExecutionRequest> executionHandler =
				(payload, headers) -> handler.handle(payload);

		return IntegrationFlows.from(channel)
				.handle(StepExecutionRequest.class, executionHandler)
				.channel(channels.workerRepliesChannels())
				.get();
	}
}