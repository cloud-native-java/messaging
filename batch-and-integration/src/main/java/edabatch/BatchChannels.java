package edabatch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
class BatchChannels {

	@Bean
	MessageChannel invalid() {
		return MessageChannels.direct().get();
	}

	@Bean
	MessageChannel completed() {
		return MessageChannels.direct().get();
	}

}
