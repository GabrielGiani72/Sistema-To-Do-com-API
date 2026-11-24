package br.com.uniube.todo.service;
import br.com.uniube.todo.dao.TarefaDAO;
import br.com.uniube.todo.model.Tarefa;
import java.util.List;

/**
 * SERVICE do sistema web.
 * Regras ficam na API. Tarefa Service só escolhe qual opção da API chamar 
 * e trata recursa com mensagem amigavel
 */
public class TarefaService {

    private final TarefaDAO tarefaDAO;

    public TarefaService() {
        this.tarefaDAO = new TarefaDAO();
    }

    public List<Tarefa> listar() {
        return this.tarefaDAO.listarTodas();
    }

    public Tarefa buscarPorId(Integer id) {
        if (id == null) {
            return null;
        }
        return this.tarefaDAO.buscarPorId(id);
    }

    public void salvar(Tarefa tarefa) {
        if (tarefa == null) {
            throw new IllegalArgumentException("Tarefa é obrigatória.");
        }

        if (tarefa.getId() == 0) {
            this.tarefaDAO.inserir(tarefa);
            return;
        }

        //Edição: o formulário não envia o status, então mantém o que já estava salvo
        Tarefa existente = this.tarefaDAO.buscarPorId(tarefa.getId());
        if (existente == null) {
            throw new IllegalArgumentException("Tarefa não encontrada para alteração.");
        }
        tarefa.setConcluida(existente.isConcluida());
        this.tarefaDAO.alterar(tarefa);
    }

    public void deletar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id é obrigatório para excluir.");
        }
        this.tarefaDAO.deletar(id);
    }

    public void alternarConcluida(Integer id) {
        Tarefa tarefa = this.buscarPorId(id);
        if (tarefa == null) {
            throw new IllegalArgumentException("Tarefa não encontrada.");
        }
        tarefa.setConcluida(!tarefa.isConcluida());
        this.tarefaDAO.alterar(tarefa);
    }
}
