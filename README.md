# Veiculos API

Repositorio organizado em duas partes:

- `back/`: API Spring Boot, dominio, testes e Dockerfile do backend.
- `front/`: interface React + TypeScript + Tailwind para visualizacao.

## Como rodar

Backend e infraestrutura:

```bash
docker compose up --build -d
```

Frontend:

```bash
cd front
npm install
npm run dev
```

Para detalhes da API, endpoints, variaveis de ambiente e exemplos de uso, veja [`back/README.md`](back/README.md).
