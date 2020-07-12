package com.example.demo.repository;

import com.example.demo.model.ToDoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ToDoRepository extends JpaRepository<ToDoEntity, Long> {

}