# --- Estágio 1: Build da Aplicação com Maven ---
# Usamos uma imagem oficial do Maven com Java 21 para compilar o projeto.
FROM maven:3.9-eclipse-temurin-21 AS build
  
# Define o diretório de trabalho dentro do container.
WORKDIR /app
  
# Copia o pom.xml e o wrapper do Maven para aproveitar o cache de camadas do Docker.
# Se o pom.xml não mudar, as dependências não serão baixadas novamente.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN mvn dependency:go-offline
  
# Copia o resto do código-fonte da aplicação.
COPY src ./src
  
# Compila o projeto e gera o JAR, pulando os testes.
RUN mvn package -DskipTests
  
  
# --- Estágio 2: Criação da Imagem Final de Runtime ---
# Usamos uma imagem leve, contendo apenas o Java 21 Runtime.
FROM eclipse-temurin:21-jre-alpine
  
# Define o diretório de trabalho.
WORKDIR /app
  
# Instala o curl, necessário para o healthcheck
RUN apk add --no-cache curl
  
# Cria o diretório de logs e o usuário não-root
RUN mkdir logs && \
addgroup -S spring && \
adduser -S spring -G spring && \
chown -R spring:spring /app
  
# Muda para o usuário não-root
USER spring:spring
  
# Copia o arquivo .jar gerado no estágio de build para a imagem final.
# O nome do JAR é genérico para funcionar com diferentes versões.
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar
  
# Expõe a porta em que a aplicação Spring Boot roda.
EXPOSE 8080
  
# Comando para executar a aplicação quando o container iniciar.
ENTRYPOINT ["java", "-jar", "app.jar"]
