import type { Dispatch, SetStateAction } from 'react';
import { Search } from 'lucide-react';
import { FormInput } from './FormInput';

export type Filters = {
  marca: string;
  ano: string;
  cor: string;
  minPreco: string;
  maxPreco: string;
  sort: string;
  size: string;
};

type FiltersPanelProps = {
  filters: Filters;
  setFilters: Dispatch<SetStateAction<Filters>>;
  setPage: Dispatch<SetStateAction<number>>;
  helperText?: string;
};

export function FiltersPanel({ filters, setFilters, setPage, helperText }: FiltersPanelProps) {
  return (
    <div className="grid gap-3">
      {helperText ? <p className="text-xs text-slate-400">{helperText}</p> : null}
      <FormInput label="Marca" value={filters.marca} onChange={(value) => update(filters, setFilters, 'marca', value, setPage)} />
      <FormInput label="Ano" value={filters.ano} onChange={(value) => update(filters, setFilters, 'ano', value, setPage)} />
      <FormInput label="Cor" value={filters.cor} onChange={(value) => update(filters, setFilters, 'cor', value, setPage)} />
      <div className="grid grid-cols-2 gap-3">
        <FormInput label="Min preco" value={filters.minPreco} onChange={(value) => update(filters, setFilters, 'minPreco', value, setPage)} />
        <FormInput label="Max preco" value={filters.maxPreco} onChange={(value) => update(filters, setFilters, 'maxPreco', value, setPage)} />
      </div>
      <FormInput label="Sort" value={filters.sort} onChange={(value) => update(filters, setFilters, 'sort', value, setPage)} />
      <FormInput label="Size" value={filters.size} onChange={(value) => update(filters, setFilters, 'size', value, setPage)} />
      <button
        className="inline-flex items-center justify-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15"
        onClick={() => setPage(0)}
        type="button"
      >
        <Search size={16} /> Aplicar ao buscar
      </button>
    </div>
  );
}

function update<K extends keyof Filters>(
  filters: Filters,
  setFilters: Dispatch<SetStateAction<Filters>>,
  key: K,
  value: string,
  setPage: Dispatch<SetStateAction<number>>,
) {
  setFilters({ ...filters, [key]: value } as Filters);
  setPage(0);
}
