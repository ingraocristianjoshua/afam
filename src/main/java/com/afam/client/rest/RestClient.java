package com.afam.client.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

/**
 * RestClient – unico punto di uscita HTTP dal client JavaFX verso il server.
 *
 * Tutte le boundary invocano questo client per comunicare con le API REST;
 * nessuna boundary crea connessioni di rete autonomamente.
 *
 * Dopo il login, il server restituisce l'id utente che viene memorizzato
 * e incluso automaticamente come header X-User-Id in ogni richiesta successiva.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RestClient {

    private static final RestClient INSTANCE = new RestClient();
    public  static RestClient getInstance() { return INSTANCE; }

    private final HttpClient   http;
    private final ObjectMapper mapper;
    private       String       baseUri;

    /** Id utente loggato, impostato dopo login riuscito. */
    private String currentUserId;

    private RestClient() {
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties p = new Properties();
            if (in != null) p.load(in);
            String fromProp = p.getProperty("server.baseUri", "http://localhost:8080/api/");
            baseUri = System.getProperty("server.baseUri", fromProp);
        } catch (IOException e) {
            baseUri = System.getProperty("server.baseUri", "http://localhost:8080/api/");
        }
    }

    // ── Gestione sessione ─────────────────────────────────────────────────────

    public void setCurrentUserId(String id) { this.currentUserId = id; }
    public String getCurrentUserId()         { return currentUserId; }
    public boolean isLoggedIn()              { return currentUserId != null; }

    public void logout() { currentUserId = null; }

    // ── Metodi HTTP ───────────────────────────────────────────────────────────

    /**
     * Esegue una POST con corpo JSON e restituisce la risposta come mappa.
     * @param path  percorso relativo (es. "auth/accedi")
     * @param body  mappa serializzata come JSON
     * @return mappa deserializzata dalla risposta JSON
     * @throws RestException se il server risponde con stato >= 400
     */
    public Map<String, Object> post(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUri + path))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));
            if (currentUserId != null) {
                builder.header("X-User-Id", currentUserId);
            }
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return handleResponse(resp);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(0, "Impossibile contattare il server: " + e.getMessage());
        }
    }

    /**
     * Esegue una GET e restituisce la risposta come mappa.
     */
    public Map<String, Object> get(String path) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUri + path))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10));
            if (currentUserId != null) {
                builder.header("X-User-Id", currentUserId);
            }
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return handleResponse(resp);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(0, "Impossibile contattare il server: " + e.getMessage());
        }
    }

    /**
     * Esegue una PUT con corpo JSON.
     */
    public Map<String, Object> put(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUri + path))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));
            if (currentUserId != null) builder.header("X-User-Id", currentUserId);
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return handleResponse(resp);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(0, "Impossibile contattare il server: " + e.getMessage());
        }
    }

    /**
     * Esegue una PATCH con corpo JSON.
     */
    public Map<String, Object> patch(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUri + path))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));
            if (currentUserId != null) builder.header("X-User-Id", currentUserId);
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return handleResponse(resp);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(0, "Impossibile contattare il server: " + e.getMessage());
        }
    }

    /**
     * Esegue una DELETE con corpo JSON opzionale.
     */
    public Map<String, Object> delete(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUri + path))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));
            if (currentUserId != null) builder.header("X-User-Id", currentUserId);
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return handleResponse(resp);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(0, "Impossibile contattare il server: " + e.getMessage());
        }
    }

    /**
     * Esegue una DELETE senza corpo.
     */
    public Map<String, Object> delete(String path) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUri + path))
                    .header("Accept", "application/json")
                    .DELETE()
                    .timeout(Duration.ofSeconds(10));
            if (currentUserId != null) {
                builder.header("X-User-Id", currentUserId);
            }
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return handleResponse(resp);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(0, "Impossibile contattare il server: " + e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> handleResponse(HttpResponse<String> resp) throws IOException {
        Map<String, Object> body = mapper.readValue(resp.body(),
                new TypeReference<Map<String, Object>>() {});
        if (resp.statusCode() >= 400) {
            String msg = body.containsKey("errore")
                    ? (String) body.get("errore")
                    : "Errore HTTP " + resp.statusCode();
            throw new RestException(resp.statusCode(), msg);
        }
        return body;
    }

    // ── Eccezione ─────────────────────────────────────────────────────────────

    /**
     * Eccezione lanciata quando il server risponde con un codice di errore
     * o quando la connessione non è disponibile (codice 0).
     */
    public static class RestException extends RuntimeException {
        private final int statusCode;
        public RestException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        public int getStatusCode() { return statusCode; }
        public boolean isConnectionError() { return statusCode == 0; }
    }
}
