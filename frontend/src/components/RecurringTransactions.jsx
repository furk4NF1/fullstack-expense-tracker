import { useEffect, useState } from 'react';
import { expenseApi } from '../api/expenseApi.js';
import { formatMoney } from './ExpenseList.jsx';

const emptyForm = () => ({ type: 'EXPENSE', description: '', amount: '', categoryId: '', dayOfMonth: '1' });

export default function RecurringTransactions({ categories, selectedMonth, monthLabel, onApplied }) {
  const [templates, setTemplates] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadTemplates() {
    const data = await expenseApi.getRecurringTransactions();
    setTemplates(data);
  }

  useEffect(() => {
    let active = true;
    expenseApi.getRecurringTransactions()
      .then((data) => { if (active) setTemplates(data); })
      .catch((loadError) => { if (active) setError(loadError.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function edit(template) {
    setForm({ type: template.type, description: template.description, amount: String(template.amount),
      categoryId: String(template.categoryId), dayOfMonth: String(template.dayOfMonth) });
    setEditingId(template.id);
    setShowForm(true);
    setError('');
    setNotice('');
  }

  function closeForm() {
    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm());
    setError('');
  }

  async function save(event) {
    event.preventDefault();
    const description = form.description.trim();
    if (!description) {
      setError('Açıklamayı yaz.');
      return;
    }
    setBusy(true);
    setError('');
    setNotice('');
    try {
      const payload = { type: form.type, description, amount: Number(form.amount),
        categoryId: Number(form.categoryId), dayOfMonth: Number(form.dayOfMonth) };
      if (editingId === null) await expenseApi.createRecurringTransaction(payload);
      else await expenseApi.updateRecurringTransaction(editingId, payload);
      await loadTemplates();
      setNotice(editingId === null ? 'Düzenli işlem eklendi.' : 'Düzenli işlem güncellendi.');
      closeForm();
    } catch (saveError) {
      setError(saveError.message || 'Düzenli işlem kaydedilemedi.');
    } finally {
      setBusy(false);
    }
  }

  async function remove(template) {
    if (!window.confirm(`“${template.description}” düzenli işlemi silinsin mi? Daha önce oluşturulan işlemler korunur.`)) return;
    setBusy(true);
    setError('');
    setNotice('');
    try {
      await expenseApi.deleteRecurringTransaction(template.id);
      await loadTemplates();
      if (editingId === template.id) closeForm();
      setNotice('Düzenli işlem silindi.');
    } catch (deleteError) {
      setError(deleteError.message || 'Düzenli işlem silinemedi.');
    } finally {
      setBusy(false);
    }
  }

  async function apply() {
    setBusy(true);
    setError('');
    setNotice('');
    try {
      const result = await expenseApi.applyRecurringTransactions(selectedMonth);
      await onApplied();
      setNotice(`${monthLabel} için ${result.createdCount} işlem eklendi, ${result.skippedCount} işlem zaten eklendiği için atlandı.`);
    } catch (applyError) {
      setError(applyError.message || 'İşlemler eklenemedi.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="panel recurring-panel" id="recurring" aria-labelledby="recurring-title">
      <div className="panel-heading recurring-heading">
        <div>
          <p className="eyebrow">AYLIK DÜZEN</p>
          <h2 id="recurring-title">Düzenli işlemler</h2>
          <p className="recurring-copy">Kira, maaş ve abonelik gibi işlemlerini kaydet; istediğin ay tek tıkla ekle.</p>
        </div>
        <span className="count-pill">{templates.length} düzenli işlem</span>
      </div>

      <div className="recurring-info">
        <p>İşlemler yalnızca sen uyguladığında eklenir. Aynı işlem aynı aya ikinci kez eklenmez. Ayın son gününü aşan tarihler, o ayın son gününe kaydedilir.</p>
        <button className="primary-button recurring-apply" type="button"
          disabled={busy || loading || templates.length === 0 || !selectedMonth} onClick={apply}>
          {busy ? 'Lütfen bekle…' : `${monthLabel} ayına uygula`}
        </button>
      </div>

      {error && <p className="recurring-error" role="alert">{error}</p>}
      {notice && <p className="recurring-notice" role="status">{notice}</p>}
      {loading ? <p className="muted-copy" role="status">Düzenli işlemler yükleniyor…</p> : (
        templates.length === 0 ? <p className="muted-copy recurring-empty">Henüz düzenli işlem eklenmedi.</p> : (
          <ul className="recurring-list">
            {templates.map((template) => {
              const category = categories.find((item) => item.id === template.categoryId);
              return <li className="recurring-item" key={template.id}>
                <span className="recurring-dot" style={{ '--category-color': category?.color ?? template.categoryColor }} aria-hidden="true" />
                <div className="recurring-item-detail">
                  <strong>{template.description}</strong>
                  <span>{category?.name ?? template.categoryName} · Her ayın {template.dayOfMonth}. günü · {template.type === 'INCOME' ? 'Gelir' : 'Gider'}</span>
                </div>
                <strong className={template.type === 'INCOME' ? 'recurring-amount income' : 'recurring-amount'}>
                  {template.type === 'INCOME' ? '+' : '−'}{formatMoney(Number(template.amount))}
                </strong>
                <div className="recurring-actions">
                  <button className="text-button" type="button" disabled={busy} onClick={() => edit(template)}
                    aria-label={`${template.description} düzenli işlemini düzenle`}>Düzenle</button>
                  <button className="text-button category-delete-button" type="button" disabled={busy} onClick={() => remove(template)}
                    aria-label={`${template.description} düzenli işlemini sil`}>Sil</button>
                </div>
              </li>;
            })}
          </ul>
        )
      )}

      {!showForm ? (
        <button className="recurring-new" type="button" disabled={busy} onClick={() => { setShowForm(true); setError(''); setNotice(''); }}>
          + Düzenli işlem ekle
        </button>
      ) : (
        <form className="recurring-form" onSubmit={save}>
          <h3>{editingId === null ? 'Yeni düzenli işlem' : 'Düzenli işlemi düzenle'}</h3>
          <div className="recurring-form-grid">
            <label>İşlem türü
              <select name="type" value={form.type} onChange={updateField} required>
                <option value="EXPENSE">Gider</option><option value="INCOME">Gelir</option>
              </select>
            </label>
            <label>Açıklama
              <input name="description" type="text" maxLength="120" placeholder="Örn. Kira" value={form.description} onChange={updateField} required />
            </label>
            <label>Tutar (₺)
              <input name="amount" type="number" min="0.01" max="9999999999.99" step="0.01" inputMode="decimal" value={form.amount} onChange={updateField} required />
            </label>
            <label>Kategori
              <select name="categoryId" value={form.categoryId} onChange={updateField} required>
                <option value="" disabled>Kategori seç</option>
                {categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}
              </select>
            </label>
            <label>Her ayın günü
              <input name="dayOfMonth" type="number" min="1" max="31" step="1" value={form.dayOfMonth} onChange={updateField} required />
            </label>
          </div>
          {categories.length === 0 && <p className="recurring-error">Önce bir kategori oluştur.</p>}
          <div className="recurring-form-actions">
            <button className="primary-button" type="submit" disabled={busy || categories.length === 0}>
              {busy ? 'Kaydediliyor…' : editingId === null ? 'Ekle' : 'Kaydet'}
            </button>
            <button className="text-button" type="button" disabled={busy} onClick={closeForm}>Vazgeç</button>
          </div>
        </form>
      )}
    </section>
  );
}
