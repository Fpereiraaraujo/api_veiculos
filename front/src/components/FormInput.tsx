type FormInputProps = {
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
  placeholder?: string;
};

export function FormInput({ label, value, onChange, type = 'text', placeholder }: FormInputProps) {
  return (
    <label className="block">
      <span className="mb-1 block text-xs text-slate-400">{label}</span>
      <input
        type={type}
        value={value}
        placeholder={placeholder}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-md border border-white/10 bg-slate-900 px-3 py-2 text-sm outline-none ring-0 placeholder:text-slate-500 focus:border-cyan-400"
      />
    </label>
  );
}
