# Event Microservice

[![CI - Build, Test and Security Analysis](https://github.com/SamuelDinizTenorio/event-microservice/actions/workflows/ci.yml/badge.svg)](https://github.com/SamuelDinizTenorio/event-microservice/actions/workflows/ci.yml)

Este projeto é um microsserviço de gerenciamento de eventos, desenvolvido com Spring Boot. Ele fornece uma API RESTful para criar, listar e gerenciar eventos, bem como para registrar participantes.

O projeto foi construído seguindo princípios de **Arquitetura Limpa (Hexagonal)**, separando o núcleo de negócio (`core`) da infraestrutura (`infrastructure`), tornando o sistema mais testável, flexível e fácil de manter.

---

## ✨ Features

- Criação de novos eventos com validação de dados robusta.
- **Atualização parcial** de eventos existentes.
- Listagem paginada de todos os eventos.
- Listagem paginada de eventos futuros.
- Busca de detalhes de um evento específico.
- Registro de participantes em um evento com validação de vagas e status.
- Listagem paginada de participantes de um evento.
- Cancelamento de eventos (Soft Delete) com validação de estado.
- Atualização automática do status de eventos para "finalizado".
- Tratamento de erro padronizado para toda a API, com mensagens claras.

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA (Hibernate)**: Para persistência de dados.
- **PostgreSQL**: Banco de dados relacional.
- **Flyway**: Para gerenciamento de migrações do banco de dados.
- **Spring Cloud OpenFeign**: Para comunicação com outros microsserviços.
- **Spring Boot Actuator**: Para health checks.
- **Spring Scheduler**: Para execução de tarefas agendadas.
- **Maven**: Para gerenciamento de dependências e build.
- **Docker & Docker Compose**: Para containerização do ambiente de desenvolvimento.
- **Testcontainers**: Para testes de integração com um banco de dados real.
- **JUnit 5 & Mockito**: Para a estrutura de testes de unidade e integração robustos.
- **AssertJ**: Para asserções fluentes e legíveis nos testes.
- **Lombok**: Para reduzir código boilerplate (getters, setters, construtores, etc...).

---

## 🚀 Como Executar

### Pré-requisitos

- **Java 21** ou superior.
- **Maven 3.8** ou superior.
- **Docker** e **Docker Compose** (essencial para o ambiente de desenvolvimento e para rodar os testes).

### 1. Configuração do Ambiente

Antes de iniciar, você precisa configurar suas variáveis de ambiente.

1.  **Copie o arquivo de exemplo:**
    ```sh
    cp .env.example .env
    ```

2.  **Edite o arquivo `.env`:**
    Abra o arquivo `.env` recém-criado e preencha as variáveis com os valores para o seu ambiente.

    ```dotenv
    # .env
    APP_PORT=8080
    DB_PORT=5432
    DB_USER=postgres
    DB_PASSWORD=sua_senha_segura_aqui
    EMAIL_SERVICE_URL=http://localhost:8081
    ```

#### Dependência Externa: Serviço de E-mail

Este projeto depende de um microsserviço externo para o envio de e-mails, cuja URL é definida pela variável `EMAIL_SERVICE_URL`.

- **O repositório para este serviço pode ser encontrado aqui:** [EMAIL-SERVICE](https://github.com/SamuelDinizTenorio/EMAIL-SERVICE).
- Para uma experiência de desenvolvimento completa, você precisará clonar e executar esse serviço também (geralmente na porta 8081).
- Se o serviço de e-mail não estiver disponível, a aplicação registrará um erro no log, mas **não falhará**. A operação principal (como o registro em um evento) será concluída com sucesso.

### 2. Executando com Docker Compose (Recomendado)

Esta é a maneira mais simples de rodar o ambiente completo.

1.  **Construa e inicie os containers em segundo plano:**
    ```sh
    docker-compose up --build -d
    ```
    - O comando `-d` (detached) inicia os containers em segundo plano e libera seu terminal.
    - A aplicação estará disponível em `http://localhost:8080` (ou na porta que você definiu em `APP_PORT`).

2.  **Gerenciando os Serviços:**
    - **Ver logs:** `docker-compose logs -f`
    - **Pausar:** `docker-compose stop`
    - **Retomar:** `docker-compose start`
    - **Parar e remover tudo:** `docker-compose down`

### 3. Executando Localmente (IDE + Banco no Docker)

Esta abordagem é ideal para desenvolvimento e depuração.

1.  **Inicie apenas o banco de dados:**
    ```sh
    docker-compose up -d db
    ```

2.  **Inicie a aplicação pela sua IDE:**
    - Abra o projeto na sua IDE (IntelliJ, VS Code, etc.).
    - Certifique-se de que a IDE carregou as dependências do Maven.
    - Encontre a classe `EventMicroserviceApplication.java` e execute o método `main`.

---

## ⚙️ Processos Automáticos

### Atualização de Status de Eventos

O sistema possui uma tarefa agendada (`EventStatusUpdaterService`) que roda a cada hora para manter a consistência dos dados.

- **Funcionalidade:** A tarefa busca por todos os eventos que estão com o status `ACTIVE` mas cuja data de término (`endDateTime`) já passou.
- **Ação:** Para cada um desses eventos, o status é atualizado para `FINISHED`.
- **Propósito:** Isso garante que o estado dos eventos no banco de dados reflita a realidade sem a necessidade de intervenção manual ou de um endpoint específico para "finalizar" um evento.

---

## 🧪 Testes

A estratégia de testes do projeto é dividida em camadas para garantir cobertura e velocidade.

- **JUnit 5** é o framework principal para a escrita de todos os testes.
- **Mockito** é utilizado para criar "mocks" (objetos falsos) de dependências externas, permitindo isolar a lógica de negócio nos testes de serviço e de controller.
- **AssertJ** fornece uma API rica e fluente para escrever asserções legíveis e poderosas.
- **Testcontainers** é usado nos testes da camada de persistência (`@DataJpaTest`) para iniciar um container Docker do PostgreSQL. Isso garante que as queries e migrações sejam testadas contra um banco de dados real, idêntico ao de produção.

- **Pré-requisito:** Para executar os testes de integração, é necessário ter o **Docker em execução** na sua máquina.

- **Executando os testes:**
  Você pode rodar todos os testes através do Maven com o comando:
  ```sh
  mvn test
  ```
  Ou executar as classes de teste diretamente pela sua IDE.

---

## 🔄 Trocando o Banco de Dados (Exemplo: para MySQL)

A arquitetura do projeto permite a troca do banco de dados. Para isso, você precisará:
1.  Atualizar a dependência do driver no `pom.xml`.
2.  Ajustar o dialeto do Hibernate no `application.yaml`.
3.  Modificar o serviço `db` no `docker-compose.yml`.
4.  **Verificar os Scripts do Flyway**, pois a sintaxe SQL pode precisar de ajustes.

---

## 📋 Endpoints da API

A API retorna respostas de erro padronizadas em caso de falha (ex: 400, 404, 409) com uma mensagem clara no corpo da resposta.

### Eventos

#### `GET /events`
Lista todos os eventos de forma paginada.
- **Parâmetros (Query):** `page`, `size`, `sort`.
- **Resposta (`200 OK`):**
  ```json
  {
    "content": [
      {
        "id": "c1f7a3d0-...",
        "title": "Tech Conference 2024",
        "description": "Um evento sobre tecnologia.",
        "startDateTime": "2024-12-25T14:00:00",
        "endDateTime": "2024-12-25T16:00:00",
        "maxParticipants": 100,
        "registeredParticipants": 42,
        "imageUrl": "http://...",
        "eventUrl": "http://...",
        "location": "São Paulo, SP",
        "isRemote": false,
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 10,
    "total_elements": 1,
    "total_pages": 1,
    "is_last": true
  }
  ```

#### `GET /events/upcoming`
Lista apenas os eventos futuros e ativos de forma paginada.
- **Parâmetros (Query):** Mesmos de `/events`.
- **Resposta (`200 OK`):** Mesma estrutura de `GET /events`.

#### `GET /events/{id}`
Obtém os detalhes completos de um evento específico.
- **Parâmetros (Path):** `id` (UUID).
- **Resposta (`200 OK`):**
  ```json
  {
    "id": "c1f7a3d0-...",
    "title": "Tech Conference 2024",
    "description": "Um evento sobre tecnologia.",
    "startDateTime": "2024-12-25T14:00:00",
    "endDateTime": "2024-12-25T16:00:00",
    "maxParticipants": 100,
    "registeredParticipants": 42,
    "imageUrl": "http://...",
    "eventUrl": "http://...",
    "location": "São Paulo, SP",
    "isRemote": false,
    "status": "ACTIVE"
  }
  ```

#### `POST /events`
Cria um novo evento.
- **Validações:** A API valida regras como `título` e `descrição` não estarem em branco, datas serem no futuro, e consistência entre localização e tipo de evento (remoto/presencial).
- **Corpo (JSON):**
  ```json
  {
    "title": "Tech Conference 2024",
    "description": "Uma descrição com pelo menos 10 caracteres.",
    "startDateTime": "2025-10-20T14:00:00",
    "endDateTime": "2025-10-20T16:00:00",
    "maxParticipants": 100,
    "imageUrl": "http://...",
    "eventUrl": "http://...",
    "location": "São Paulo, SP",
    "isRemote": false
  }
  ```
- **Resposta (`201 Created`):** Mesma estrutura de `GET /events/{id}`.

#### `PATCH /events/{id}`
Atualiza parcialmente um evento existente. Apenas os campos fornecidos no corpo da requisição serão alterados.
- **Parâmetros (Path):** `id` (UUID).
- **Corpo (JSON - Exemplo):**
  ```json
  {
    "title": "Novo Título do Evento",
    "maxParticipants": 150
  }
  ```
- **Resposta (`200 OK`):** Retorna o objeto completo do evento com os dados atualizados.

#### `POST /events/{id}/cancel`
Cancela um evento (Soft Delete), alterando seu status para `CANCELLED`. A operação falhará se o evento já ocorreu ou já foi cancelado.
- **Parâmetros (Path):** `id` (UUID).
- **Corpo:** Vazio.
- **Resposta (`200 OK`):**
  ```json
  {
    "message": "Evento cancelado com sucesso!"
  }
  ```

### Inscrições

#### `POST /events/{eventId}/register`
Registra um participante em um evento. A operação falhará se o evento não estiver ativo, se já estiver lotado, ou se o participante já estiver inscrito.
- **Parâmetros (Path):** `eventId` (UUID).
- **Corpo (JSON):**
  ```json
  {
    "participantEmail": "usuario@exemplo.com"
  }
  ```
- **Resposta (`200 OK`):**
  ```json
  {
    "message": "Inscrição realizada com sucesso!"
  }
  ```

#### `GET /events/{eventId}/participants`
Lista os participantes inscritos em um evento de forma paginada.
- **Parâmetros (Path):** `eventId` (UUID).
- **Parâmetros (Query):** `page`, `size`, `sort`.
- **Resposta (`200 OK`):**
  ```json
  {
    "content": [
      {
        "participantEmail": "usuario1@exemplo.com"
      },
      {
        "participantEmail": "usuario2@exemplo.com"
      }
    ],
    "page": 0,
    "size": 10,
    "total_elements": 2,
    "total_pages": 1,
    "is_last": true
  }
  ```
