package com.example;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@Profile(Profiles.LEADER)
@RestController
class ProcessRestController {

	private final ProcessEngine processEngine;

	@Autowired
	ProcessRestController(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	@GetMapping("/start")
	Map<String, String> launch() {
		ProcessInstance pi = this.processEngine.getRuntimeService()
				.startProcessInstanceByKey("asyncProcess");
		return Collections.singletonMap("processInstanceId", pi .getProcessInstanceId());
	}
}
