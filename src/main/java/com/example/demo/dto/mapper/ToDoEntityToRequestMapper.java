package com.example.demo.dto.mapper;

import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.model.ToDoEntity;

public class ToDoEntityToRequestMapper {
    public static ToDoSaveRequest map(ToDoEntity todoEntity) {
        if (todoEntity == null)
            return null;
        return new ToDoSaveRequest(todoEntity.getId(), todoEntity.getText());
    }
}
