package stream.producer.channels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stream.producer.ProducerChannels;

@SpringBootApplication
@EnableBinding(ProducerChannels.class)
// <1>
public class StreamProducer {

	public static void main(String args[]) {
		SpringApplication.run(StreamProducer.class, args);
	}
}

@RestController
class GreetingProducer {

	private final MessageChannel broadcast, direct;

	// <2>
	@Autowired
	GreetingProducer(ProducerChannels channels) {
		this.broadcast = channels.broadcastGreetings();
		this.direct = channels.directGreetings();
	}

	@RequestMapping("/hi/{name}")
	ResponseEntity<String> hi(@PathVariable String name) {
		String message = "Hello, " + name + "!";

		// <3>
		this.direct.send(MessageBuilder.withPayload("Direct: " + message).build());

		this.broadcast.send(MessageBuilder.withPayload("Broadcast: " + message)
			.build());
		return ResponseEntity.ok(message);
	}
}