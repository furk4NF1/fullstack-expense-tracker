import { useEffect, useState } from 'react';

function today() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function emptyForm(selectedMonth) {
  const currentDate = today();
  const defaultDate = currentDate.startsWith(`${selectedMonth}-`) ? currentDate : `${selectedMonth}-01`;
  return { type: 'EXPENSE', amount: '', description: '', expenseDate: defaultDate, categoryId: '' };
}

export default function ExpenseForm({ categories, selectedExpense, selectedMonth, onSave, onCreateCategory, onCancel, isSaving }) {
  const [form, setForm] = useState(() => emptyForm(selectedMonth));
  const [showCategoryForm, setShowCategoryForm] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [newCategoryColor, setNewCategoryColor] = useState('#527a68');
  const [categoryError, setCategoryError] = useState('');
  const [isCreatingCategory, setIsCreatingCategory] = useState(false);

  useEffect(() => {
    setForm(selectedExpense
      ? {
          type: selectedExpense.type,
          amount: String(selectedExpense.amount),
          description: selectedExpense.description,
          expenseDate: selectedExpense.expenseDate,
          categoryId: String(selectedExpense.categoryId)
        }
      : emptyForm(selectedMonth));
  }, [selectedExpense, selectedMonth]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function submit(event) {
    event.preventDefault();
    onSave({
      type: form.type,
      amount: Number(form.amount),
      description: form.description.trim(),
      expenseDate: form.expenseDate,
      categoryId: Number(form.categoryId)
    });
  }

  async function createCategory() {
    const name = newCategoryName.trim();
    if (!name) {
      setCategoryError('Kategori adını yaz.');
      return;
    }

    setIsCreatingCategory(true);
    setCategoryError('');
    try {
      const category = await onCreateCategory({ name, color: newCategoryColor });
      setForm((current) => ({ ...current, categoryId: String(category.id) }));
      setNewCategoryName('');
      setShowCategoryForm(false);
    } catch (error) {
      setCategoryError(error.message || 'Kategori eklenemedi.');
    } finally {
      setIsCreatingCategory(false);
    }
  }

  return (
    <section className="panel form-panel" aria-labelledby="form-title">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">KAYIT</p>
          <h2 id="form-title">{selectedExpense ? 'Kaydı düzenle' : 'Yeni işlem ekle'}</h2>
        </div>
        {selectedExpense && <button className="text-button" type="button" onClick={onCancel}>Vazgeç</button>}
      </div>

      <form className="expense-form" onSubmit={submit}>
        <fieldset className="type-toggle">
          <legend>İşlem türü</legend>
          <label className={form.type === 'EXPENSE' ? 'selected expense-choice' : 'expense-choice'}>
            <input type="radio" name="type" value="EXPENSE" checked={form.type === 'EXPENSE'} onChange={updateField} />
            <span>Gider</span>
          </label>
          <label className={form.type === 'INCOME' ? 'selected income-choice' : 'income-choice'}>
            <input type="radio" name="type" value="INCOME" checked={form.type === 'INCOME'} onChange={updateField} />
            <span>Gelir</span>
          </label>
        </fieldset>

        <label className="field-label" htmlFor="amount">Tutar</label>
        <div className="amount-input-wrap">
          <input id="amount" name="amount" type="number" inputMode="decimal" min="0.01" step="0.01"
            placeholder="0,00" value={form.amount} onChange={updateField} required />
          <span>₺</span>
        </div>

        <label className="field-label" htmlFor="description">Açıklama</label>
        <input id="description" name="description" type="text" maxLength="120"
          placeholder="Örn. Market alışverişi" value={form.description} onChange={updateField} required />

        <div className="field-row">
          <div>
            <label className="field-label" htmlFor="expenseDate">Tarih</label>
            <input id="expenseDate" name="expenseDate" type="date" value={form.expenseDate}
              onChange={updateField} required />
          </div>
          <div>
            <label className="field-label" htmlFor="categoryId">Kategori</label>
            <select id="categoryId" name="categoryId" value={form.categoryId} onChange={updateField} required>
              <option value="" disabled>Kategori seç</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>{category.name}</option>
              ))}
            </select>
          </div>
        </div>

        <button className="text-button category-create-toggle" type="button"
          aria-expanded={showCategoryForm} aria-controls="category-create-panel"
          onClick={() => {
            setShowCategoryForm((visible) => !visible);
            setCategoryError('');
          }}>
          {showCategoryForm ? 'Kategori eklemeyi kapat' : '+ Yeni kategori ekle'}
        </button>
        {showCategoryForm && (
          <div className="category-create-panel" id="category-create-panel" role="group" aria-label="Yeni kategori bilgileri">
            <label className="category-create-field category-create-name" htmlFor="newCategoryName">
              <span>Kategori adı</span>
              <input id="newCategoryName" type="text" maxLength="80" value={newCategoryName}
                placeholder="Örn. Eğitim" onChange={(event) => setNewCategoryName(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault();
                    createCategory();
                  }
                }} />
            </label>
            <label className="category-create-field category-create-color" htmlFor="newCategoryColor">
              <span>Renk</span>
              <input id="newCategoryColor" type="color" value={newCategoryColor}
                onChange={(event) => setNewCategoryColor(event.target.value)} />
            </label>
            <button className="primary-button category-create-submit" type="button"
              disabled={isCreatingCategory} onClick={createCategory}>
              {isCreatingCategory ? 'Ekleniyor…' : 'Ekle'}
            </button>
            {categoryError && <p className="category-create-error" role="alert">{categoryError}</p>}
          </div>
        )}

        <button className="primary-button submit-button" type="submit" disabled={isSaving || categories.length === 0}>
          {isSaving ? 'Kaydediliyor…' : selectedExpense ? 'Değişiklikleri kaydet' : 'İşlemi kaydet'}
        </button>
      </form>
    </section>
  );
}

