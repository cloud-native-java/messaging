package stream.producer.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import stream.producer.ProducerChannels;

// <3>
@MessagingGateway
interface GreetingGateway {

 @Gateway(requestChannel = ProducerChannels.BROADCAST)
 void broadcastGreet(String msg);

 @Gateway(requestChannel = ProducerChannels.DIRECT)
 void directGreet(String msg);
}

@SpringBootApplication
@EnableBinding(ProducerChannels.class)
// <1>
@IntegrationComponentScan
// <2>
public class StreamProducer {

 public static void main(String args[]) {
  SpringApplication.run(StreamProducer.class, args);
 }
}

@RestController
class GreetingProducer {

 private final GreetingGateway gateway;

 // <4>
 @Autowired
 GreetingProducer(GreetingGateway gateway) {
  this.gateway = gateway;
 }

 @RequestMapping(method = RequestMethod.GET, value = "/hi/{name}")
 ResponseEntity<?> hi(@PathVariable String name) {
  String message = "Hello, " + name + "!";
  this.gateway.directGreet("Direct: " + message);
  this.gateway.broadcastGreet("Broadcast: " + message);
  return ResponseEntity.ok(message);
 }
}