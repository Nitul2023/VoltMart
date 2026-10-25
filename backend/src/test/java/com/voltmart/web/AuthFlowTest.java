package com.voltmart.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voltmart.VoltMartApplication;
import com.voltmart.domain.entity.Role;
import com.voltmart.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = VoltMartApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void seedRole() {
        if (roleRepository.findByName("CUSTOMER").isEmpty()) {
            Role role = new Role();
            role.setName("CUSTOMER");
            roleRepository.save(role);
        }
    }

    @Test
    void registerAndLogin() throws Exception {
        String email = "buyer@example.com";
        String body = """
                {"email":"%s","password":"Password123!","firstName":"Ada","lastName":"Lovelace"}
                """.formatted(email);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"Password123!\"}".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        String body = """
                {"email":"dup@example.com","password":"Password123!","firstName":"Dup","lastName":"User"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
