import type { RelatorioPorMarca } from '../types';
import { EmptyState } from './EmptyState';

export function ReportList({ items }: { items: RelatorioPorMarca[] }) {
  if (!items.length) {
    return <EmptyState text="Relatorio vazio." />;
  }

  return (
    <div className="space-y-2">
      {items.map((item) => (
        <div key={item.marca} className="flex items-center justify-between rounded-md border border-white/10 bg-slate-900 px-3 py-2">
          <span>{item.marca}</span>
          <span className="text-slate-300">{item.quantidade}</span>
        </div>
      ))}
    </div>
  );
}
