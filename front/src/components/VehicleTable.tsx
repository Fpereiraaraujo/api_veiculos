import type { ReactNode } from 'react';
import { Pencil, Eraser, Trash2 } from 'lucide-react';
import type { Veiculo } from '../types';
import { EmptyState } from './EmptyState';

type VehicleTableProps = {
  items: Veiculo[];
  canManage?: boolean;
  onEdit?: (item: Veiculo) => void;
  onPatch?: (item: Veiculo) => void;
  onDelete?: (item: Veiculo) => void;
};

export function VehicleTable({ items, canManage = false, onEdit, onPatch, onDelete }: VehicleTableProps) {
  if (!items.length) {
    return <EmptyState text="Sem resultados para os filtros atuais." />;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <thead className="text-xs uppercase tracking-[0.2em] text-slate-400">
          <tr>
            {['Placa', 'Marca', 'Modelo', 'Ano', 'Cor', 'USD', 'BRL'].map((label) => <th key={label} className="pb-3 pr-4">{label}</th>)}
            {canManage ? <th className="pb-3 pr-4">Acoes</th> : null}
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id} className="border-t border-white/10">
              <td className="py-3 pr-4 font-medium">{item.placa}</td>
              <td className="py-3 pr-4">{item.marca}</td>
              <td className="py-3 pr-4">{item.modelo}</td>
              <td className="py-3 pr-4">{item.ano}</td>
              <td className="py-3 pr-4">{item.cor}</td>
              <td className="py-3 pr-4">{formatMoney(item.precoUsd)}</td>
              <td className="py-3 pr-4">{formatMoney(item.precoBrl)}</td>
              {canManage ? (
                <td className="py-3 pr-0">
                  <div className="flex gap-2">
                    <ActionButton label="PUT" icon={<Pencil size={14} />} onClick={() => onEdit?.(item)} />
                    <ActionButton label="PATCH" icon={<Eraser size={14} />} onClick={() => onPatch?.(item)} />
                    <ActionButton label="DELETE" icon={<Trash2 size={14} />} onClick={() => onDelete?.(item)} danger />
                  </div>
                </td>
              ) : null}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ActionButton({
  label,
  icon,
  onClick,
  danger = false,
}: {
  label: string;
  icon: ReactNode;
  onClick: () => void;
  danger?: boolean;
}) {
  return (
    <button
      type="button"
      className={`inline-flex items-center gap-1 rounded-md px-2 py-1 text-xs ${
        danger ? 'bg-red-500/15 text-red-200 hover:bg-red-500/20' : 'bg-white/10 text-slate-200 hover:bg-white/15'
      }`}
      onClick={onClick}
    >
      {icon}
      {label}
    </button>
  );
}

function formatMoney(value: Veiculo['precoUsd'] | Veiculo['precoBrl']) {
  if (value === null || value === undefined) {
    return '-';
  }
  const number = typeof value === 'number' ? value : Number(value);
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(number);
}
