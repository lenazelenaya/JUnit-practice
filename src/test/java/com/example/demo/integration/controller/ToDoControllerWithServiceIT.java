package com.example.demo.integration.controller;

import com.example.demo.controller.ToDoController;
import com.example.demo.dto.mapper.ToDoEntityToRequestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Collections;

import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import com.example.demo.service.ToDoService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ToDoController.class)
@ActiveProfiles(profiles = "demo")
@Import(ToDoService.class)
class ToDoControllerWithServiceIT {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ToDoRepository toDoRepository;

	@Test
	void whenGetAll_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		when(toDoRepository.findAll()).thenReturn(
				Collections.singletonList(
						new ToDoEntity(1L, testText)
				)
		);
		
		this.mockMvc
			.perform(get("/todos"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].text").value(testText))
			.andExpect(jsonPath("$[0].id").isNumber())
			.andExpect(jsonPath("$[0].completedAt").doesNotExist());
	}

	//Does not work
	@Test
	void whenSave_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		Long testId = 1L;
		var newEntity = new ToDoEntity(testText);
		var newRequest = ToDoEntityToRequestMapper.map(newEntity);

		when(toDoRepository.save(newEntity)).thenReturn(new ToDoEntity(testId, testText));

		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newRequest)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isNotEmpty())
				.andExpect(jsonPath("$.text").value(testText))
				.andExpect(jsonPath("$.id").value(testId))
				.andExpect(jsonPath("$.completedAt").doesNotExist());
	}

	//Does not work
	@Test
	void whenGetText_thenReturnValidResponse() throws Exception {
		Long testId = 1L;
		String testText = "Test text";

		when(toDoRepository.getOne(testId)).thenReturn(new ToDoEntity(testId, testText));

		this.mockMvc
				.perform(get("/todos/{id}", testId))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isNotEmpty())
				.andExpect(jsonPath("$.text").value(testText))
				.andExpect(jsonPath("$.id").value(testId))
				.andExpect(jsonPath("$.completedAt").doesNotExist());
	}
}
