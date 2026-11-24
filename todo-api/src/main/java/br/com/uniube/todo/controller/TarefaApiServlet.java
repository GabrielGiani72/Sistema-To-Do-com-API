package br.com.uniube.todo.controller;

import br.com.uniube.todo.config.LocalDateAdapter;
import br.com.uniube.todo.model.Tarefa;
import br.com.uniube.todo.service.TarefaService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
  Controller com CRUD da API REST via GET, POST...
 */
@WebServlet("/api/tarefas/*")
public class TarefaApiServlet extends HttpServlet {

    private final TarefaService tarefaService = new TarefaService();

    //Gson com o adaptador de data
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, LocalDateAdapter.SERIALIZER)
            .registerTypeAdapter(LocalDate.class, LocalDateAdapter.DESERIALIZER)
            .create();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        try {
           
            if (req.getMethod().equals("PATCH")) {
                doPatch(req, resp);
            } else {
                super.service(req, resp);
            }
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            
            String msg = (e instanceof JsonSyntaxException) ? "JSON inválido." : e.getMessage();
            enviarErro(resp, HttpServletResponse.SC_BAD_REQUEST, msg);
        } catch (RuntimeException e) {
           
            enviarErro(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = lerId(req);

        if (id == null) {
            resp.getWriter().write(gson.toJson(tarefaService.listar()));
            return;
        }

        Tarefa tarefa = tarefaService.buscarPorId(id);
        if (tarefa == null) {
            enviarErro(resp, HttpServletResponse.SC_NOT_FOUND, "Tarefa não encontrada.");
            return;
        }
        resp.getWriter().write(gson.toJson(tarefa));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Tarefa tarefa = lerCorpo(req);

        //ignora id e data que o cliente tenha mandado
        tarefa.setId(0);
        tarefaService.salvar(tarefa);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(gson.toJson(tarefa));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = lerId(req);
        if (id == null) {
            enviarErro(resp, HttpServletResponse.SC_BAD_REQUEST, "Informe o id na URL.");
            return;
        }

        Tarefa existente = tarefaService.buscarPorId(id);
        if (existente == null) {
            enviarErro(resp, HttpServletResponse.SC_NOT_FOUND, "Tarefa não encontrada.");
            return;
        }

        Tarefa tarefa = lerCorpo(req);
        tarefa.setId(id);
        tarefaService.salvar(tarefa);
        tarefa.setDataCriacao(existente.getDataCriacao());
        resp.getWriter().write(gson.toJson(tarefa));
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = lerId(req);
        if (id == null) {
            enviarErro(resp, HttpServletResponse.SC_BAD_REQUEST, "Informe o id na URL.");
            return;
        }

        Tarefa tarefa = tarefaService.buscarPorId(id);
        if (tarefa == null) {
            enviarErro(resp, HttpServletResponse.SC_NOT_FOUND, "Tarefa não encontrada.");
            return;
        }

        JsonObject campos = gson.fromJson(req.getReader(), JsonObject.class);
        if (campos == null) {
            throw new IllegalArgumentException("Corpo da requisição vazio.");
        }

        if (campos.has("titulo")) {
            tarefa.setTitulo(lerTexto(campos.get("titulo")));
        }
        if (campos.has("descricao")) {
            tarefa.setDescricao(lerTexto(campos.get("descricao")));
        }
        if (campos.has("concluida")) {
            tarefa.setConcluida(campos.get("concluida").getAsBoolean());
        }

        tarefaService.salvar(tarefa);
        resp.getWriter().write(gson.toJson(tarefa));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = lerId(req);
        if (id == null) {
            enviarErro(resp, HttpServletResponse.SC_BAD_REQUEST, "Informe o id na URL.");
            return;
        }

        if (tarefaService.buscarPorId(id) == null) {
            enviarErro(resp, HttpServletResponse.SC_NOT_FOUND, "Tarefa não encontrada.");
            return;
        }

        tarefaService.deletar(id);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


    private Integer lerId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            return null;
        }
        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Id inválido.");
        }
    }


    private String lerTexto(JsonElement elemento) {
        return elemento.isJsonNull() ? null : elemento.getAsString();
    }

    private Tarefa lerCorpo(HttpServletRequest req) throws IOException {
        Tarefa tarefa = gson.fromJson(req.getReader(), Tarefa.class);
        if (tarefa == null) {
            throw new IllegalArgumentException("Corpo da requisição vazio.");
        }
        return tarefa;
    }


    private void enviarErro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(Map.of("erro", mensagem)));
    }
}
