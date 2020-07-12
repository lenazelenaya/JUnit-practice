package com.example.demo.unit.controller;

import com.example.demo.controller.ToDoController;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.service.ToDoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ToDoController.class)
//@ActiveProfiles(profiles = "test")
class ToDoControllerTest {


    @MockBean
    private ToDoService toDoService;


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ToDoController toDoController;

    @Test
    void handleException() {
    }

    //Verifying HTTP Request Matching.   ??????? it is ok...
    @Test
    void whenInvalidRequest_thenReturns404() throws Exception {
        mockMvc.perform(get("/todos/{id}", 42L)
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    //Verifying Input Serialization
    @Test
    void whenValidInput_thenReturns200() throws Exception{
        ToDoSaveRequest saveRequest = new ToDoSaveRequest(42L, "Test serialization");
        mockMvc.perform(post("/todos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isOk());
    }

    //Verifying Input Validation
    @Test
    void whenNullValue_thenReturns400() throws Exception {
        ToDoSaveRequest saveRequest = new ToDoSaveRequest(null, null);
        mockMvc.perform(post("/todos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void whenNullValue_thenReturns404AndErrorMessage() throws Exception {
        Long id = 42L;
        MvcResult mvcResult = mockMvc.perform(get("/todos/{id}", id)
                .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expectedResult = String.format("Can not find todo with id %d", id);
        String actualResult = mvcResult.getResponse().getContentAsString();

        assertEquals(expectedResult, actualResult);
    }
}