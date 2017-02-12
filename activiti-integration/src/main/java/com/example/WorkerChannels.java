package com.example;


import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface WorkerChannels {

	@Input
	MessageChannel workerRequests();

	@Output
	MessageChannel workerReplies();
}
