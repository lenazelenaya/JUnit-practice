package com.example.demo.integration;

import com.example.demo.controller.ToDoController;

import com.example.demo.dto.mapper.ToDoEntityToRequestMapper;
import com.example.demo.model.ToDoEntity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.example.demo.service.ToDoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationIT {

	@Autowired
	private MockMvc mockMvc;
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

	@Test
	public void saveEntity() throws Exception{
		//arange
		var text = "Test text";
		var newEntity = ToDoEntityToRequestMapper.map(new ToDoEntity(text));

		//act
		var result = toDoService.upsert(newEntity);

		//assert
		assertThat(result != null);
		assertThat(result.text.equals(text));
	}

	@Test
	public void saveEntityWithNullId() throws Exception{
		//arange
		var text = "Test text";
		var newEntity = ToDoEntityToRequestMapper.map(new ToDoEntity(null, text));

		//act
		var result = toDoService.upsert(newEntity);

		//assert
		assertThat(result != null);
		assertThat(result.text.equals(text));
	}

	@Test
	public void completeEntity() throws Exception{
		//arange
		var text = "Test text complete";
		var newEntity = ToDoEntityToRequestMapper.map(new ToDoEntity(text));
		var response = toDoService.upsert(newEntity);

		//act
		var result = toDoService.completeToDo(response.id);

		//assert
		assertThat(result != null);
		assertThat(result.text.equals(text));
		assertThat(result.completedAt != null);
	}

	@Test
	public void completeEntityWithController() throws Exception{
		//arange
		var text = "Test text";
		var newEntity = new ToDoEntity(text).completeNow();
		var newResponse = toDoService.upsert(ToDoEntityToRequestMapper.map(newEntity));
		var id = newResponse.id;

		mockMvc.perform(put("/todos/{id}/complete", id))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isNotEmpty())
				.andExpect(jsonPath("$.text").value(text))
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.completedAt").exists());
	}
}
