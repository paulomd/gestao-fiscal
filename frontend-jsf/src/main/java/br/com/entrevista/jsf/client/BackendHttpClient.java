package br.com.entrevista.jsf.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class BackendHttpClient {

    private final String apiBaseUrl;
    private final ApiTokenResolver apiTokenResolver;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public BackendHttpClient(
            @Value("${app.api-base-url}") String apiBaseUrl,
            ApiTokenResolver apiTokenResolver) {
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        this.apiTokenResolver = apiTokenResolver;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String get(String path) {
        return send(HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Authorization", bearer())
                .GET()
                .build());
    }

    public String post(String path, Object body) {
        return sendJson(HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Authorization", bearer())
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body))), body);
    }

    public String put(String path, Object body) {
        return sendJson(HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Authorization", bearer())
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(body))), body);
    }

    public void delete(String path) {
        send(HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Authorization", bearer())
                .DELETE()
                .build());
    }

    public JsonNode readTree(String json) {
        try {
            if (json == null || json.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Falha ao interpretar JSON da API: {}", resumo(json), e);
            throw new ApiException("Erro ao ler resposta da API: " + e.getMessage(), e);
        }
    }

    public <T> T readJson(String json, Class<T> type) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Falha ao mapear {}: {}", type.getSimpleName(), resumo(json), e);
            throw new ApiException("Erro ao ler resposta da API: " + e.getMessage(), e);
        }
    }

    public <T> java.util.List<T> readList(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, type));
        } catch (Exception e) {
            throw new ApiException("Erro ao ler lista da API", e);
        }
    }

    private String sendJson(HttpRequest.Builder builder, Object body) {
        HttpRequest request = builder
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        log.info("{} {} payload={}", request.method(), request.uri(), toJson(body));
        return send(request);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ApiException("API retornou " + response.statusCode() + ": " + resumo(response.body()));
            }
            String body = response.body() != null ? response.body() : "";
            if (!body.isBlank() && body.trim().startsWith("<")) {
                throw new ApiException("API retornou HTML em vez de JSON — faça logout/login ou verifique o token OAuth2");
            }
            return body;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Falha na comunicação com a API: " + e.getMessage(), e);
        }
    }

    private String bearer() {
        return "Bearer " + apiTokenResolver.resolveToken();
    }

    private URI uri(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(apiBaseUrl + p);
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new ApiException("Erro ao montar JSON", e);
        }
    }

    private String resumo(String body) {
        if (body == null || body.isBlank()) {
            return "(sem detalhe)";
        }
        if (body.trim().startsWith("<")) {
            return "resposta HTML — verifique login";
        }
        String limpo = body.replaceAll("\\s+", " ").trim();
        return limpo.length() > 280 ? limpo.substring(0, 280) + "..." : limpo;
    }
}
