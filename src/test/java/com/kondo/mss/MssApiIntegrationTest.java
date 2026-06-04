package com.kondo.mss;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class MssApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listOrders_shouldReturnArray_whenStaffAuthenticated() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createThenCancelOrder_shouldMarkCancelled_andRejectDoubleCancel() throws Exception {
        String body = """
                { "customerName": "結合テスト隊", "items": [ { "productId": 2, "quantity": 1 } ] }
                """;
        MvcResult created = mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        int orderId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.orderId");

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_shouldReturnOk_forAdmin() throws Exception {
        String body = """
                { "name": "更新後ラーメン", "category": "食品", "unitPrice": 700.00, "reorderPoint": 12 }
                """;
        mockMvc.perform(put("/api/admin/products/1")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("admin", "mountain123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("更新後ラーメン"))
                .andExpect(jsonPath("$.data.reorderPoint").value(12));
    }

    @Test
    void updateProduct_shouldReturnForbidden_forStaff() throws Exception {
        String body = """
                { "name": "不正更新", "category": "食品", "unitPrice": 700.00, "reorderPoint": 12 }
                """;
        mockMvc.perform(put("/api/admin/products/1")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void inventoryMovements_shouldReturnArray_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/inventory/movements")
                        .param("productId", "1")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("staff", "mountain123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    private String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
