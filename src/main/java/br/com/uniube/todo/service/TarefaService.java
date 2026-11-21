package br.com.uniube.todo.service;
import br.com.uniube.todo.dao.TarefaDAO;
import br.com.uniube.todo.model.Tarefa;
import java.util.List;

/**
 * SERVICE do sistema web.
 *
 * As regras de negócio (título obrigatório, tamanho máximo, data de criação)
 * agora ficam na API, que é quem trata os dados. Aqui só decidimos qual
 * operação chamar. Se a API recusar, o DAO lança IllegalArgumentException
 * e o TarefaServlet mostra a mensagem na tela, como antes.
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
