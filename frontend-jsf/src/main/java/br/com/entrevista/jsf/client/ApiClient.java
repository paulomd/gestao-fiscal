package br.com.entrevista.jsf.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiClient {

    private final BackendHttpClient http;

    public <T> List<T> getList(String path, Class<T> type) {
        return http.readList(http.get(path), type);
    }

    public String getRaw(String path) {
        return http.get(path);
    }

    public JsonNode readTree(String json) {
        return http.readTree(json);
    }

    public <T> T get(String path, Class<T> type) {
        return http.readJson(http.get(path), type);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        String json = http.post(path, body);
        return http.readJson(json, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) {
        String json = http.put(path, body);
        return http.readJson(json, responseType);
    }

    public void delete(String path) {
        http.delete(path);
    }
}
