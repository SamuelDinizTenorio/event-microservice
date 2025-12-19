# Event Microservice

[![CI - Build, Test and Security Analysis](https://github.com/SamuelDinizTenorio/event-microservice/actions/workflows/ci.yml/badge.svg)](https://github.com/SamuelDinizTenorio/event-microservice/actions/workflows/ci.yml)

Este projeto é um microsserviço de gerenciamento de eventos, desenvolvido com Spring Boot. Ele fornece uma API RESTful para criar, listar e gerenciar eventos, bem como para registrar participantes.

O projeto foi construído seguindo princípios de **Arquitetura Limpa (Hexagonal)**, separando o núcleo de negócio (`core`) da infraestrutura (`infrastructure`), tornando o sistema mais testável, flexível e fácil de manter.

---

## ✨ Features

- Criação de novos eventos com validação de dados.
- Listagem paginada de todos os eventos.
- Listagem paginada de eventos futuros.
- Busca de detalhes de um evento específico.
- Registro de participantes em um evento.
- Listagem paginada de participantes de um evento.
- Tratamento de erro padronizado para toda a API.

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA (Hibernate)**: Para persistência de dados.
- **PostgreSQL**: Banco de dados relacional.
- **Flyway**: Para gerenciamento de migrações do banco de dados.
- **Spring Cloud OpenFeign**: Para comunicação com outros microsserviços.
- **Spring Boot Actuator**: Para health checks.
- **Maven**: Para gerenciamento de dependências e build.
- **Docker & Docker Compose**: Para containerização do ambiente de desenvolvimento.
- **Testcontainers**: Para testes de integração com um banco de dados real.
- **Lombok**: Para reduzir código boilerplate.

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
    - A aplicação irá iniciar e se conectar ao banco de dados que está rodando no container Docker.
    
---

## 🧪 Testes

O projeto utiliza **Testcontainers** para executar os testes de integração da camada de persistência (`@DataJpaTest`) contra um banco de dados PostgreSQL real, garantindo que os testes sejam fiéis ao ambiente de produção.

- **Pré-requisito:** Para executar os testes, é necessário ter o **Docker em execução** na sua máquina.

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

- `GET /events`: Lista todos os eventos de forma paginada.
- `GET /events/upcoming`: Lista todos os eventos futuros de forma paginada.
- `GET /events/{id}`: Obtém os detalhes de um evento específico.
- `POST /events`: Cria um novo evento.
- `POST /events/{eventId}/register`: Registra um participante em um evento.
- `GET /events/{eventId}/participants`: Lista os participantes de um evento de forma paginada.
