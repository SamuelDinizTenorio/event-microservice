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
- **Spring Cloud OpenFeign**: Para comunicação com outros microsserviços (ex: serviço de e-mail).
- **Spring Boot Actuator**: Para health checks.
- **Maven**: Para gerenciamento de dependências e build.
- **Docker & Docker Compose**: Para containerização e orquestração do ambiente de desenvolvimento.
- **Lombok**: Para reduzir código boilerplate.

---

## 🚀 Como Executar

### Pré-requisitos

- **Java 21** ou superior.
- **Maven 3.8** ou superior.
- **Docker** e **Docker Compose**.

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

- **O repositório para este serviço pode ser encontrado aqui:** [seu-servico-de-email](https://github.com/seu-usuario/seu-servico-de-email) (substitua pela URL real).
- Para uma experiência de desenvolvimento completa, você precisará clonar e executar esse serviço também (geralmente na porta 8081).
- Se o serviço de e-mail não estiver disponível, a aplicação registrará um erro no log, mas **não falhará**. A operação principal (como o registro em um evento) será concluída com sucesso.

### 2. Executando com Docker Compose (Recomendado)

Esta é a maneira mais simples de rodar o ambiente completo.

1.  **Construa e inicie os containers em segundo plano:**
    No terminal, na raiz do projeto, execute:
    ```sh
    docker-compose up --build -d
    ```
    - O comando `-d` (detached) inicia os containers em segundo plano e libera seu terminal.
    - A aplicação estará disponível em `http://localhost:8080` (ou na porta que você definiu em `APP_PORT`).

2.  **Gerenciando os Serviços:**
    - **Ver logs:** Para acompanhar os logs de todos os serviços em tempo real, use:
      ```sh
      docker-compose logs -f
      ```
    - **Pausar os serviços:** Se quiser parar os containers sem removê-los (preservando o estado), use:
      ```sh
      docker-compose stop
      ```
    - **Retomar os serviços:** Para reiniciar os containers que foram parados, use:
      ```sh
      docker-compose start
      ```
    - **Parar e remover tudo:** Para parar e remover os containers, a rede e os volumes anônimos, use:
      ```sh
      docker-compose down
      ```

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

## 🔄 Trocando o Banco de Dados (Exemplo: para MySQL)

A arquitetura do projeto permite a troca do banco de dados. Aqui está um guia de como trocar de PostgreSQL para MySQL.

1.  **Atualize as Dependências no `pom.xml`:**
    - Remova a dependência do driver do PostgreSQL e adicione a do MySQL.

2.  **Ajuste o Dialeto do Hibernate no `application.yaml`:**
    - Altere `spring.jpa.properties.hibernate.dialect` para `org.hibernate.dialect.MySQLDialect`.

3.  **Modifique o Serviço `db` no `docker-compose.yml`:**
    - Altere a `image` para `mysql:8.0`, ajuste as `environment` e mude a `ports` para `3306`.

4.  **Verifique os Scripts do Flyway:**
    - **Aviso Importante:** Os scripts SQL em `src/main/resources/db/migration` podem não ser compatíveis com MySQL. Você provavelmente precisará criar novas versões dos scripts.

---

## 📋 Endpoints da API

- `GET /events`: Lista todos os eventos de forma paginada.
- `GET /events/upcoming`: Lista todos os eventos futuros de forma paginada.
- `GET /events/{id}`: Obtém os detalhes de um evento específico.
- `POST /events`: Cria um novo evento.
- `POST /events/{eventId}/register`: Registra um participante em um evento.
- `GET /events/{eventId}/participants`: Lista os participantes de um evento de forma paginada.
