package stream.consumer.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import stream.consumer.ConsumerChannels;

@SpringBootApplication
@EnableBinding(ConsumerChannels.class)
public class StreamConsumer {

	public static void main(String args[]) {
		SpringApplication.run(StreamConsumer.class, args);
	}
}

@Component
class GreetingProcessor {

	private Log log = LogFactory.getLog(getClass());

	@StreamListener(ConsumerChannels.DIRECTED)
	public void onNewDirectedGreetings(String greeting) {
		this.onNewGreeting(ConsumerChannels.DIRECTED, greeting);
	}

	@StreamListener(ConsumerChannels.BROADCASTS)
	public void onNewBroadcastGreeting(String greeting) {
		this.onNewGreeting(ConsumerChannels.BROADCASTS, greeting);
	}

	private void onNewGreeting(String prefix, String greeting) {
		log.info("greeting received in @StreamListener (" + prefix + "): "
				+ greeting);
	}
}