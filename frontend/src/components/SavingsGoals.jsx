import { useEffect, useState } from 'react';
import { expenseApi } from '../api/expenseApi.js';
import { formatMoney } from './ExpenseList.jsx';

const emptyForm = () => ({ name: '', targetAmount: '', savedAmount: '0', targetDate: '', version: null });
const today = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
};

export default function SavingsGoals() {
  const [goals, setGoals] = useState([]);
  const [form, setForm] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    let active = true;
    expenseApi.getSavingsGoals()
      .then(data => { if (active) setGoals(data); })
      .catch(e => { if (active) setError(e.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function reload() {
    setLoading(true);
    setError('');
    try { setGoals(await expenseApi.getSavingsGoals()); }
    catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }

  function open(goal) {
    setEditingId(goal?.id ?? null);
    setForm(goal ? { ...goal, targetDate: goal.targetDate ?? '' } : emptyForm());
    setError('');
    setNotice('');
  }

  async function save(event) {
    event.preventDefault();
    if (!form.name.trim()) { setError('Hedefin adını yaz.'); return; }
    setBusy(true);
    setError('');
    setNotice('');
    try {
      const payload = { name: form.name.trim(), targetAmount: Number(form.targetAmount),
        savedAmount: Number(form.savedAmount), targetDate: form.targetDate || null, version: form.version };
      const saved = editingId === null
        ? await expenseApi.createSavingsGoal(payload)
        : await expenseApi.updateSavingsGoal(editingId, payload);
      setGoals(current => editingId === null ? [saved, ...current] : current.map(g => g.id === saved.id ? saved : g));
      setForm(null);
      setNotice('Birikim hedefi kaydedildi.');
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }

  async function remove(goal) {
    if (!window.confirm(`“${goal.name}” hedefi silinsin mi? Gelir-gider kayıtların değişmez.`)) return;
    setBusy(true);
    setError('');
    setNotice('');
    try {
      await expenseApi.deleteSavingsGoal(goal.id);
      setGoals(current => current.filter(g => g.id !== goal.id));
      if (editingId === goal.id) setForm(null);
      setNotice('Birikim hedefi silindi.');
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }

  const field = event => setForm(current => ({ ...current, [event.target.name]: event.target.value }));
  return <section className="panel savings-panel" id="savings" aria-labelledby="savings-title">
    <div className="panel-heading recurring-heading">
      <div><p className="eyebrow">GELECEĞİNE AYIR</p><h2 id="savings-title">Birikim hedefleri</h2>
        <p className="recurring-copy">Hayalini bir hedefe dönüştür; birikimini adım adım takip et.</p></div>
      <button className="primary-button" disabled={busy || loading} onClick={() => open(null)}>+ Yeni hedef</button>
    </div>
    <p className="savings-note">Birikim tutarını kendin güncellersin. Hedefler tüm aylar için geçerlidir; gelir-gider kayıtlarını ve bakiyeni değiştirmez.</p>
    {error && <div className="recurring-error" role="alert">{error} <button className="text-button" disabled={busy || loading} onClick={reload}>Listeyi yenile</button></div>}
    {notice && <p className="recurring-notice" role="status">{notice}</p>}
    {loading ? <p role="status">Hedefler yükleniyor…</p> : goals.length === 0
      ? <p className="muted-copy recurring-empty">Henüz hedefin yok. Bilgisayar, tatil veya acil durum birikimiyle başlayabilirsin.</p>
      : <div className="savings-grid">{goals.map(goal => {
        const percentage = Math.min(100, Number(goal.savedAmount) / Number(goal.targetAmount) * 100);
        const complete = Number(goal.savedAmount) >= Number(goal.targetAmount);
        const overdue = goal.targetDate && goal.targetDate < today() && !complete;
        return <article className="savings-card" key={goal.id}>
          <div className="savings-card-heading"><h3>{goal.name}</h3><span className={complete ? 'savings-badge complete' : 'savings-badge'}>{complete ? 'Tamamlandı' : overdue ? 'Tarih geçti' : 'Devam ediyor'}</span></div>
          <p className="savings-amount">{formatMoney(Number(goal.savedAmount))} <span>/ {formatMoney(Number(goal.targetAmount))}</span></p>
          <progress max="100" value={percentage} aria-label={`${goal.name} ilerleme`} />
          <div className="savings-progress-copy"><strong>%{Math.floor(percentage)}</strong><span>Kalan {formatMoney(Math.max(0, Number(goal.targetAmount) - Number(goal.savedAmount)))}</span></div>
          <p className="savings-date">{goal.targetDate ? `Hedef tarihi: ${new Intl.DateTimeFormat('tr-TR').format(new Date(goal.targetDate + 'T12:00:00'))}` : 'Hedef tarihi belirlenmedi'}</p>
          <div className="recurring-actions"><button className="text-button" disabled={busy} onClick={() => open(goal)} aria-label={`${goal.name} hedefini ve birikimini düzenle`}>Birikimi / hedefi düzenle</button><button className="text-button category-delete-button" disabled={busy} onClick={() => remove(goal)} aria-label={`${goal.name} hedefini sil`}>Sil</button></div>
        </article>;
      })}</div>}
    {form && <form className="recurring-form" onSubmit={save}>
      <h3>{editingId === null ? 'Yeni birikim hedefi' : 'Hedefi ve birikimi düzenle'}</h3>
      <fieldset disabled={busy} className="savings-fields">
        <div className="recurring-form-grid">
          <label>Hedef adı<input autoFocus name="name" maxLength="120" required value={form.name} onChange={field} placeholder="Örn. Yeni bilgisayar" /></label>
          <label>Hedef tutarı (₺)<input name="targetAmount" type="number" min="0.01" max="9999999999.99" step="0.01" required value={form.targetAmount} onChange={field} /></label>
          <label>Şu an biriken toplam (₺)<input name="savedAmount" type="number" min="0" max="9999999999.99" step="0.01" required value={form.savedAmount} onChange={field} /></label>
          <label>Hedef tarihi (isteğe bağlı)<input name="targetDate" type="date" min="1900-01-01" max="9999-12-31" value={form.targetDate} onChange={field} /></label>
        </div>
        <p className="savings-note">Eklediğin miktarı değil, güncel toplam birikimini yaz. Hedef tutarını aşabilirsin.</p>
        <div className="recurring-form-actions"><button className="primary-button" type="submit">{busy ? 'Kaydediliyor…' : 'Kaydet'}</button><button className="text-button" type="button" onClick={() => setForm(null)}>Vazgeç</button></div>
      </fieldset>
    </form>}
  </section>;
}
