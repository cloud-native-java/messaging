package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;


@Configuration
@Profile(Profiles.WORKER)
class WorkerConfiguration {

	@Bean
	IntegrationFlow requestsFlow(WorkerChannels channels) {

		Log log = LogFactory.getLog(getClass());

		// <1>
		return IntegrationFlows
				.from(channels.workerRequests())
				.handle((GenericHandler<String>) (executionId, headers) -> {
					// <2>
					headers
						.entrySet()
							.forEach(e -> log.info(e.getKey() + '=' + e.getValue()));
					log.info("sending executionId (" + executionId + ") to workerReplies.");
					return executionId;
				})
				.channel(channels.workerReplies()) // <3>
				.get();
	}
}
