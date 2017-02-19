package eda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.file.Files;
import org.springframework.messaging.MessageChannel;

import java.io.File;

@Configuration
public class IntegrationConfiguration {

	private final Log log = LogFactory.getLog(getClass());

	@Bean
	IntegrationFlow etlFlow(
		@Value("${input-directory:${HOME}/Desktop/in}") File dir) {

		return IntegrationFlows
		// <1>
			.from(Files.inboundAdapter(dir).autoCreateDirectory(true),
				consumer -> consumer.poller(spec -> spec.fixedRate(1000)))
			// <2>
			.handle(File.class, (file, headers) -> {
				log.info("we noticed a new file, " + file);
				return file;
			})
			// <3>
			.routeToRecipients(
				spec -> spec.recipient(csv(), msg -> hasExt(msg.getPayload(), ".csv"))
					.recipient(txt(), msg -> hasExt(msg.getPayload(), ".txt"))).get();
	}

	private boolean hasExt(Object f, String ext) {
		File file = File.class.cast(f);
		return file.getName().toLowerCase().endsWith(ext.toLowerCase());
	}

	// <4>
	@Bean
	MessageChannel txt() {
		return MessageChannels.direct().get();
	}

	// <5>
	@Bean
	MessageChannel csv() {
		return MessageChannels.direct().get();
	}

	// <6>
	@Bean
	IntegrationFlow txtFlow() {
		return IntegrationFlows.from(txt()).handle(File.class, (f, h) -> {
			log.info("file is .txt!");
			return null;
		}).get();
	}

	// <7>
	@Bean
	IntegrationFlow csvFlow() {
		return IntegrationFlows.from(csv()).handle(File.class, (f, h) -> {
			log.info("file is .csv!");
			return null;
		}).get();
	}
}
