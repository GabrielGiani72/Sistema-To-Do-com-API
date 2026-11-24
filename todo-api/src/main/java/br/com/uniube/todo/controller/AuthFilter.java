package br.com.uniube.todo.controller;

import br.com.uniube.todo.config.JwtUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Valida token. Caso invalido lança 401
 */
@WebFilter("/api/tarefas/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String header = req.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            negar(resp, "Token não informado.");
            return;
        }

        String token = header.substring("Bearer ".length());

        try {
            JwtUtil.validarToken(token);
        } catch (JWTVerificationException e) {
            negar(resp, "Token inválido ou expirado.");
            return;
        }

        //Token ok: segue para o servlet
        chain.doFilter(request, response);
    }

    private void negar(HttpServletResponse resp, String mensagem) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"erro\": \"" + mensagem + "\"}");
    }
}
