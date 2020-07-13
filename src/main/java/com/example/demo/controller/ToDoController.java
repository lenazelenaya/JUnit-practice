package com.example.demo.controller;

import com.example.demo.exception.ControllerExceptionHandler;
import com.example.demo.exception.NoArgsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.service.ToDoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ToDoController {

	@Autowired
	ToDoService toDoService;

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ToDoNotFoundException.class)
	public String handleException(Exception ex) {
		return ex.getMessage();
	}
	
	@GetMapping("/todos")
	@Valid List<ToDoResponse> getAll() {
		return toDoService.getAll();
	}

	@PostMapping("/todos")
	@Valid ToDoResponse save(@Valid @RequestBody ToDoSaveRequest todoSaveRequest) throws ToDoNotFoundException {
		return toDoService.upsert(todoSaveRequest);
	}

	@PutMapping("/todos/{id}/complete")
	@Valid ToDoResponse save(@PathVariable Long id) throws ToDoNotFoundException, NoArgsException {
		return toDoService.completeToDo(id);
	}

	@GetMapping("/todos/{id}")
	@Valid ToDoResponse getOne(@PathVariable Long id) throws ToDoNotFoundException, NoArgsException {
			return toDoService.getOne(id);
	}

	@DeleteMapping("/todos/{id}")
	void delete(@PathVariable Long id) {
		toDoService.deleteOne(id);
	}

	@GetMapping("/todos/{id}/text")
	@Valid String getText(@PathVariable Long id) throws ToDoNotFoundException, NoArgsException {
		return toDoService.getTextById(id);
	}

}