package integration;

import cnj.CloudFoundryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RemotePartitioningIT.Config.class)
public class RemotePartitioningIT {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private CloudFoundryService cloudFoundryService;

	private String mysql = "batch-mysql", rmq = "batch-rmq";

	private File leader, worker;

	@Before
	public void before() throws Throwable {
		Stream.of("p-mysql 100mb " + mysql, "cloudamqp lemur " + rmq)
				.map(x -> x.split(" "))
				.forEach(t -> this.cloudFoundryService.createServiceIfMissing(t[0], t[1], t[2]));
		File projectFolder = new File(new File("."), "../remote-partitioning");
		this.leader = new File(projectFolder, "manifest-leader.yml");
		this.worker = new File(projectFolder, "manifest-worker.yml");
		Assert.assertTrue("the manifest files  " + this.leader.getAbsolutePath() +
						"and " + this.worker.getAbsolutePath() + " must exist",
				leader.exists() && worker.exists());
		Stream.of(this.leader, this.worker).parallel()
				.forEach(f -> this.cloudFoundryService.pushApplicationUsingManifest(f));
	}

	@After
	public void after() throws Throwable {
		Stream.of(this.leader, this.worker)
				.filter(Objects::nonNull)
				.forEach(this.cloudFoundryService::destroyApplicationUsingManifest);
	}

	@Test
	public void partitionedJob() {

		String leaderUrl = cloudFoundryService.urlForApplication("partition-leader");

		String migrationJobUrl = leaderUrl + "/migrate";
		ResponseEntity<Map<String, String>> jsonResponse =
				this.restTemplate.exchange(
						migrationJobUrl, GET, null,
						new ParameterizedTypeReference<Map<String, String>>() {
						});
		assertTrue(jsonResponse.getBody().get("exitCode")
				.equalsIgnoreCase("completed"));
		assertTrue(jsonResponse.getBody().get("running")
				.equalsIgnoreCase("false"));
		assertTrue("the job should have completed successfully.",
				jsonResponse.getStatusCode().is2xxSuccessful());
		Map<String, Number> status =
				this.restTemplate
						.exchange(leaderUrl + "/status", GET, null,
								new ParameterizedTypeReference<Map<String, Number>>() {
								})
						.getBody();

		Assert.assertEquals("there should be an identical number" +
						" of records in the source and destination table",
				status.get("people.count"), status.get("new_people.count"));
	}

	@SpringBootApplication
	public static class Config {

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}
}
