package integration;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest(classes = ActivitiIT.Config.class)
@RunWith(SpringRunner.class)
public class ActivitiIT {

	private final RestTemplate restTemplate = new RestTemplateBuilder()
			.basicAuthorization("operator", "operator")
			.build();

	@Autowired
	private RetryTemplate retryTemplate;

	@Autowired
	private CloudFoundryService cloudFoundryService;

	private File leaderManifest, workerManifest;

	private Log log = LogFactory.getLog(getClass());

	@Before
	public void before() throws Throwable {

		// deploy the activiti application, twice, to CF as a leader and a worker node.
		String mysql = "activiti-mysql", rmq = "activiti-rabbitmq",
				leader = "activiti-leader", worker = "activiti-worker";

		File projectFolder = new File(new File("."), "../activiti-integration");
		this.leaderManifest = new File(projectFolder, "manifest-leader.yml");
		this.workerManifest = new File(projectFolder, "manifest-worker.yml");

		log.debug("activiti folder: "
				+ projectFolder.getAbsolutePath());

		// reset
		Runnable apps = () -> Stream.of(leader, worker)
				.parallel().forEach(app -> this.cloudFoundryService.destroyApplicationIfExists(app));

		Runnable routes = () -> this.cloudFoundryService.destroyOrphanedRoutes();

		Runnable services = () -> Stream.of(mysql, rmq)
				.parallel().forEach(svc -> this.cloudFoundryService.destroyServiceIfExists(svc));

		// apps must be reset first!
		Stream.of(apps, routes, services).forEach(Runnable::run);

		// create services required
		Stream.of("p-mysql 100mb " + mysql, "cloudamqp lemur " + rmq)
				.map(x -> x.split(" "))
				.parallel()
				.forEach(t -> this.cloudFoundryService.createService(t[0], t[1], t[2]));

		// deploy
		Arrays.asList(leaderManifest, workerManifest)
				.parallelStream()
				.forEach(mf -> this.cloudFoundryService.pushApplicationUsingManifest(mf));
	}

	@After
	public void after() throws Throwable {
		Stream.of(this.leaderManifest, workerManifest)
				.forEach(this.cloudFoundryService::destroyApplicationUsingManifest);
	}

	@Test
	public void testDistributedWorkflows() throws Throwable {

		String url = this.cloudFoundryService
				.urlForApplication("activiti-leader");

		ResponseEntity<Map<String, String>> entity =
				restTemplate.exchange(url + "/start",
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<Map<String, String>>() {
						});
		Assert.assertEquals(entity.getStatusCode(), HttpStatus.OK);
		String pid = entity.getBody().get("processInstanceId");
		log.info("process instance ID: " + pid);

		RetryCallback<Boolean, RuntimeException> rt = retryContext -> {
			String pidUrl = url + "/history/historic-process-instances/" + pid;
			log.info("calling the " + url + " endpoint to confirm the process ran and completed successfully.");
			Map<String, Object> instanceInformation = restTemplate.exchange(
					pidUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<Map<String, Object>>() {
					})
					.getBody();

			if (instanceInformation.get("endTime") != null) {
				log.info("endTime was not null..");
				return true;
			}
			log.info("endTime was null..");
			throw new RuntimeException("the endTime attribute was null");
		};
		Boolean endTimeNull = retryTemplate.execute(rt, retryContext -> false);
		Assert.assertTrue("the endTime attribute should eventually return null",
				endTimeNull);
	}

	@SpringBootApplication
	public static class Config {
	}
}