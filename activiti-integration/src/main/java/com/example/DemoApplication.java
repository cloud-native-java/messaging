package com.example;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableAutoConfiguration
public class DemoApplication {

	@Configuration
	@Profile(Profiles.LEADER)
	@EnableBinding(LeaderChannels.class)
	public static class Leader {

		@Bean
		InitializingBean init(IdentityService identityService) {
			return () -> {

				String usersGroup = "users";
				if (0 == identityService.createGroupQuery().groupId(usersGroup).count()) {
					Group group = identityService.newGroup(usersGroup);
					group.setName(usersGroup);
					group.setType("security-role");
					identityService.saveGroup(group);
				}

				String adminUser = "operator";
				if (0 == identityService.createUserQuery().userId(adminUser).count()) {
					User admin = identityService.newUser(adminUser);
					admin.setPassword(adminUser);
					identityService.saveUser(admin);
				}
			};
		}
	}

	@Configuration
	@Profile(Profiles.WORKER)
	@EnableBinding(WorkerChannels.class)
	public static class Worker {
	}


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}


