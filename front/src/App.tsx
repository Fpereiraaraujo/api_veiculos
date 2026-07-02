import { useEffect, useMemo, useState } from 'react';
import type { Dispatch, FormEvent, SetStateAction } from 'react';
import { ArrowRight, BarChart3, CarFront, Crown, Loader2, LogOut, RefreshCw, Search, ShieldCheck } from 'lucide-react';
import { listarRelatorio, listarVeiculos, login } from './api';
import type { RelatorioPorMarca, Veiculo } from './types';

type Filters = {
  marca: string;
  ano: string;
  cor: string;
  minPreco: string;
  maxPreco: string;
  sort: string;
  size: string;
};

const defaultFilters: Filters = {
  marca: '',
  ano: '',
  cor: '',
  minPreco: '',
  maxPreco: '',
  sort: 'marca,asc',
  size: '20',
};

type AccessRole = 'ADMIN' | 'USER';

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('veiculos.token') ?? '');
  const [role, setRole] = useState<AccessRole | ''>(() => getStoredRole() || getRoleFromToken(localStorage.getItem('veiculos.token') ?? ''));
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [filters, setFilters] = useState(defaultFilters);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [authLoading, setAuthLoading] = useState(false);
  const [error, setError] = useState('');
  const [items, setItems] = useState<Veiculo[]>([]);
  const [report, setReport] = useState<RelatorioPorMarca[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loggedIn = Boolean(token);
  const query = useMemo(() => buildQuery(filters, page), [filters, page]);

  useEffect(() => {
    if (token && !role) {
      const resolvedRole = getRoleFromToken(token);
      setRole(resolvedRole);
      localStorage.setItem('veiculos.role', resolvedRole);
    }
  }, [token, role]);

  useEffect(() => {
    if (loggedIn) {
      void loadData();
    }
  }, [loggedIn, query]);

  async function handleLogin(event: FormEvent) {
    event.preventDefault();
    setAuthLoading(true);
    setError('');
    try {
      const result = await login(username, password);
      const resolvedRole = getRoleFromToken(result.token);
      localStorage.setItem('veiculos.token', result.token);
      localStorage.setItem('veiculos.role', resolvedRole);
      setToken(result.token);
      setRole(resolvedRole);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao autenticar');
    } finally {
      setAuthLoading(false);
    }
  }

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [pageResult, reportResult] = await Promise.all([
        listarVeiculos(token, query),
        listarRelatorio(token),
      ]);
      setItems(pageResult.content);
      setTotalElements(pageResult.page.totalElements);
      setTotalPages(pageResult.page.totalPages);
      setReport(reportResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao carregar dados');
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem('veiculos.token');
    localStorage.removeItem('veiculos.role');
    setToken('');
    setRole('');
    setItems([]);
    setReport([]);
  }

  if (!loggedIn) {
    return <LoginScreen username={username} password={password} loading={authLoading} error={error}
      onUsername={setUsername} onPassword={setPassword} onSubmit={handleLogin} />;
  }

  return role === 'ADMIN' ? (
    <AdminDashboard
      loading={loading}
      error={error}
      filters={filters}
      setFilters={setFilters}
      page={page}
      setPage={setPage}
      totalElements={totalElements}
      totalPages={totalPages}
      items={items}
      report={report}
      onRefresh={loadData}
      onLogout={logout}
    />
  ) : (
    <UserDashboard
      loading={loading}
      error={error}
      filters={filters}
      setFilters={setFilters}
      page={page}
      setPage={setPage}
      totalElements={totalElements}
      totalPages={totalPages}
      items={items}
      onRefresh={loadData}
      onLogout={logout}
    />
  );
}

function AdminDashboard(props: {
  loading: boolean;
  error: string;
  filters: Filters;
  setFilters: Dispatch<SetStateAction<Filters>>;
  page: number;
  setPage: Dispatch<SetStateAction<number>>;
  totalElements: number;
  totalPages: number;
  items: Veiculo[];
  report: RelatorioPorMarca[];
  onRefresh: () => void;
  onLogout: () => void;
}) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col gap-6 px-4 py-6 lg:px-8">
        <header className="flex flex-wrap items-center justify-between gap-4 border-b border-white/10 pb-4">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Veiculos API</p>
            <h1 className="flex items-center gap-2 text-2xl font-semibold">
              <Crown size={20} className="text-amber-300" />
              Painel administrativo
            </h1>
          </div>
          <div className="flex items-center gap-2">
            <button className="inline-flex items-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={props.onRefresh}>
              <RefreshCw size={16} /> Atualizar
            </button>
            <button className="inline-flex items-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={props.onLogout}>
              <LogOut size={16} /> Sair
            </button>
          </div>
        </header>

        <section className="grid gap-4 lg:grid-cols-[320px_1fr]">
          <aside className="rounded-lg border border-white/10 bg-white/5 p-4">
            <h2 className="mb-3 text-sm font-medium text-slate-300">Filtros</h2>
            <FiltersPanel filters={props.filters} setFilters={props.setFilters} page={props.page} setPage={props.setPage} />
          </aside>
          <main className="grid gap-4">
            <Summary cards={[
              { label: 'Veiculos', value: String(props.totalElements) },
              { label: 'Pagina', value: `${props.page + 1}/${Math.max(props.totalPages, 1)}` },
              { label: 'Token', value: 'ativo' },
              { label: 'Perfil', value: 'ADMIN' },
            ]} />
            <section className="grid gap-4 xl:grid-cols-[1fr_320px]">
              <div className="rounded-lg border border-white/10 bg-white/5 p-4">
                <div className="mb-3 flex items-center justify-between">
                  <h2 className="flex items-center gap-2 text-sm font-medium text-slate-300"><CarFront size={16} /> Veiculos</h2>
                  {props.loading && <Loader2 className="animate-spin text-slate-400" size={16} />}
                </div>
                <VehicleTable items={props.items} />
              </div>
              <div className="rounded-lg border border-white/10 bg-white/5 p-4">
                <h2 className="mb-3 flex items-center gap-2 text-sm font-medium text-slate-300"><BarChart3 size={16} /> Relatorio</h2>
                <ReportList items={props.report} />
              </div>
            </section>
            <footer className="flex items-center justify-between text-xs text-slate-400">
              <span>GET /veiculos</span>
              <span>{props.error || 'pronto'}</span>
            </footer>
          </main>
        </section>
      </div>
    </div>
  );
}

function UserDashboard(props: {
  loading: boolean;
  error: string;
  filters: Filters;
  setFilters: Dispatch<SetStateAction<Filters>>;
  page: number;
  setPage: Dispatch<SetStateAction<number>>;
  totalElements: number;
  totalPages: number;
  items: Veiculo[];
  onRefresh: () => void;
  onLogout: () => void;
}) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col gap-6 px-4 py-6 lg:px-8">
        <header className="flex flex-wrap items-center justify-between gap-4 border-b border-white/10 pb-4">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Veiculos API</p>
            <h1 className="flex items-center gap-2 text-2xl font-semibold">
              <ShieldCheck size={20} className="text-cyan-300" />
              Painel de consulta
            </h1>
          </div>
          <div className="flex items-center gap-2">
            <button className="inline-flex items-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={props.onRefresh}>
              <RefreshCw size={16} /> Atualizar
            </button>
            <button className="inline-flex items-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={props.onLogout}>
              <LogOut size={16} /> Sair
            </button>
          </div>
        </header>

        <section className="grid gap-4 lg:grid-cols-[320px_1fr]">
          <aside className="rounded-lg border border-white/10 bg-white/5 p-4">
            <div className="mb-3">
              <h2 className="text-sm font-medium text-slate-300">Filtros</h2>
              <p className="mt-1 text-xs text-slate-400">Consulta liberada para o perfil USER.</p>
            </div>
            <FiltersPanel filters={props.filters} setFilters={props.setFilters} page={props.page} setPage={props.setPage} />
          </aside>
          <main className="grid gap-4">
            <Summary cards={[
              { label: 'Veiculos', value: String(props.totalElements) },
              { label: 'Pagina', value: `${props.page + 1}/${Math.max(props.totalPages, 1)}` },
              { label: 'Perfil', value: 'USER' },
            ]} />
            <section className="rounded-lg border border-white/10 bg-white/5 p-4">
              <div className="mb-3 flex items-center justify-between">
                <h2 className="flex items-center gap-2 text-sm font-medium text-slate-300"><CarFront size={16} /> Veiculos</h2>
                {props.loading && <Loader2 className="animate-spin text-slate-400" size={16} />}
              </div>
              <VehicleTable items={props.items} />
            </section>
            <footer className="flex items-center justify-between text-xs text-slate-400">
              <span>GET /veiculos</span>
              <span>{props.error || 'pronto'}</span>
            </footer>
          </main>
        </section>
      </div>
    </div>
  );
}

function LoginScreen(props: {
  username: string;
  password: string;
  loading: boolean;
  error: string;
  onUsername: (value: string) => void;
  onPassword: (value: string) => void;
  onSubmit: (event: React.FormEvent) => void;
}) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex min-h-screen w-full max-w-5xl items-center px-4 py-8">
        <div className="grid w-full gap-6 lg:grid-cols-[1.2fr_0.8fr]">
          <section className="rounded-lg border border-white/10 bg-white/5 p-8">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Veiculos API</p>
            <h1 className="mt-3 text-4xl font-semibold">Visualizacao simples do cadastro</h1>
            <p className="mt-4 max-w-xl text-sm leading-6 text-slate-300">
              Login, consulta paginada e relatorio por marca em uma interface limpa, feita para enxergar o backend funcionando.
            </p>
          </section>
          <form className="rounded-lg border border-white/10 bg-slate-900 p-6 shadow-soft" onSubmit={props.onSubmit}>
            <h2 className="text-lg font-semibold">Entrar</h2>
            <div className="mt-4 space-y-3">
              <Input label="Usuario" value={props.username} onChange={props.onUsername} />
              <Input label="Senha" type="password" value={props.password} onChange={props.onPassword} />
            </div>
            <button disabled={props.loading} className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-md bg-cyan-500 px-4 py-2.5 text-sm font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-70">
              {props.loading ? <Loader2 size={16} className="animate-spin" /> : <ArrowRight size={16} />}
              Acessar
            </button>
            <p className="mt-4 text-xs text-slate-400">Padrao: `admin / admin123`</p>
            {props.error && <p className="mt-3 rounded-md border border-red-500/30 bg-red-500/10 p-3 text-sm text-red-200">{props.error}</p>}
          </form>
        </div>
      </div>
    </div>
  );
}

function FiltersPanel({ filters, setFilters, page, setPage }: {
  filters: Filters;
  setFilters: React.Dispatch<React.SetStateAction<Filters>>;
  page: number;
  setPage: (page: number) => void;
}) {
  return (
    <div className="grid gap-3">
      <Input label="Marca" value={filters.marca} onChange={(value) => update(filters, setFilters, 'marca', value, setPage)} />
      <Input label="Ano" value={filters.ano} onChange={(value) => update(filters, setFilters, 'ano', value, setPage)} />
      <Input label="Cor" value={filters.cor} onChange={(value) => update(filters, setFilters, 'cor', value, setPage)} />
      <div className="grid grid-cols-2 gap-3">
        <Input label="Min preco" value={filters.minPreco} onChange={(value) => update(filters, setFilters, 'minPreco', value, setPage)} />
        <Input label="Max preco" value={filters.maxPreco} onChange={(value) => update(filters, setFilters, 'maxPreco', value, setPage)} />
      </div>
      <Input label="Sort" value={filters.sort} onChange={(value) => update(filters, setFilters, 'sort', value, setPage)} />
      <Input label="Size" value={filters.size} onChange={(value) => update(filters, setFilters, 'size', value, setPage)} />
      <button className="inline-flex items-center justify-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={() => setPage(0)} type="button">
        <Search size={16} /> Aplicar ao buscar
      </button>
    </div>
  );
}

function VehicleTable({ items }: { items: Veiculo[] }) {
  if (!items.length) {
    return <EmptyState text="Sem resultados para os filtros atuais." />;
  }
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <thead className="text-xs uppercase tracking-[0.2em] text-slate-400">
          <tr>
            {['Placa', 'Marca', 'Modelo', 'Ano', 'Cor', 'USD', 'BRL'].map((label) => <th key={label} className="pb-3 pr-4">{label}</th>)}
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
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ReportList({ items }: { items: RelatorioPorMarca[] }) {
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

function Summary({ cards }: { cards: { label: string; value: string }[] }) {
  return (
    <div className="grid gap-3 sm:grid-cols-3">
      {cards.map((card) => (
        <div key={card.label} className="rounded-lg border border-white/10 bg-white/5 p-4">
          <p className="text-xs uppercase tracking-[0.2em] text-slate-400">{card.label}</p>
          <p className="mt-2 text-2xl font-semibold">{card.value}</p>
        </div>
      ))}
    </div>
  );
}

function EmptyState({ text }: { text: string }) {
  return <div className="rounded-md border border-dashed border-white/10 p-6 text-sm text-slate-400">{text}</div>;
}

function Input({ label, value, onChange, type = 'text' }: { label: string; value: string; onChange: (value: string) => void; type?: string; }) {
  return (
    <label className="block">
      <span className="mb-1 block text-xs text-slate-400">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-md border border-white/10 bg-slate-900 px-3 py-2 text-sm outline-none ring-0 placeholder:text-slate-500 focus:border-cyan-400"
      />
    </label>
  );
}

function update<K extends keyof Filters>(filters: Filters, setFilters: Dispatch<SetStateAction<Filters>>, key: K, value: string, setPage: (page: number) => void) {
  setFilters({ ...filters, [key]: value } as Filters);
  setPage(0);
}

function buildQuery(filters: Filters, page: number) {
  const query = new URLSearchParams();
  if (filters.marca) query.set('marca', filters.marca);
  if (filters.ano) query.set('ano', filters.ano);
  if (filters.cor) query.set('cor', filters.cor);
  if (filters.minPreco) query.set('minPreco', filters.minPreco);
  if (filters.maxPreco) query.set('maxPreco', filters.maxPreco);
  query.set('page', String(page));
  query.set('size', filters.size || '20');
  const [property, direction] = filters.sort.split(',');
  if (property) query.set('sort', `${property},${direction || 'asc'}`);
  return query;
}

function formatMoney(value: Veiculo['precoUsd'] | Veiculo['precoBrl']) {
  if (value === null || value === undefined) {
    return '-';
  }
  const number = typeof value === 'number' ? value : Number(value);
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(number);
}

function getStoredRole(): AccessRole | '' {
  const stored = localStorage.getItem('veiculos.role');
  return stored === 'ADMIN' || stored === 'USER' ? stored : '';
}

function getRoleFromToken(token: string): AccessRole {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) {
      return 'USER';
    }
    const payloadJson = decodeBase64Url(payloadBase64);
    const payload = JSON.parse(payloadJson) as { roles?: string[] };
    return payload.roles?.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER';
  } catch {
    return 'USER';
  }
}

function decodeBase64Url(value: string) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
  return atob(padded);
}
