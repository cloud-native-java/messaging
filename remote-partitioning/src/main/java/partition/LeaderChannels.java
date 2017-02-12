package partition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableBinding(LeaderChannels.Leader.class)
class LeaderChannels {

	private final Leader leader;

	@Autowired
	public LeaderChannels(Leader leader) {
		this.leader = leader;
	}

	@Bean
	public QueueChannel leaderRepliesAggregatedChannel() {
		return MessageChannels.queue().get();
	}

	public MessageChannel leaderRequestsChannel() {
		return leader.leaderRequests();
	}

	public interface Leader {

		@Output
		MessageChannel leaderRequests();
	}
}
