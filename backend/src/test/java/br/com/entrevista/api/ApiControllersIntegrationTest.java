package br.com.entrevista.api;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.api.dto.ApuracaoRequestDTO;
import br.com.entrevista.config.TestSecurityConfig;
import br.com.entrevista.service.AliquotaService;
import br.com.entrevista.support.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class ApiControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AliquotaService aliquotaService;

    @BeforeEach
    void seedAliquota() {
        aliquotaService.criar(TestDataFactory.aliquotaLucroPresumido());
    }

    @Test
    void aliquotas_crudViaApi() throws Exception {
        AliquotaDTO nova = TestDataFactory.aliquotaLucroReal();
        String json = objectMapper.writeValueAsString(nova);

        String location = mockMvc.perform(post("/api/aliquotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.regimeTributario").value("Lucro Real"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AliquotaDTO criada = objectMapper.readValue(location, AliquotaDTO.class);

        mockMvc.perform(get("/api/aliquotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/aliquotas/regime/{regime}", "Lucro Presumido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pis").value(0.65));

        mockMvc.perform(get("/api/aliquotas/{id}", criada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criada.getId().intValue()));

        criada.setPis(new java.math.BigDecimal("2.00"));
        mockMvc.perform(put("/api/aliquotas/{id}", criada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pis").value(2.00));

        mockMvc.perform(delete("/api/aliquotas/{id}", criada.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void aliquota_naoEncontrada_retorna404() throws Exception {
        mockMvc.perform(get("/api/aliquotas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("não encontrada")));
    }

    @Test
    void aliquota_payloadInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/aliquotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    void apuracao_calcularViaApi_comJwt() throws Exception {
        ApuracaoRequestDTO request = new ApuracaoRequestDTO(
                "04/2025",
                new java.math.BigDecimal("50000"),
                "Lucro Presumido");

        mockMvc.perform(post("/api/apuracoes/calcular")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.totalTributos").value(5665.00));
    }

    @Test
    void dashboard_statsViaApi() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAliquotas").value(1))
                .andExpect(jsonPath("$.totalRegimes").value(1));
    }
}
