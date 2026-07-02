import type { AuthResponse, PageData, RelatorioPorMarca, Veiculo, VeiculoPatchPayload, VeiculoPayload } from './types';

const API_BASE = '/api';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, init);
  if (!response.ok) {
    throw new Error(await extractMessage(response));
  }
  return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
}

async function extractMessage(response: Response) {
  try {
    const body = await response.json();
    return body.message ?? body.error ?? 'Falha na requisicao';
  } catch {
    return 'Falha na requisicao';
  }
}

export async function login(username: string, password: string) {
  return request<AuthResponse>('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
}

export async function listarVeiculos(token: string, query: URLSearchParams) {
  return request<PageData<Veiculo>>(`/veiculos?${query.toString()}`, {
    headers: authHeaders(token),
  });
}

export async function listarRelatorio(token: string) {
  return request<RelatorioPorMarca[]>('/veiculos/relatorios/por-marca', {
    headers: authHeaders(token),
  });
}

export async function cadastrarVeiculo(token: string, payload: VeiculoPayload) {
  return request<Veiculo>('/veiculos', {
    method: 'POST',
    headers: jsonHeaders(token),
    body: JSON.stringify(payload),
  });
}

export async function atualizarVeiculo(token: string, id: string, payload: VeiculoPayload) {
  return request<Veiculo>(`/veiculos/${id}`, {
    method: 'PUT',
    headers: jsonHeaders(token),
    body: JSON.stringify(payload),
  });
}

export async function atualizarParcialVeiculo(token: string, id: string, payload: VeiculoPatchPayload) {
  return request<Veiculo>(`/veiculos/${id}`, {
    method: 'PATCH',
    headers: jsonHeaders(token),
    body: JSON.stringify(payload),
  });
}

export async function removerVeiculo(token: string, id: string) {
  return request<void>(`/veiculos/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
}

function authHeaders(token: string) {
  return { Authorization: `Bearer ${token}` };
}

function jsonHeaders(token: string) {
  return {
    ...authHeaders(token),
    'Content-Type': 'application/json',
  };
}
