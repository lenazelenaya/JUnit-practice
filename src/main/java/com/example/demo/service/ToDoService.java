package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.exception.NoArgsException;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Service
public class ToDoService {
	
	private final ToDoRepository toDoRepository;

	public ToDoService(ToDoRepository toDoRepository) {
		this.toDoRepository = toDoRepository;
	}
	
	public List<ToDoResponse> getAll() {
		return toDoRepository.findAll().stream()
			.map(ToDoEntityToResponseMapper::map)
			.collect(Collectors.toList()); 
	}

	public ToDoResponse upsert(ToDoSaveRequest toDoDTO) throws ToDoNotFoundException {
		ToDoEntity todo;
		//update if it has id or create if it hasn't
		if (toDoDTO.id == null) {
			todo = new ToDoEntity(toDoDTO.text);
		} else {
			todo = toDoRepository.findById(toDoDTO.id)
					.orElseThrow(() -> new ToDoNotFoundException(toDoDTO.id));
			todo.setText(toDoDTO.text);
		}
		return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
	}

	public ToDoResponse completeToDo(Long id) throws ToDoNotFoundException, NoArgsException {
		if (id == null) throw new NoArgsException();
		ToDoEntity todo = toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id));
		todo.completeNow();
		return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
	}

	public ToDoResponse getOne(Long id) throws ToDoNotFoundException, NoArgsException {
		if (id == null) throw new NoArgsException();
		return  ToDoEntityToResponseMapper.map(
			toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id))
		);
	}

	public String getTextById(Long id) throws ToDoNotFoundException, NoArgsException {
		if (id == null) throw new NoArgsException();
		ToDoEntity todo = toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id));
		return todo.getText();
	}

	public String getTimeById(Long id) throws ToDoNotFoundException, NoArgsException {
		if (id == null) throw new NoArgsException();
		ToDoEntity todo = toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id));
		return todo.getCompletedAt().toString();
	}

	public void deleteOne(Long id) {
		toDoRepository.deleteById(id);
	}

}
