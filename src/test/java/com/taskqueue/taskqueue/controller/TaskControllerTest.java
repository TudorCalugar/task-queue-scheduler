package com.taskqueue.taskqueue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postTask_cuTypeValid_intoarce200() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content("{\"type\":\"email\",\"payload\":\"salut\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("email"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void postTask_faraType_intoarce400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content("{\"payload\":\"fara type\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.type").exists());
    }

    @Test
    void getTasks_intoarceLista() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void getTask_inexistent_intoarce404() throws Exception {
        mockMvc.perform(get("/tasks/id-care-nu-exista"))
                .andExpect(status().isNotFound());
    }
}