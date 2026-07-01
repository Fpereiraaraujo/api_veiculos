# Veiculos API - Desafio Tinnova

API REST para gerenciamento de veiculos usando Java 21, Spring Boot 3.4.2 e arquitetura hexagonal.

## Recursos Entregues

- CRUD de veiculos com soft delete.
- Listagem com filtros por `marca`, `ano`, `cor`, `minPreco`, `maxPreco`, paginacao e ordenacao.
- Relatorio de quantidade de veiculos por marca.
- Autenticacao JWT com perfis `USER` e `ADMIN`.
- `USER`: leitura de veiculos.
- `ADMIN`: cadastro, atualizacao, remocao e leitura.
- Preco persistido em dolar (`precoUsd`) e retornado tambem em real (`precoBrl`).
- Cotacao USD-BRL pela AwesomeAPI, com fallback para Frankfurter.
- Cache Redis para cotacao.
- Retry e circuit breaker com Resilience4j no adapter de cambio.
- Swagger UI com esquema Bearer JWT.
- Tratamento padronizado de erros.
- Testes unitarios de regra de negocio e testes MVC de seguranca/erros.

## Arquitetura

```text
domain/                 modelo e exceptions de negocio
application/port/in      portas de entrada
application/port/out     portas de saida
application/usecase      casos de uso e regras de aplicacao
framework/in             controllers, DTOs, mappers, exception handler
framework/out            adapters JPA e clients HTTP externos
infrastructure           configuracoes tecnicas de security, cache e OpenAPI
```

## Executando Com Docker

```bash
docker compose up --build -d
```

Servicos:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Usuarios De Teste

```text
admin / admin123
user  / user123
```

Login:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Use o token retornado no header:

```text
Authorization: Bearer <token>
```

## Endpoints

```text
POST   /auth/login
GET    /veiculos
GET    /veiculos/{id}
POST   /veiculos
PUT    /veiculos/{id}
PATCH  /veiculos/{id}
DELETE /veiculos/{id}
GET    /veiculos/relatorios/por-marca
```

Exemplo de cadastro:

```bash
curl -X POST http://localhost:8080/veiculos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-admin>" \
  -d '{
    "placa": "ABC1D23",
    "marca": "Honda",
    "modelo": "Civic",
    "ano": 2021,
    "cor": "Preto",
    "precoUsd": 21000.00
  }'
```

Exemplo de listagem filtrada:

```bash
curl "http://localhost:8080/veiculos?marca=Honda&cor=Preto&page=0&size=20&sort=ano,desc" \
  -H "Authorization: Bearer <token>"
```

## Testes

Com Maven local:

```bash
mvn test
```

Sem Maven local, usando Docker:

```bash
docker run --rm -v ${PWD}:/app -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

Observacao: testes baseados em Testcontainers precisam enxergar o socket Docker. Ao rodar Maven dentro de um container sem `/var/run/docker.sock`, esses testes sao pulados pela configuracao `disabledWithoutDocker`.

## Cache De Cotacao

A cotacao fica em Redis na chave:

```text
dollarQuote::USD_BRL
```

O TTL e configurado em:

```yaml
app:
  currency:
    cache-ttl-minutes: 5
```
