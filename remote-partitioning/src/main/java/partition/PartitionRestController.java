package partition;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@Profile(Profiles.LEADER_PROFILE)
class PartitionRestController {

	private final Job job;
	private final JobLauncher jobLauncher;
	private final JdbcOperations jdbcOperations;

	@Autowired
	public PartitionRestController(
			JdbcOperations jdbcOperations,
			JobConfiguration jobConfiguration,
			JobLauncher jobLauncher) throws Exception {
		this.jobLauncher = jobLauncher;
		this.jdbcOperations = jdbcOperations;
		this.job = jobConfiguration.job(null, null);
	}

	@RequestMapping(value = "/migrate",
			method = {RequestMethod.POST, RequestMethod.GET})
	ResponseEntity<?> start() throws JobExecutionException {
		JobExecution execution = this.jobLauncher.run(this.job,
				new JobParametersBuilder()
						.addDate("date", new Date())
						.toJobParameters());
		return ResponseEntity.ok(execution.getExitStatus());
	}

	@GetMapping("/status")
	ResponseEntity<?> status() {
		Map<String, Number> status = new HashMap<>();
		status.put("people.count", this.jdbcOperations.queryForObject("select count(*) from PEOPLE", Number.class));
		status.put("new_people.count", this.jdbcOperations.queryForObject("select count(*) from NEW_PEOPLE", Number.class));
		return ResponseEntity.ok(status);
	}
}
