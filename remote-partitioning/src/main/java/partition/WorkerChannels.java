package partition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableBinding(WorkerChannels.Worker.class)
@Profile(Profiles.WORKER_PROFILE)
class WorkerChannels {

	private final Worker worker;

	@Autowired
	public WorkerChannels(Worker worker) {
		this.worker = worker;
	}

	MessageChannel workerRequestsChannels() {
		return this.worker.workerRequests();
	}

	MessageChannel workerRepliesChannels() {
		return this.worker.workerReplies();
	}

	public interface Worker {

		@Input
		MessageChannel workerRequests();

		@Output
		MessageChannel workerReplies();
	}
}
