export function Summary({ cards }: { cards: { label: string; value: string }[] }) {
  return (
    <div className={`grid gap-3 ${cards.length === 4 ? 'sm:grid-cols-2 xl:grid-cols-4' : 'sm:grid-cols-3'}`}>
      {cards.map((card) => (
        <div key={card.label} className="rounded-lg border border-white/10 bg-white/5 p-4">
          <p className="text-xs uppercase tracking-[0.2em] text-slate-400">{card.label}</p>
          <p className="mt-2 text-2xl font-semibold">{card.value}</p>
        </div>
      ))}
    </div>
  );
}
