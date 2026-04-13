package com.kondo.mss.report;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dailySales_shouldReturnOk_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/reports/sales/daily")
                        .param("from", "2026-04-01")
                        .param("to", "2026-04-30")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].saleDate").value("2026-04-02"));
    }

    @Test
    void dailySales_shouldReturnUnauthorized_whenNoCredential() throws Exception {
        mockMvc.perform(get("/api/reports/sales/daily")
                        .param("from", "2026-04-01")
                        .param("to", "2026-04-30"))
                .andExpect(status().isUnauthorized());
    }

    private String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
