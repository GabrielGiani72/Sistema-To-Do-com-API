package br.com.uniube.todo.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL = "http://api:8080/api";
    private static final String USUARIO = "admin";
    private static final String SENHA = "123456";

    private static ApiClient instance;

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private String token;

    private ApiClient() {
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public synchronized RespostaApi enviar(String metodo, String caminho, String corpoJson) {
        if (this.token == null) {
            this.fazerLogin();
        }

        RespostaApi resposta = this.executar(metodo, caminho, corpoJson, true);

        //erro de expiração do token
        if (resposta.getStatus() == 401) {
            this.fazerLogin();
            resposta = this.executar(metodo, caminho, corpoJson, true);
        }
        return resposta;
    }

    //recebe o token 
    private void fazerLogin() {
        String corpo = gson.toJson(Map.of("usuario", USUARIO, "senha", SENHA));
        RespostaApi resposta = this.executar("POST", "/login", corpo, false);

        if (resposta.getStatus() != 200) {
            throw new RuntimeException("Falha no login da API (status " + resposta.getStatus() + ").");
        }
        this.token = gson.fromJson(resposta.getCorpo(), JsonObject.class).get("token").getAsString();
    }

    private RespostaApi executar(String metodo, String caminho, String corpoJson, boolean comToken) {
        HttpRequest.BodyPublisher corpo = (corpoJson == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpoJson, StandardCharsets.UTF_8);

        HttpRequest.Builder requisicao = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + caminho))
                .header("Content-Type", "application/json; charset=UTF-8")
                .method(metodo, corpo);
        if (comToken) {
            requisicao.header("Authorization", "Bearer " + this.token);
        }

        try {
            HttpResponse<String> resposta = this.http.send(
                    requisicao.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new RespostaApi(resposta.statusCode(), resposta.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Não foi possível conectar na API.", e);
        }
    }
}
