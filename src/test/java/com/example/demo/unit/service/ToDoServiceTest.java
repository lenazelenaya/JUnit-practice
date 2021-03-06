package com.example.demo.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static org.mockito.Mockito.*;
//import static org.mockito.AdditionalAnswers.*;
import com.example.demo.exception.NoArgsException;
import com.example.demo.service.ToDoService;
import org.mockito.ArgumentMatchers;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;

class ToDoServiceTest {

	private ToDoRepository toDoRepository;

	private ToDoService toDoService;

	//executes before each test defined below
	@BeforeEach
	void setUp() {
		this.toDoRepository = mock(ToDoRepository.class);
		toDoService = new ToDoService(toDoRepository);
	}

	@Test
	void whenGetAll_thenReturnAll() {
		//mock
		var testToDos = new ArrayList<ToDoEntity>();
		testToDos.add(new ToDoEntity(0L, "Test 1"));
		var toDo = new ToDoEntity(1L, "Test 2");
		toDo.completeNow();
		testToDos.add(toDo);
		when(toDoRepository.findAll()).thenReturn(testToDos);

		//call
		var todos = toDoService.getAll();

		//validate
		assertEquals(todos.size(), testToDos.size());
		for (int i = 0; i < todos.size(); i++) {
			assertThat(todos.get(i), samePropertyValuesAs(
				ToDoEntityToResponseMapper.map(testToDos.get(i))
			));
		}
	}

	@Test
	void whenUpsertWithId_thenReturnUpdated() throws ToDoNotFoundException {
		//mock
		var expectedToDo = new ToDoEntity(0L, "New Item");
		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if (id.equals(expectedToDo.getId())) {
				return Optional.of(expectedToDo);
			} else {
				return Optional.empty();
			}
		});
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
			ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
			Long id = arg.getId();
			if (id != null) {
				if (!id.equals(expectedToDo.getId()))
					return new ToDoEntity(id, arg.getText());
				expectedToDo.setText(arg.getText());
				return expectedToDo; //return valid result only if we get valid id
			} else {
				return new ToDoEntity(40158L, arg.getText());
			}
		});
		
		//call
		var toDoSaveRequest = new ToDoSaveRequest();
		toDoSaveRequest.id = expectedToDo.getId();
		toDoSaveRequest.text = "Updated Item";
		var todo = toDoService.upsert(toDoSaveRequest);

		//validate
		assertSame(todo.id, toDoSaveRequest.id);
		assertEquals(todo.text, toDoSaveRequest.text);
	}
	
	@Test
	void whenUpsertNoId_thenReturnNew() throws ToDoNotFoundException {
		//mock
		var newId = 0L;
		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if (id == newId) {
				return Optional.empty();
			} else {
				return Optional.of(new ToDoEntity(newId, "Wrong ToDo"));
			}
		});
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
			ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
			Long id = arg.getId();
			if (id == null)
				return new ToDoEntity(newId, arg.getText());
			else 
				return new ToDoEntity();
		});

		//call
		var toDoDto = new ToDoSaveRequest();
		toDoDto.text = "Created Item";
		var result = toDoService.upsert(toDoDto);

		//validate
		assertEquals((long) result.id, newId);
		assertEquals(result.text, toDoDto.text);
	}

	@Test
	void whenComplete_thenReturnWithCompletedAt() throws ToDoNotFoundException, NoArgsException {
		var startTime = ZonedDateTime.now(ZoneOffset.UTC);
		//mock
		var todo = new ToDoEntity(0L, "Test 1");
		when(toDoRepository.findById(anyLong())).thenReturn(Optional.of(todo));
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
			ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
			Long id = arg.getId();
			if (id.equals(todo.getId())) {
				return todo;
			} else {
				return new ToDoEntity();
			}
		});

		//call
		var result = toDoService.completeToDo(todo.getId());

		//validate
		assertSame(result.id, todo.getId());
		assertEquals(result.text, todo.getText());
		assertTrue(result.completedAt.isAfter(startTime));
	}

	@Test
	void whenGetOne_thenReturnCorrectOne() throws ToDoNotFoundException, NoArgsException {
		//mock
		var todo = new ToDoEntity(0L, "Test 1");
		when(toDoRepository.findById(anyLong())).thenReturn(Optional.of(todo));

		//call
		var result = toDoService.getOne(0L);

		//validate
		assertThat(result, samePropertyValuesAs(
			ToDoEntityToResponseMapper.map(todo)
		));
	}

	@Test
	void whenDeleteOne_thenRepositoryDeleteCalled() {
		//call
		var id = 0L;
		toDoService.deleteOne(id);

		//validate
		verify(toDoRepository, times(1)).deleteById(id);
	}

	@Test
	void whenIdNotFound_thenThrowNotFoundException() {
		assertThrows(ToDoNotFoundException.class, () -> toDoService.getOne(1L));
//		assertThrows(ToDoNotFoundException.class, () -> toDoService.completeToDo(1L));
//		assertThrows(ToDoNotFoundException.class, () -> toDoService.getTextById(1L));
//		assertThrows(ToDoNotFoundException.class, () -> toDoService.getTimeById(1L));
	}

	@Test
	void whenIdNull_thenNoArgsException(){
		assertThrows(NoArgsException.class, () -> toDoService.getTextById(null));
//		assertThrows(NoArgsException.class, () -> toDoService.getOne(null));
	}

	@Test
	void whenGetTextById_thenReturnCorrectOne() throws ToDoNotFoundException, NoArgsException {
		//mock
		var todo = new ToDoEntity(0L, "Test 1");
		when(toDoRepository.findById(anyLong())).thenReturn(Optional.of(todo));

		//call
		var result = toDoService.getTextById(0L);

		//validate
		assertEquals(result, todo.getText());
	}

	@Test
	void whenGetTimeById_thenReturnCorrectOne() throws ToDoNotFoundException, NoArgsException {
		//mock
		var todo = new ToDoEntity(0L, "Test 1");
		todo.completeNow();
		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if (id.equals(todo.getId())) {
				return Optional.of(todo);
			} else {
				return Optional.empty();
			}
		});
		//call
		var result = toDoService.getTimeById(0L);

		//validate
		assertEquals(result, todo.getCompletedAt().toString());
	}

	@Test
	void whenSaveToDo_thenItHasTheText() throws ToDoNotFoundException {
		//mock
		var expectedToDo = new ToDoEntity(0L, "Text for test");
		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if (id.equals(expectedToDo.getId())) {
				return Optional.of(expectedToDo);
			} else {
				return Optional.empty();
			}
		});
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
			ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
			Long id = arg.getId();
			if (id != null) {
				if (!id.equals(expectedToDo.getId()))
					return new ToDoEntity(id, arg.getText());
				expectedToDo.setText(arg.getText());
				return expectedToDo;
			} else {
				return new ToDoEntity(40158L, arg.getText());
			}
		});

		//call
		var toDoSaveRequest = new ToDoSaveRequest();
		toDoSaveRequest.id = expectedToDo.getId();
		toDoSaveRequest.text = "Text for test";
		var todo = toDoService.upsert(toDoSaveRequest);

		//validate
		assertNotNull(todo.text);
		assertEquals(expectedToDo.getText(), todo.text);
	}

	@Test
	void whenSaveToDo_thenItHasNoTime() throws ToDoNotFoundException {
		//mock
		var expectedToDo = new ToDoEntity(0L, "Text for test");
		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if (id.equals(expectedToDo.getId())) {
				return Optional.of(expectedToDo);
			} else {
				return Optional.empty();
			}
		});
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
			ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
			Long id = arg.getId();
			if (id != null) {
				if (!id.equals(expectedToDo.getId()))
					return new ToDoEntity(id, arg.getText());
				expectedToDo.setText(arg.getText());
				return expectedToDo;
			} else {
				return new ToDoEntity(40158L, arg.getText());
			}
		});

		//call
		var toDoSaveRequest = new ToDoSaveRequest();
		toDoSaveRequest.id = expectedToDo.getId();
		toDoSaveRequest.text = "Updated Item";
		var todo = toDoService.upsert(toDoSaveRequest);

		//validate
		assertNull(todo.completedAt);
	}

	@Test
	void whenUpsertIncorrectId_thenToDoNotFoundException() {
		var incorrectId = 321L;
		var entity = new ToDoSaveRequest();
		entity.id = incorrectId;

		assertThrows(ToDoNotFoundException.class, () -> toDoService.upsert(entity));
	}

}
