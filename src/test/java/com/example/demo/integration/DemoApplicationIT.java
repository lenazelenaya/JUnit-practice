package com.example.demo.integration;

import com.example.demo.controller.ToDoController;

import com.example.demo.service.ToDoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationIT {

	@Autowired
	private ToDoController toDoController;
	@Autowired
	private ToDoService toDoService;

	@Test
	void contextLoads() throws Exception {
		if (toDoController == null) {
			throw new Exception("ToDoController is null");
		}

		if(toDoService == null){
			throw new Exception("ToDoService is null");
		}
	}

}
