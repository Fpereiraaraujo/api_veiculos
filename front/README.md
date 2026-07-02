# Veiculos Frontend

Interface React + TypeScript + Tailwind para visualizar a API de veiculos.

## Requisitos

- Node.js 20+
- Backend rodando em `http://localhost:8080`

## Como rodar

```bash
cd front
npm install
npm run dev
```

O Vite faz proxy de `/api` para `http://localhost:8080`, entao o backend precisa estar ativo para o login e as consultas funcionarem.

## Acesso

Usuarios de desenvolvimento:

```text
admin / admin123
user  / user123
```

## Scripts

- `npm run dev`: sobe o ambiente de desenvolvimento
- `npm run build`: gera a build de producao
- `npm run preview`: serve a build localmente

## O que o front mostra

- Tela de login
- Listagem paginada de veiculos
- Filtros por marca, ano, cor e faixa de preco
- Ordenacao
- Relatorio por marca
- Logout e reutilizacao do token em `localStorage`
