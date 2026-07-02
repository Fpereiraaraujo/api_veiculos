# Veiculos API - Desafio Tinnova

API REST para gerenciamento de veiculos usando Java 21, Spring Boot 3.4.2 e arquitetura hexagonal.

## Recursos

- CRUD de veiculos com soft delete.
- Listagem com filtros por `marca`, `ano`, `cor`, `minPreco`, `maxPreco`, paginacao e ordenacao.
- Validacao de `page`, limite maximo de `size=100` e whitelist de campos para `sort`.
- Relatorio de quantidade de veiculos por marca.
- Autenticacao JWT com perfis `USER` e `ADMIN`.
- `USER`: leitura de veiculos.
- `ADMIN`: cadastro, atualizacao, remocao e leitura.
- Preco persistido em dolar (`precoUsd`) e retornado tambem em real (`precoBrl`).
- Cotacao USD-BRL pela AwesomeAPI, com fallback para Frankfurter.
- Cache Redis para cotacao, com uso do cache quando APIs externas falham.
- Retry e circuit breaker com Resilience4j no adapter de cambio.
- Swagger UI com esquema Bearer JWT.
- Actuator com health, liveness, readiness, metrics, prometheus e circuit breakers.
- Tratamento padronizado de erros e logs para erros de negocio/validacao.
- Testes unitarios, MVC de seguranca/validacao e integracao com Testcontainers.

## Arquitetura

```text
domain/                 modelo, exceptions e validacoes de negocio
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
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Frontend

O front fica em [`frontend`](./frontend) e usa React + TypeScript + Tailwind.

```bash
cd frontend
npm install
npm run dev
```

O Vite faz proxy de `/api` para `http://localhost:8080`, entao o backend precisa estar rodando junto.

## Variaveis De Ambiente

| Variavel | Padrao dev | Descricao |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/veiculos_db` | URL do PostgreSQL |
| `DB_USERNAME` | `veiculos_admin` | Usuario do banco |
| `DB_PASSWORD` | `veiculos_pass` | Senha do banco |
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `JWT_SECRET` | segredo de dev | Segredo HMAC do JWT |
| `DEV_ADMIN_USERNAME` | `admin` | Usuario admin de desenvolvimento |
| `DEV_ADMIN_PASSWORD` | `admin123` | Senha admin de desenvolvimento |
| `DEV_USER_USERNAME` | `user` | Usuario comum de desenvolvimento |
| `DEV_USER_PASSWORD` | `user123` | Senha do usuario comum de desenvolvimento |

Os usuarios em memoria sao apenas para desenvolvimento/teste. Em producao, use `SPRING_PROFILES_ACTIVE=prod` e configure um `JWT_SECRET` forte; a aplicacao falha ao subir se o profile `prod` usar o segredo padrao.

## Login

Usuarios de desenvolvimento:

```text
admin / admin123
user  / user123
```

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

Campos permitidos para `sort`:

```text
placa, marca, modelo, ano, cor, precoUsd, createdAt, updatedAt
```

## Exemplos Curl

Login:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)
```

Cadastrar:

```bash
curl -X POST http://localhost:8080/veiculos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "placa": "ABC1D23",
    "marca": "Honda",
    "modelo": "Civic",
    "ano": 2021,
    "cor": "Preto",
    "precoUsd": 21000.00
  }'
```

Listar com filtros, paginacao e ordenacao:

```bash
curl "http://localhost:8080/veiculos?marca=Honda&ano=2021&cor=Preto&minPreco=10000&maxPreco=30000&page=0&size=20&sort=ano,desc" \
  -H "Authorization: Bearer $TOKEN"
```

Buscar por id:

```bash
curl http://localhost:8080/veiculos/{id} \
  -H "Authorization: Bearer $TOKEN"
```

Atualizar completo:

```bash
curl -X PUT http://localhost:8080/veiculos/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "placa": "ABC1D23",
    "marca": "Honda",
    "modelo": "Accord",
    "ano": 2022,
    "cor": "Prata",
    "precoUsd": 25000.00
  }'
```

Atualizar parcial:

```bash
curl -X PATCH http://localhost:8080/veiculos/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"cor":"Azul","precoUsd":23000.00}'
```

Remover:

```bash
curl -X DELETE http://localhost:8080/veiculos/{id} \
  -H "Authorization: Bearer $TOKEN"
```

Relatorio por marca:

```bash
curl http://localhost:8080/veiculos/relatorios/por-marca \
  -H "Authorization: Bearer $TOKEN"
```

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

## Testes

Com Maven local:

```bash
mvn test
```

Sem Maven local, usando Docker:

```bash
docker run --rm -v ${PWD}:/app -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

Testes de integracao usam Testcontainers e precisam enxergar o socket Docker. Ao rodar Maven dentro de um container sem `/var/run/docker.sock`, esses testes sao pulados pela configuracao `disabledWithoutDocker`.
