================================================================================
VEICULOS API - Desafio Tecnico Tinnova
================================================================================

1. SOBRE O PROJETO
API REST para gerenciamento de veiculos, com controle de acesso baseado em
papeis (USER/ADMIN), preco armazenado em dolar (cotacao obtida em tempo real,
com fallback e cache), e arquitetura hexagonal (Ports and Adapters).

Este README e atualizado a cada etapa de desenvolvimento.

--------------------------------------------------------------------------------
2. STACK E DECISOES DE ENGENHARIA (etapa 1 - setup)
--------------------------------------------------------------------------------

- Java 21 + Spring Boot 3.4.2
  -> Optei por 3.4.x em vez do recem-lancado Spring Boot 4 (GA nov/2025).
     O Boot 4 renomeou starters (web -> webmvc) e o ecossistema de libs que
     vamos usar aqui (resilience4j-spring-boot3, springdoc, testcontainers)
     ainda esta estabilizando compatibilidade total. Para um desafio com
     prazo de avaliacao, preferi estabilidade de build a usar a versao mais
     nova. Dá pra migrar para o Boot 4 depois, se fizer sentido.

- Arquitetura Hexagonal:
    domain/            -> modelo de negocio puro, sem anotacao de framework
    application/port/  -> contratos (in = entrada, out = saida)
    application/usecase/ -> regras de negocio (implementa as portas de entrada)
    framework/in/       -> controllers, DTOs, exception handler (adapters de entrada)
    framework/out/      -> JPA, clients HTTP externos (adapters de saida)
    infrastructure/     -> configuracao tecnica (security, cache, openapi)

- Persistencia: Spring Data JPA + PostgreSQL. Pool de conexoes via HikariCP
  (auto-configurado pelo Spring Boot, tuning explicito em application.yml).

- Seguranca: Spring Security + JWT (lib jjwt). Estrutura da cadeia de filtros
  ja criada; a logica real de autenticacao/autorizacao fica para uma etapa
  dedicada (ver roadmap). Ate la os endpoints ficam liberados de proposito,
  pra nao travar o desenvolvimento de persistencia/regras de negocio.

- Cache em 2 camadas para a cotacao do dolar:
    L1 Caffeine (in-memory, baixa latencia) - cache primario via @Cacheable
    L2 Redis (distribuido) - usado pelo client de cambio como camada extra,
    relevante se a API escalar horizontalmente
  Ambos ja configurados em CacheConfig.

- Resiliencia: Resilience4j (circuit breaker + retry + timeout) protegendo
  as chamadas as APIs externas de cambio (AwesomeAPI / Frankfurter como
  fallback). Configuracao base ja em application.yml; uso real (anotacoes
  @CircuitBreaker/@Retry) entra na etapa de integracao com cambio.

- Observabilidade: Spring Actuator + Micrometer (+ Prometheus registry),
  com endpoints de health/metrics/circuitbreakers expostos -- base para
  coletar metricas depois, como combinado.

- Documentacao: springdoc-openapi (Swagger UI em /swagger-ui.html).

- Testes: JUnit 5 + Mockito (via spring-boot-starter-test) para unidade,
  Testcontainers (Postgres real + Redis real) para integracao. Classe base
  AbstractIntegrationTest ja criada e reutilizavel nas proximas etapas.

- Docker: Dockerfile multi-stage + docker-compose (Postgres + Redis + API).

--------------------------------------------------------------------------------
3. COMO EXECUTAR
--------------------------------------------------------------------------------

Pre-requisito: Docker e Docker Compose.

    docker-compose up --build -d

- API: http://localhost:8080/veiculos
- Swagger: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health

Para rodar localmente sem Docker (precisa de Postgres e Redis locais ou via
`docker-compose up db redis -d`):

    ./mvnw spring-boot:run

Para rodar os testes (sobe containers Testcontainers automaticamente,
precisa do Docker rodando):

    ./mvnw test

--------------------------------------------------------------------------------
4. ROADMAP DE DESENVOLVIMENTO
--------------------------------------------------------------------------------

[x] Etapa 1 - Setup: dependencias, Docker, infra base, skeleton hexagonal,
    GlobalExceptionHandler, cache config, security skeleton, testes base.

[ ] Etapa 2 - Dominio + Persistencia: VeiculoEntity (JPA), VeiculoRepositoryAdapter,
    VeiculoController com GET (listagem com filtros combinados, paginacao e
    ordenacao, GET por id).

[ ] Etapa 3 - Regras de negocio: VeiculoUseCase completo (CRUD, duplicidade
    de placa, soft delete, PATCH parcial), relatorio por marca.

[ ] Etapa 4 - Seguranca: endpoint de autenticacao, JwtService real, roles
    USER/ADMIN aplicadas nos endpoints conforme especificacao.

[ ] Etapa 5 - Cambio: clients AwesomeAPI + Frankfurter (fallback) com
    Resilience4j e cache Caffeine/Redis aplicados de verdade.

[ ] Etapa 6 - Testes finais ate a cobertura "Senior" (>= 75%, incluindo
    integracao e seguranca) + Swagger refinado + README final com instrucoes
    completas de execucao e testes.

================================================================================
Desenvolvido por Fernando Pereira
================================================================================
