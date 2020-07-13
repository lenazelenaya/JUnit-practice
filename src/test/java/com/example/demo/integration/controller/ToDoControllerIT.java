package com.example.demo.integration.controller;

import com.example.demo.controller.ToDoController;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToRequestMapper;
import com.example.demo.exception.NoArgsException;
import com.example.demo.exception.ToDoNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Collections;

import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.model.ToDoEntity;
import com.example.demo.service.ToDoService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ToDoController.class)
@ActiveProfiles(profiles = "test")
class ToDoControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ToDoService toDoService;

	@Test
	void whenGetAll_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		Long testId = 1L;
		when(toDoService.getAll()).thenReturn(
				Collections.singletonList(
						ToDoEntityToResponseMapper.map(new ToDoEntity(testId, testText))
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
			.andExpect(jsonPath("$[0].id").value(testId))
			.andExpect(jsonPath("$[0].completedAt").doesNotExist());
	}

	@Test
	void whenSave_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		Long testId = 1L;
		var newEntity = new ToDoEntity(testText);
		var tempEntity = new ToDoEntity(testId, testText);
		var newRequest = ToDoEntityToRequestMapper.map(newEntity);
		var newResponse = ToDoEntityToResponseMapper.map(tempEntity);

		when(toDoService.upsert(ArgumentMatchers.any(ToDoSaveRequest.class)))
				.thenReturn(newResponse);

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

	@Test
	void whenComplete_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		Long testId = 1L;

		var newEntity = new ToDoEntity(testId, testText);
		var newResponse = ToDoEntityToResponseMapper.map(newEntity);

		when(toDoService.completeToDo(testId)).thenReturn(newResponse);

		this.mockMvc
				.perform(put("/todos/{id}/complete", testId))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isNotEmpty())
				.andExpect(jsonPath("$.text").value(testText))
				.andExpect(jsonPath("$.id").value(testId))
				.andExpect(jsonPath("$.completedAt").value(newEntity.getCompletedAt()));
	}

	@Test
	void whenGetText_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		Long testId = 1L;

		when(toDoService.getTextById(testId)).thenReturn(testText);

		this.mockMvc
				.perform(get("/todos/{id}/text", testId))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/plain;charset=UTF-8"))
				.andExpect(jsonPath("$").isString())
				.andExpect(jsonPath("$").value(testText));
	}

	@Test
	void whenGetTextWithNullId_thenBadRequestError() throws Exception {
		Long id = null;

		when(toDoService.getTextById(id)).thenThrow(new NoArgsException());

		this.mockMvc
				.perform(get("/todos/{id}/text", id))
				.andExpect(status().isBadRequest());
	}

	@Test
	void whenCompleteWithNullId_thenBadRequestError() throws Exception {
		Long id = null;
		when(toDoService.completeToDo(id)).thenThrow(new NoArgsException());

		this.mockMvc
				.perform(get("/todos/{id}/complete", id))
				.andExpect(status().isBadRequest());
	}

	@Test
	void whenGetTextWithFailId_thenBadRequestError() throws Exception {
		Long id = 42L;
		when(toDoService.getTextById(ArgumentMatchers.any(Long.class)))
				.thenThrow(new ToDoNotFoundException(id));

		this.mockMvc
				.perform(get("/todos/{id}/text", id))
				.andExpect(status().isNotFound());
	}
}
