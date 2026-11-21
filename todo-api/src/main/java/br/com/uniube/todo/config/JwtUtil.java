package br.com.uniube.todo.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

//Gera e valida tokens JWT usando a biblioteca

public class JwtUtil {

    private static final String SEGREDO = "segredo-do-todo-uniube";
    private static final String EMISSOR = "todo-api";
    private static final long VALIDADE_MS = 60 * 60 * 1000; //1 hora

    private static final Algorithm ALGORITMO = Algorithm.HMAC256(SEGREDO);

    //Cria um token para usuário
    public static String gerarToken(String usuario) {
        return JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(usuario)
                .withExpiresAt(new Date(System.currentTimeMillis() + VALIDADE_MS))
                .sign(ALGORITMO);
    }

    //Confere assinatura, emissor e validade, se estiver inválido lança JWTVerificationException
    public static void validarToken(String token) {
        JWT.require(ALGORITMO)
                .withIssuer(EMISSOR)
                .build()
                .verify(token);
    }
}
