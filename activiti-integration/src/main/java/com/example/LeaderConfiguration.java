package com.example;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@Configuration
@Profile(Profiles.LEADER)
class LeaderConfiguration {

	// <1>
	@Bean
	ActivityBehavior gateway(LeaderChannels channels) {
		return new ReceiveTaskActivityBehavior() {

			@Override
			public void execute(ActivityExecution execution) throws Exception {

				Message<?> executionMessage = MessageBuilder
						.withPayload(execution.getId())
						.build();

				channels.leaderRequests().send(executionMessage);
			}
		};
	}

	// <2>
	@Bean
	IntegrationFlow repliesFlow(LeaderChannels channels,
	                            ProcessEngine engine) {
		return IntegrationFlows
				.from(channels.leaderReplies())
				.handle(String.class, (executionId, map) -> {
					engine.getRuntimeService().signal(executionId);
					return null;
				})
				.get();
	}
}
