package br.com.uniube.todo.dao;

import br.com.uniube.todo.config.ApiClient;
import br.com.uniube.todo.config.LocalDateAdapter;
import br.com.uniube.todo.config.RespostaApi;
import br.com.uniube.todo.model.Tarefa;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

// Usa a API como intermediario para conexão BD usando comunivação via cod HTTP

public class TarefaDAO {

    private final ApiClient api;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, LocalDateAdapter.SERIALIZER)
            .registerTypeAdapter(LocalDate.class, LocalDateAdapter.DESERIALIZER)
            .create();

    public TarefaDAO() {
        this.api = ApiClient.getInstance();
    }

    //GET /tarefas
    public List<Tarefa> listarTodas() {
        RespostaApi resposta = this.api.enviar("GET", "/tarefas", null);

        if (resposta.getStatus() == 200) {
            Type tipoLista = new TypeToken<List<Tarefa>>() { }.getType();
            return gson.fromJson(resposta.getCorpo(), tipoLista);
        }
        throw this.erroInesperado("listar tarefas", resposta);
    }

    //GET /tarefas/{id}  (retorna null se não existir)
    public Tarefa buscarPorId(int id) {
        RespostaApi resposta = this.api.enviar("GET", "/tarefas/" + id, null);

        if (resposta.getStatus() == 200) {
            return gson.fromJson(resposta.getCorpo(), Tarefa.class);
        }
        if (resposta.getStatus() == 404) {
            return null;
        }
        throw this.erroInesperado("buscar tarefa", resposta);
    }


    public void inserir(Tarefa tarefa) {
        RespostaApi resposta = this.api.enviar("POST", "/tarefas", gson.toJson(tarefa));

        if (resposta.getStatus() == 201) {
            Tarefa criada = gson.fromJson(resposta.getCorpo(), Tarefa.class);
            tarefa.setId(criada.getId());
            tarefa.setDataCriacao(criada.getDataCriacao());
            return;
        }
        if (resposta.getStatus() == 400) {
            throw new IllegalArgumentException(this.lerErro(resposta));
        }
        throw this.erroInesperado("inserir tarefa", resposta);
    }

    public void alterar(Tarefa tarefa) {
        RespostaApi resposta = this.api.enviar("PUT", "/tarefas/" + tarefa.getId(), gson.toJson(tarefa));

        if (resposta.getStatus() == 200) {
            return;
        }
        if (resposta.getStatus() == 400 || resposta.getStatus() == 404) {
            throw new IllegalArgumentException(this.lerErro(resposta));
        }
        throw this.erroInesperado("alterar tarefa", resposta);
    }

    public void deletar(int id) {
        RespostaApi resposta = this.api.enviar("DELETE", "/tarefas/" + id, null);

        if (resposta.getStatus() == 204) {
            return;
        }
        if (resposta.getStatus() == 404) {
            throw new IllegalArgumentException(this.lerErro(resposta));
        }
        throw this.erroInesperado("deletar tarefa", resposta);
    }

    private String lerErro(RespostaApi resposta) {
        try {
            JsonObject json = gson.fromJson(resposta.getCorpo(), JsonObject.class);
            return json.get("erro").getAsString();
        } catch (JsonSyntaxException | NullPointerException e) {
            return "Erro na requisição (status " + resposta.getStatus() + ").";
        }
    }

    private RuntimeException erroInesperado(String acao, RespostaApi resposta) {
        return new RuntimeException("Erro ao " + acao + " (a API respondeu " + resposta.getStatus() + ").");
    }
}
