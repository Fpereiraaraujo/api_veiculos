import type { FormEvent } from 'react';
import { Loader2, Plus, Save, Wand2 } from 'lucide-react';
import type { Veiculo } from '../types';
import { FormInput } from './FormInput';

export type AdminFormMode = 'create' | 'put' | 'patch';

export type AdminVehicleFormValues = {
  placa: string;
  marca: string;
  modelo: string;
  ano: string;
  cor: string;
  precoUsd: string;
};

type AdminVehicleFormProps = {
  mode: AdminFormMode;
  values: AdminVehicleFormValues;
  selectedVehicle: Veiculo | null;
  loading: boolean;
  message: string;
  error: string;
  onModeChange: (mode: AdminFormMode) => void;
  onChange: (field: keyof AdminVehicleFormValues, value: string) => void;
  onSubmit: (event: FormEvent) => void;
  onReset: () => void;
};

const modes: { value: AdminFormMode; label: string; helper: string }[] = [
  { value: 'create', label: 'POST', helper: 'Adiciona um novo veiculo.' },
  { value: 'put', label: 'PUT', helper: 'Atualiza todos os dados do veiculo selecionado.' },
  { value: 'patch', label: 'PATCH', helper: 'Atualiza parcialmente o veiculo selecionado.' },
];

export function AdminVehicleForm(props: AdminVehicleFormProps) {
  return (
    <section className="rounded-lg border border-white/10 bg-white/5 p-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Acoes de admin</p>
          <h2 className="mt-1 text-lg font-semibold">CRUD de veiculos</h2>
          <p className="mt-1 text-sm text-slate-400">
            Selecione uma operacao para refletir as rotas exclusivas de `ADMIN`.
          </p>
        </div>
        <button type="button" className="rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={props.onReset}>
          Limpar
        </button>
      </div>

      <div className="mt-4 grid gap-2 sm:grid-cols-3">
        {modes.map((item) => (
          <button
            key={item.value}
            type="button"
            onClick={() => props.onModeChange(item.value)}
            className={`rounded-md border px-3 py-3 text-left text-sm ${
              props.mode === item.value ? 'border-cyan-400 bg-cyan-400/10 text-cyan-100' : 'border-white/10 bg-slate-900 text-slate-300'
            }`}
          >
            <p className="font-medium">{item.label}</p>
            <p className="mt-1 text-xs text-slate-400">{item.helper}</p>
          </button>
        ))}
      </div>

      <form className="mt-4 grid gap-3" onSubmit={props.onSubmit}>
        {props.mode !== 'create' ? (
          <div className="rounded-md border border-white/10 bg-slate-900 px-3 py-2 text-xs text-slate-400">
            {props.selectedVehicle ? `Veiculo selecionado: ${props.selectedVehicle.placa} - ${props.selectedVehicle.marca} ${props.selectedVehicle.modelo}` : 'Escolha um veiculo na tabela para usar PUT ou PATCH.'}
          </div>
        ) : null}

        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          <FormInput label="Placa" value={props.values.placa} onChange={(value) => props.onChange('placa', value)} placeholder="ABC1D23" />
          <FormInput label="Marca" value={props.values.marca} onChange={(value) => props.onChange('marca', value)} />
          <FormInput label="Modelo" value={props.values.modelo} onChange={(value) => props.onChange('modelo', value)} />
          <FormInput label="Ano" value={props.values.ano} onChange={(value) => props.onChange('ano', value)} type="number" />
          <FormInput label="Cor" value={props.values.cor} onChange={(value) => props.onChange('cor', value)} />
          <FormInput label="Preco USD" value={props.values.precoUsd} onChange={(value) => props.onChange('precoUsd', value)} type="number" />
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            type="submit"
            disabled={props.loading}
            className="inline-flex items-center gap-2 rounded-md bg-cyan-500 px-4 py-2 text-sm font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-70"
          >
            {props.loading ? <Loader2 size={16} className="animate-spin" /> : iconForMode(props.mode)}
            {labelForMode(props.mode)}
          </button>
          <span className="text-xs text-slate-400">
            {props.mode === 'create' ? 'Use POST para cadastro.' : props.mode === 'put' ? 'Use PUT para substituicao completa.' : 'Use PATCH para alteracao parcial.'}
          </span>
        </div>

        {props.message ? <p className="rounded-md border border-emerald-500/20 bg-emerald-500/10 p-3 text-sm text-emerald-100">{props.message}</p> : null}
        {props.error ? <p className="rounded-md border border-red-500/20 bg-red-500/10 p-3 text-sm text-red-200">{props.error}</p> : null}
      </form>
    </section>
  );
}

function labelForMode(mode: AdminFormMode) {
  if (mode === 'create') return 'POST /veiculos';
  if (mode === 'put') return 'PUT /veiculos/{id}';
  return 'PATCH /veiculos/{id}';
}

function iconForMode(mode: AdminFormMode) {
  if (mode === 'create') return <Plus size={16} />;
  if (mode === 'put') return <Save size={16} />;
  return <Wand2 size={16} />;
}
