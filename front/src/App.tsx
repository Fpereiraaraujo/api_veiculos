import { useEffect, useMemo, useState } from 'react';
import type { Dispatch, FormEvent, ReactNode, SetStateAction } from 'react';
import { BarChart3, CarFront, Crown, Loader2, LogOut, RefreshCw, ShieldCheck } from 'lucide-react';
import { atualizarParcialVeiculo, atualizarVeiculo, cadastrarVeiculo, listarRelatorio, listarVeiculos, login, removerVeiculo } from './api';
import { getRoleFromToken, getStoredRole } from './auth';
import { AdminVehicleForm, type AdminFormMode, type AdminVehicleFormValues } from './components/AdminVehicleForm';
import { FiltersPanel, type Filters } from './components/FiltersPanel';
import { LoginScreen } from './components/LoginScreen';
import { ReportList } from './components/ReportList';
import { Summary } from './components/Summary';
import { VehicleTable } from './components/VehicleTable';
import type { RelatorioPorMarca, Veiculo, VeiculoPatchPayload, VeiculoPayload } from './types';

const defaultFilters: Filters = {
  marca: '',
  ano: '',
  cor: '',
  minPreco: '',
  maxPreco: '',
  sort: 'marca,asc',
  size: '20',
};

const emptyAdminForm: AdminVehicleFormValues = {
  placa: '',
  marca: '',
  modelo: '',
  ano: '',
  cor: '',
  precoUsd: '',
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
  const [adminLoading, setAdminLoading] = useState(false);
  const [error, setError] = useState('');
  const [adminError, setAdminError] = useState('');
  const [adminMessage, setAdminMessage] = useState('');
  const [items, setItems] = useState<Veiculo[]>([]);
  const [report, setReport] = useState<RelatorioPorMarca[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedVehicle, setSelectedVehicle] = useState<Veiculo | null>(null);
  const [adminMode, setAdminMode] = useState<AdminFormMode>('create');
  const [adminForm, setAdminForm] = useState<AdminVehicleFormValues>(emptyAdminForm);

  const loggedIn = Boolean(token);
  const isAdmin = role === 'ADMIN';
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
  }, [loggedIn, query, role]);

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
      const pageResult = await listarVeiculos(token, query);
      setItems(pageResult.content);
      setTotalElements(pageResult.page.totalElements);
      setTotalPages(pageResult.page.totalPages);

      if (isAdmin) {
        const reportResult = await listarRelatorio(token);
        setReport(reportResult);
      } else {
        setReport([]);
      }
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
    resetAdminState();
  }

  function handleAdminModeChange(mode: AdminFormMode) {
    setAdminMode(mode);
    setAdminError('');
    setAdminMessage('');

    if (mode === 'create') {
      setSelectedVehicle(null);
      setAdminForm(emptyAdminForm);
      return;
    }

    if (selectedVehicle) {
      setAdminForm(toFormValues(selectedVehicle));
    }
  }

  function handleAdminFieldChange(field: keyof AdminVehicleFormValues, value: string) {
    setAdminForm((current) => ({ ...current, [field]: value }));
  }

  async function handleAdminSubmit(event: FormEvent) {
    event.preventDefault();
    setAdminLoading(true);
    setAdminError('');
    setAdminMessage('');

    try {
      if (adminMode === 'create') {
        await cadastrarVeiculo(token, toCreatePayload(adminForm));
        setAdminMessage('Veiculo cadastrado com sucesso via POST.');
        setAdminForm(emptyAdminForm);
      } else if (adminMode === 'put') {
        if (!selectedVehicle) {
          throw new Error('Selecione um veiculo para usar PUT.');
        }
        await atualizarVeiculo(token, selectedVehicle.id, toCreatePayload(adminForm));
        setAdminMessage('Veiculo atualizado com sucesso via PUT.');
      } else {
        if (!selectedVehicle) {
          throw new Error('Selecione um veiculo para usar PATCH.');
        }
        await atualizarParcialVeiculo(token, selectedVehicle.id, toPatchPayload(adminForm, selectedVehicle));
        setAdminMessage('Veiculo atualizado com sucesso via PATCH.');
      }

      await loadData();
    } catch (err) {
      setAdminError(err instanceof Error ? err.message : 'Falha ao executar operacao de admin');
    } finally {
      setAdminLoading(false);
    }
  }

  function handleSelectForPut(item: Veiculo) {
    setSelectedVehicle(item);
    setAdminMode('put');
    setAdminError('');
    setAdminMessage('');
    setAdminForm(toFormValues(item));
  }

  function handleSelectForPatch(item: Veiculo) {
    setSelectedVehicle(item);
    setAdminMode('patch');
    setAdminError('');
    setAdminMessage('');
    setAdminForm(toFormValues(item));
  }

  async function handleDelete(item: Veiculo) {
    const confirmed = window.confirm(`Remover ${item.placa} via DELETE?`);
    if (!confirmed) {
      return;
    }

    setAdminLoading(true);
    setAdminError('');
    setAdminMessage('');

    try {
      await removerVeiculo(token, item.id);
      setAdminMessage('Veiculo removido com sucesso via DELETE.');
      if (selectedVehicle?.id === item.id) {
        resetAdminState();
      }
      await loadData();
    } catch (err) {
      setAdminError(err instanceof Error ? err.message : 'Falha ao remover veiculo');
    } finally {
      setAdminLoading(false);
    }
  }

  function resetAdminState() {
    setSelectedVehicle(null);
    setAdminMode('create');
    setAdminForm(emptyAdminForm);
    setAdminError('');
    setAdminMessage('');
  }

  if (!loggedIn) {
    return (
      <LoginScreen
        username={username}
        password={password}
        loading={authLoading}
        error={error}
        onUsername={setUsername}
        onPassword={setPassword}
        onSubmit={handleLogin}
      />
    );
  }

  return isAdmin ? (
    <AdminDashboard
      loading={loading}
      adminLoading={adminLoading}
      error={error}
      filters={filters}
      setFilters={setFilters}
      setPage={setPage}
      totalElements={totalElements}
      totalPages={totalPages}
      page={page}
      items={items}
      report={report}
      onRefresh={loadData}
      onLogout={logout}
      adminMode={adminMode}
      adminForm={adminForm}
      selectedVehicle={selectedVehicle}
      adminMessage={adminMessage}
      adminError={adminError}
      onAdminModeChange={handleAdminModeChange}
      onAdminFieldChange={handleAdminFieldChange}
      onAdminSubmit={handleAdminSubmit}
      onAdminReset={resetAdminState}
      onSelectForPut={handleSelectForPut}
      onSelectForPatch={handleSelectForPatch}
      onDelete={handleDelete}
    />
  ) : (
    <UserDashboard
      loading={loading}
      error={error}
      filters={filters}
      setFilters={setFilters}
      setPage={setPage}
      totalElements={totalElements}
      totalPages={totalPages}
      page={page}
      items={items}
      onRefresh={loadData}
      onLogout={logout}
    />
  );
}

type SharedDashboardProps = {
  loading: boolean;
  error: string;
  filters: Filters;
  setFilters: Dispatch<SetStateAction<Filters>>;
  setPage: Dispatch<SetStateAction<number>>;
  totalElements: number;
  totalPages: number;
  page: number;
  items: Veiculo[];
  onRefresh: () => void;
  onLogout: () => void;
};

type AdminDashboardProps = SharedDashboardProps & {
  adminLoading: boolean;
  report: RelatorioPorMarca[];
  adminMode: AdminFormMode;
  adminForm: AdminVehicleFormValues;
  selectedVehicle: Veiculo | null;
  adminMessage: string;
  adminError: string;
  onAdminModeChange: (mode: AdminFormMode) => void;
  onAdminFieldChange: (field: keyof AdminVehicleFormValues, value: string) => void;
  onAdminSubmit: (event: FormEvent) => void;
  onAdminReset: () => void;
  onSelectForPut: (item: Veiculo) => void;
  onSelectForPatch: (item: Veiculo) => void;
  onDelete: (item: Veiculo) => void;
};

function AdminDashboard(props: AdminDashboardProps) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col gap-6 px-4 py-6 lg:px-8">
        <DashboardHeader
          title="Painel administrativo"
          icon={<Crown size={20} className="text-amber-300" />}
          onRefresh={props.onRefresh}
          onLogout={props.onLogout}
        />

        <AdminVehicleForm
          mode={props.adminMode}
          values={props.adminForm}
          selectedVehicle={props.selectedVehicle}
          loading={props.adminLoading}
          message={props.adminMessage}
          error={props.adminError}
          onModeChange={props.onAdminModeChange}
          onChange={props.onAdminFieldChange}
          onSubmit={props.onAdminSubmit}
          onReset={props.onAdminReset}
        />

        <section className="grid gap-4 lg:grid-cols-[320px_1fr]">
          <aside className="rounded-lg border border-white/10 bg-white/5 p-4">
            <h2 className="mb-3 text-sm font-medium text-slate-300">Filtros</h2>
            <FiltersPanel
              filters={props.filters}
              setFilters={props.setFilters}
              setPage={props.setPage}
              helperText="Consulta liberada para ADMIN e USER, mas o CRUD completo e exclusivo de ADMIN."
            />
          </aside>
          <main className="grid gap-4">
            <Summary
              cards={[
                { label: 'Veiculos', value: String(props.totalElements) },
                { label: 'Pagina', value: `${props.page + 1}/${Math.max(props.totalPages, 1)}` },
                { label: 'Perfil', value: 'ADMIN' },
                { label: 'Rotas', value: 'CRUD' },
              ]}
            />
            <section className="grid gap-4 xl:grid-cols-[1fr_320px]">
              <div className="rounded-lg border border-white/10 bg-white/5 p-4">
                <div className="mb-3 flex items-center justify-between">
                  <h2 className="flex items-center gap-2 text-sm font-medium text-slate-300"><CarFront size={16} /> Veiculos</h2>
                  {props.loading ? <Loader2 className="animate-spin text-slate-400" size={16} /> : null}
                </div>
                <VehicleTable
                  items={props.items}
                  canManage
                  onEdit={props.onSelectForPut}
                  onPatch={props.onSelectForPatch}
                  onDelete={props.onDelete}
                />
              </div>
              <div className="rounded-lg border border-white/10 bg-white/5 p-4">
                <h2 className="mb-3 flex items-center gap-2 text-sm font-medium text-slate-300"><BarChart3 size={16} /> Relatorio</h2>
                <ReportList items={props.report} />
              </div>
            </section>
            <footer className="flex items-center justify-between text-xs text-slate-400">
              <span>POST, PUT, PATCH e DELETE disponiveis no painel ADMIN</span>
              <span>{props.error || 'pronto'}</span>
            </footer>
          </main>
        </section>
      </div>
    </div>
  );
}

function UserDashboard(props: SharedDashboardProps) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col gap-6 px-4 py-6 lg:px-8">
        <DashboardHeader
          title="Painel de consulta"
          icon={<ShieldCheck size={20} className="text-cyan-300" />}
          onRefresh={props.onRefresh}
          onLogout={props.onLogout}
        />

        <section className="grid gap-4 lg:grid-cols-[320px_1fr]">
          <aside className="rounded-lg border border-white/10 bg-white/5 p-4">
            <h2 className="mb-3 text-sm font-medium text-slate-300">Filtros</h2>
            <FiltersPanel
              filters={props.filters}
              setFilters={props.setFilters}
              setPage={props.setPage}
              helperText="Perfil USER: acesso de leitura apenas."
            />
          </aside>
          <main className="grid gap-4">
            <Summary
              cards={[
                { label: 'Veiculos', value: String(props.totalElements) },
                { label: 'Pagina', value: `${props.page + 1}/${Math.max(props.totalPages, 1)}` },
                { label: 'Perfil', value: 'USER' },
              ]}
            />
            <section className="rounded-lg border border-white/10 bg-white/5 p-4">
              <div className="mb-3 flex items-center justify-between">
                <h2 className="flex items-center gap-2 text-sm font-medium text-slate-300"><CarFront size={16} /> Veiculos</h2>
                {props.loading ? <Loader2 className="animate-spin text-slate-400" size={16} /> : null}
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

function DashboardHeader({
  title,
  icon,
  onRefresh,
  onLogout,
}: {
  title: string;
  icon: ReactNode;
  onRefresh: () => void;
  onLogout: () => void;
}) {
  return (
    <header className="flex flex-wrap items-center justify-between gap-4 border-b border-white/10 pb-4">
      <div>
        <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Veiculos API</p>
        <h1 className="flex items-center gap-2 text-2xl font-semibold">
          {icon}
          {title}
        </h1>
      </div>
      <div className="flex items-center gap-2">
        <button className="inline-flex items-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={onRefresh}>
          <RefreshCw size={16} /> Atualizar
        </button>
        <button className="inline-flex items-center gap-2 rounded-md bg-white/10 px-3 py-2 text-sm hover:bg-white/15" onClick={onLogout}>
          <LogOut size={16} /> Sair
        </button>
      </div>
    </header>
  );
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

function toFormValues(item: Veiculo): AdminVehicleFormValues {
  return {
    placa: item.placa,
    marca: item.marca,
    modelo: item.modelo,
    ano: String(item.ano),
    cor: item.cor,
    precoUsd: String(item.precoUsd),
  };
}

function toCreatePayload(values: AdminVehicleFormValues): VeiculoPayload {
  return {
    placa: values.placa.trim(),
    marca: values.marca.trim(),
    modelo: values.modelo.trim(),
    ano: Number(values.ano),
    cor: values.cor.trim(),
    precoUsd: Number(values.precoUsd),
  };
}

function toPatchPayload(values: AdminVehicleFormValues, selectedVehicle: Veiculo): VeiculoPatchPayload {
  const patch: VeiculoPatchPayload = {};

  if (values.placa.trim() && values.placa !== selectedVehicle.placa) patch.placa = values.placa.trim();
  if (values.marca.trim() && values.marca !== selectedVehicle.marca) patch.marca = values.marca.trim();
  if (values.modelo.trim() && values.modelo !== selectedVehicle.modelo) patch.modelo = values.modelo.trim();
  if (values.ano.trim() && Number(values.ano) !== selectedVehicle.ano) patch.ano = Number(values.ano);
  if (values.cor.trim() && values.cor !== selectedVehicle.cor) patch.cor = values.cor.trim();
  if (values.precoUsd.trim() && Number(values.precoUsd) !== Number(selectedVehicle.precoUsd)) patch.precoUsd = Number(values.precoUsd);

  if (!Object.keys(patch).length) {
    throw new Error('Altere ao menos um campo para usar PATCH.');
  }

  return patch;
}
