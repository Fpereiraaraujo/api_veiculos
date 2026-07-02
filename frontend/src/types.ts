export type AuthResponse = {
  token: string;
  tokenType: string;
};

export type Veiculo = {
  id: string;
  placa: string;
  marca: string;
  modelo: string;
  ano: number;
  cor: string;
  precoUsd: string | number;
  precoBrl?: string | number | null;
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type PageData<T> = {
  content: T[];
  page: {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
  };
};

export type RelatorioPorMarca = {
  marca: string;
  quantidade: number;
};
