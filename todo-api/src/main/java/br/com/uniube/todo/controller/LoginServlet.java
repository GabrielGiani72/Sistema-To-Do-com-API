package br.com.uniube.todo.controller;

import br.com.uniube.todo.config.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/login
 * Recebe {"usuario": "...", "senha": "..."} e devolve {"token": "..."}.
 *
 * Para manter simples, existe um único usuário fixo no código.
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private static final String USUARIO = "admin";
    private static final String SENHA = "123456";

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        JsonObject corpo;
        try {
            corpo = gson.fromJson(req.getReader(), JsonObject.class);
        } catch (JsonSyntaxException e) {
            corpo = null;
        }

        if (corpo == null || !corpo.has("usuario") || !corpo.has("senha")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Map.of("erro", "Informe usuario e senha.")));
            return;
        }

        String usuario = corpo.get("usuario").getAsString();
        String senha = corpo.get("senha").getAsString();

        if (!USUARIO.equals(usuario) || !SENHA.equals(senha)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(gson.toJson(Map.of("erro", "Usuário ou senha inválidos.")));
            return;
        }

        resp.getWriter().write(gson.toJson(Map.of("token", JwtUtil.gerarToken(usuario))));
    }
}
