import type { FormEvent } from 'react';
import { ArrowRight, Loader2 } from 'lucide-react';
import { FormInput } from './FormInput';

type LoginScreenProps = {
  username: string;
  password: string;
  loading: boolean;
  error: string;
  onUsername: (value: string) => void;
  onPassword: (value: string) => void;
  onSubmit: (event: FormEvent) => void;
};

export function LoginScreen(props: LoginScreenProps) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex min-h-screen w-full max-w-5xl items-center px-4 py-8">
        <div className="grid w-full gap-6 lg:grid-cols-[1.2fr_0.8fr]">
          <section className="rounded-lg border border-white/10 bg-white/5 p-8">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Veiculos API</p>
            <h1 className="mt-3 text-4xl font-semibold">Acesso por perfil</h1>
            <p className="mt-4 max-w-xl text-sm leading-6 text-slate-300">
              `USER` consulta os dados. `ADMIN` consulta, cadastra, atualiza e remove veiculos.
            </p>
          </section>
          <form className="rounded-lg border border-white/10 bg-slate-900 p-6 shadow-soft" onSubmit={props.onSubmit}>
            <h2 className="text-lg font-semibold">Entrar</h2>
            <div className="mt-4 space-y-3">
              <FormInput label="Usuario" value={props.username} onChange={props.onUsername} />
              <FormInput label="Senha" type="password" value={props.password} onChange={props.onPassword} />
            </div>
            <button
              disabled={props.loading}
              className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-md bg-cyan-500 px-4 py-2.5 text-sm font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-70"
            >
              {props.loading ? <Loader2 size={16} className="animate-spin" /> : <ArrowRight size={16} />}
              Acessar
            </button>
            <div className="mt-4 space-y-1 text-xs text-slate-400">
              <p>`admin / admin123`</p>
              <p>`user / user123`</p>
            </div>
            {props.error ? <p className="mt-3 rounded-md border border-red-500/30 bg-red-500/10 p-3 text-sm text-red-200">{props.error}</p> : null}
          </form>
        </div>
      </div>
    </div>
  );
}
