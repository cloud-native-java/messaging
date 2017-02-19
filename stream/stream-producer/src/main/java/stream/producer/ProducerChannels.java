package stream.producer;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface ProducerChannels {

 // <1>
 String DIRECT = "directGreetings";

 String BROADCAST = "broadcastGreetings";

 @Output(DIRECT)
 // <2>
 MessageChannel directGreetings();

 @Output(BROADCAST)
 MessageChannel broadcastGreetings();
}
