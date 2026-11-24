package br.com.uniube.todo.config;

/**
 * Guarda o que a resposta da API e o Json do corpo
 */
public class RespostaApi {

    private final int status;
    private final String corpo;

    public RespostaApi(int status, String corpo) {
        this.status = status;
        this.corpo = corpo;
    }

    public int getStatus() {
        return status;
    }

    public String getCorpo() {
        return corpo;
    }
}
