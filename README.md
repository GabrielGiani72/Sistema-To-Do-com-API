Trabalho de Aplicações para Internet.

Sistema de gerenciamento de tarefas (To-Do) desenvolvido em Java, aplicando o padrão de arquitetura MVC (Model-View-Controller), complementado pelos padrões de projeto DAO (Data Access Object) e Service Layer.

O sistema é dividido em duas aplicações: o sistema web (MVC), que mostra as telas, e uma API REST, que é a única que acessa o banco de dados. O sistema web conversa com a API por HTTP, usando token JWT.

Funcionalidades
Criar tarefa (título e descrição)
Listar todas as tarefas cadastradas
Editar uma tarefa existente
Marcar/desmarcar tarefa como concluída
Excluir tarefa
API REST com CRUD da tarefa, protegida por token JWT
Tecnologias utilizadas
Java 17 -> linguagem principal
Jakarta Servlet / JSP -> camada web (Controller e View) e controllers da API
JSTL -> tags para lógica simples dentro das JSPs
Gson -> conversão entre objetos Java e JSON
java-jwt (Auth0) -> geração e validação do token JWT
java.net.http (HttpClient) -> chamadas do sistema web para a API
Maven -> gerenciamento de dependências e build
Apache Tomcat 10.1 -> servidor de aplicação
MySQL 8.4 -> banco de dados
Docker / Docker Compose -> containerização do ambiente (MySQL + API + sistema web)
Postman -> teste e documentação dos endpoints
Git / GitHub -> versionamento de código
Arquitetura

O projeto tem duas aplicações, cada uma em seu container, e as duas seguem o padrão MVC com as camadas Controller, Service e DAO:

projetoweb_patrik (sistema web, container todo-tomcat): telas JSP. Não acessa o banco, só consome a API.
todo-api (API REST, container todo-api): valida o token, aplica as regras de negócio e acessa o MySQL (container todo-mysql).

Sistema web:

src/main/java/br/com/uniube/todo/
├── model/       -> Tarefa.java (representa os dados de uma tarefa)
├── config/      -> ApiClient.java (Singleton: login, token e chamadas à API)
│                   RespostaApi.java, LocalDateAdapter.java
├── dao/         -> TarefaDAO.java (chama a API e interpreta os códigos HTTP)
├── service/     -> TarefaService.java (decide qual operação chamar)
└── controller/  -> TarefaServlet.java (recebe requisições HTTP e decide a view)

src/main/webapp/
├── css/         -> style.css
└── WEB-INF/
    └── views/   -> lista.jsp, form.jsp (telas do sistema)

API (pasta todo-api):

todo-api/src/main/java/br/com/uniube/todo/
├── model/       -> Tarefa.java
├── config/      -> ConexaoBD.java (conexão com o MySQL, padrão Singleton)
│                   JwtUtil.java (gera e valida o token), LocalDateAdapter.java
├── dao/         -> TarefaDAO.java (acesso ao banco de dados / SQL)
├── service/     -> TarefaService.java (regras de negócio e validações)
└── controller/  -> TarefaApiServlet.java (rotas /api/tarefas, responde JSON)
                    LoginServlet.java (rota /api/login, devolve o token)
                    AuthFilter.java (confere o token antes de chegar no servlet)

Fluxo de uma requisição:

Navegador -> TarefaServlet -> TarefaService -> TarefaDAO (site) -> ApiClient
    -> HTTP + token JWT -> AuthFilter -> TarefaApiServlet -> TarefaService -> TarefaDAO -> MySQL
    <- JSON + código HTTP <- (volta pelo mesmo caminho até a JSP desenhar a tela)

O ApiClient faz o login uma vez, guarda o token e o envia em todas as chamadas (Authorization: Bearer <token>). Se o token expirar (401), ele faz login de novo e repete a chamada. O TarefaDAO do site interpreta a resposta: 200/201/204 sucesso, 400 mostra a mensagem de erro da API no formulário, 404 tarefa não encontrada.

Padrões de projeto aplicados
MVC — separa dados (Model), interface (View) e controle de fluxo (Controller)
DAO — isola todo o acesso a dados numa camada própria (TarefaDAO)
Service Layer — concentra as regras de negócio e validações (TarefaService)
Singleton — uma única instância de conexão: ConexaoBD (banco, na API) e ApiClient (comunicação com a API, no sistema web)
Como rodar o projeto
Pré-requisitos
Docker Desktop instalado
Java 17+ instalado
Maven instalado (ou usar o wrapper, se disponível)
Git instalado
Passo a passo
git clone <URL_DO_REPOSITORIO>
cd projetoweb_patrik
mvn clean package
cd todo-api
mvn clean package
cd ..
docker compose up -d

O primeiro mvn gera o sistema web (pasta deploy) e o segundo gera a API (pasta deploy-api).

Depois acesse no navegador:

http://localhost:8080/projetoweb_patrik/

A API fica acessível em:

http://localhost:8081/api/tarefas

Para encerrar o ambiente:

docker compose down
API REST

URL base: http://localhost:8081/api

Autenticação

POST /api/login com o usuário fixo admin / 123456:

json
{ "usuario": "admin", "senha": "123456" }

Resposta (200): { "token": "eyJhbGciOi..." }

O token vale 1 hora e deve ser enviado em todas as rotas de tarefas, no header Authorization: Bearer <token>. Sem token válido, a API responde 401.

Endpoints
GET /api/tarefas -> lista todas as tarefas (200)
GET /api/tarefas/{id} -> busca uma tarefa (200, ou 404)
POST /api/tarefas -> cria uma tarefa (201, ou 400)
PUT /api/tarefas/{id} -> atualiza a tarefa inteira (200, 400 ou 404)
PATCH /api/tarefas/{id} -> atualiza só os campos enviados (200, 400 ou 404)
DELETE /api/tarefas/{id} -> exclui a tarefa (204, ou 404)

Regras (no TarefaService da API): o título é obrigatório e tem no máximo 150 caracteres; a data de criação é definida pelo servidor.

Códigos de resposta
200 -> sucesso
201 -> tarefa criada
204 -> tarefa excluída (sem corpo)
400 -> dados inválidos
401 -> token ausente, inválido ou expirado
404 -> tarefa não encontrada

Erros vêm sempre no formato:

json
{ "erro": "O título da tarefa é obrigatório." }
Exemplos

Criar tarefa (POST /api/tarefas):

json
{ "titulo": "Estudar REST", "descricao": "Revisar os verbos HTTP" }

Resposta (201):

json
{
  "id": 4,
  "titulo": "Estudar REST",
  "descricao": "Revisar os verbos HTTP",
  "concluida": false,
  "dataCriacao": "2026-11-21"
}

Atualizar tarefa inteira (PUT /api/tarefas/4):

json
{ "titulo": "Estudar REST", "descricao": "Revisar status codes", "concluida": true }

Atualizar parcialmente (PATCH /api/tarefas/4):

json
{ "concluida": true }
Testando a API

Postman: importe o arquivo todo-api.postman_collection.json e rode primeiro a requisição "Login", que guarda o token sozinha.

PowerShell:

$corpo = '{"usuario":"admin","senha":"123456"}'
$token = (Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/login -ContentType "application/json" -Body $corpo).token
Invoke-RestMethod -Uri http://localhost:8081/api/tarefas -Headers @{ Authorization = "Bearer $token" }
Estrutura do banco de dados

Tabela tarefas:

id (BIGINT, chave primária, auto increment) -> identificador único
titulo (VARCHAR(150)) -> título da tarefa (obrigatório)
descricao (VARCHAR(500)) -> descrição da tarefa
concluida (BOOLEAN) -> status de conclusão
data_criacao (DATE) -> data em que a tarefa foi criada