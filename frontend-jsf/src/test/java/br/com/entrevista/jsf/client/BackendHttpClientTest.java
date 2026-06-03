package br.com.entrevista.jsf.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackendHttpClientTest {

    @Mock
    private ApiTokenResolver apiTokenResolver;

    private HttpServer server;
    private BackendHttpClient client;
    private final AtomicReference<String> lastAuth = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        when(apiTokenResolver.resolveToken()).thenReturn("token-teste");
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        client = new BackendHttpClient("http://127.0.0.1:" + port, apiTokenResolver);

        server.createContext("/api/ping", exchange -> {
            lastAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/api/itens", exchange -> {
            lastAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            String method = exchange.getRequestMethod();
            byte[] body = switch (method) {
                case "GET" -> "[{\"nome\":\"A\"}]".getBytes(StandardCharsets.UTF_8);
                case "POST", "PUT" -> "{\"id\":1,\"nome\":\"A\"}".getBytes(StandardCharsets.UTF_8);
                case "DELETE" -> new byte[0];
                default -> new byte[0];
            };
            int status = "DELETE".equals(method) ? 204 : 200;
            if (status == 204) {
                exchange.sendResponseHeaders(status, -1);
            } else {
                exchange.sendResponseHeaders(status, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        });

        server.createContext("/api/erro", exchange -> {
            byte[] body = "{\"message\":\"falhou\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/api/html", exchange -> {
            byte[] body = "<html>login</html>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void get_enviaBearer_eInterpretaJson() {
        String json = client.get("/api/ping");

        assertThat(lastAuth.get()).isEqualTo("Bearer token-teste");
        JsonNode node = client.readTree(json);
        assertThat(node.get("ok").asBoolean()).isTrue();
    }

    @Test
    void post_put_delete_eLeituraDeLista() {
        Map<String, Object> payload = Map.of("nome", "A");

        String criado = client.post("/api/itens", payload);
        assertThat(client.readJson(criado, Resposta.class).nome()).isEqualTo("A");

        String atualizado = client.put("/api/itens/1", payload);
        assertThat(client.readJson(atualizado, Resposta.class).id()).isEqualTo(1);

        List<Resposta> lista = client.readList(client.get("/api/itens"), Resposta.class);
        assertThat(lista).hasSize(1).first().satisfies(r -> assertThat(r.nome()).isEqualTo("A"));

        client.delete("/api/itens/1");
    }

    @Test
    void statusErro_lancaApiException() {
        assertThatThrownBy(() -> client.get("/api/erro"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void respostaHtml_lancaApiException() {
        assertThatThrownBy(() -> client.get("/api/html"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("HTML");
    }

    @Test
    void baseUrlComBarraFinal_normaliza() {
        BackendHttpClient comBarra = new BackendHttpClient("http://127.0.0.1:" + server.getAddress().getPort() + "/",
                apiTokenResolver);
        assertThat(comBarra.get("/api/ping")).contains("\"ok\":true");
    }

    record Resposta(int id, String nome) {
    }
}
