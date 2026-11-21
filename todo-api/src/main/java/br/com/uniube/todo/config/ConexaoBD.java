package br.com.uniube.todo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe responsável por abrir e reaproveitar a conexão com o MySQL.
 *
 * Usa o padrão Singleton: só existe UMA instância dessa classe
 * em todo o sistema, evitando abrir conexões demais com o banco.
 *
 * IMPORTANTE: o host "mysql" abaixo é o nome do serviço definido
 * no docker-compose.yml — não é "localhost" porque a aplicação
 * roda dentro do container do Tomcat, e o MySQL está em outro container.
 */
public class ConexaoBD {

    private static final String URL =
            "jdbc:mysql://mysql:3306/todo_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "todo_user";
    private static final String PASSWORD = "todo123";

    private static ConexaoBD instance;
    private Connection conexao;

    private ConexaoBD() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL não encontrado no projeto (confira o pom.xml).", e);
        }
    }

    public static synchronized ConexaoBD getInstance() {
        if (instance == null) {
            instance = new ConexaoBD();
        }
        return instance;
    }

    private Connection obterConexao() throws SQLException {
        if (this.conexao == null || this.conexao.isClosed()) {
            this.conexao = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return this.conexao;
    }

    /**
     * Usado para SELECT — retorna um ResultSet com os dados encontrados.
     */
    public ResultSet executar(String sql, Object... parametros) throws SQLException {
        Connection conn = this.obterConexao();
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
        return ps.executeQuery();
    }

    /**
     * Usado para INSERT/UPDATE/DELETE — retorna quantas linhas foram afetadas.
     */
    public int executarUpdate(String sql, Object... parametros) throws SQLException {
        Connection conn = this.obterConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            return ps.executeUpdate();
        }
    }

    /**
     * Usado para INSERT quando precisamos do id que o banco gerou (AUTO_INCREMENT).
     */
    public int executarInsert(String sql, Object... parametros) throws SQLException {
        Connection conn = this.obterConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            ps.executeUpdate();

            //Pega o id gerado pelo banco
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getInt(1);
                }
            }
            return 0;
        }
    }
}
