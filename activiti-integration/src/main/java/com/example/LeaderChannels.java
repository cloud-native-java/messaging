package com.example;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface LeaderChannels {

	@Output
	MessageChannel leaderRequests();

	@Input
	MessageChannel leaderReplies();
}
