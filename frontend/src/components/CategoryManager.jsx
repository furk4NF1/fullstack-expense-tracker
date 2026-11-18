import { useState } from 'react';

export default function CategoryManager({ categories, onUpdate, onDelete, isLoading }) {
  const [editingId, setEditingId] = useState(null);
  const [draft, setDraft] = useState({ name: '', color: '#527a68' });
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');

  function startEditing(category) {
    setEditingId(category.id);
    setDraft({ name: category.name, color: category.color });
    setError('');
  }

  async function save(event, category) {
    event.preventDefault();
    const name = draft.name.trim();
    if (!name) {
      setError('Kategori adı boş olamaz.');
      return;
    }

    setBusyId(category.id);
    setError('');
    try {
      await onUpdate(category.id, { name, color: draft.color });
      setEditingId(null);
    } catch (saveError) {
      setError(saveError.message || 'Kategori güncellenemedi.');
    } finally {
      setBusyId(null);
    }
  }

  async function remove(category) {
    const confirmed = window.confirm(
      `“${category.name}” kategorisini silmek istiyor musun? Harcama veya bütçede kullanılıyorsa silinemez.`
    );
    if (!confirmed) return;

    setBusyId(category.id);
    setError('');
    try {
      await onDelete(category.id);
      if (editingId === category.id) setEditingId(null);
    } catch (deleteError) {
      setError(deleteError.message || 'Kategori silinemedi.');
    } finally {
      setBusyId(null);
    }
  }

  return (
    <section className="panel category-management-panel" aria-labelledby="category-management-title">
      <div className="panel-heading category-management-heading">
        <div>
          <p className="eyebrow">KİŞİSELLEŞTİR</p>
          <h2 id="category-management-title">Kategorileri yönet</h2>
          <p className="category-management-copy">Adı veya rengi değiştirebilir, kullanılmayan kategorileri silebilirsin.</p>
        </div>
        <span className="count-pill">{categories.length} kategori</span>
      </div>

      {error && <p className="category-manager-error" role="alert">{error}</p>}
      {categories.length === 0 && !isLoading ? (
        <p className="muted-copy">Henüz kategori yok.</p>
      ) : (
        <ul className="category-manager-list">
          {categories.map((category) => (
            <li className="category-manager-item" key={category.id}>
              {editingId === category.id ? (
                <form className="category-edit-form" onSubmit={(event) => save(event, category)}>
                  <label className="category-edit-name" htmlFor={`category-name-${category.id}`}>
                    <span>Ad</span>
                    <input id={`category-name-${category.id}`} type="text" maxLength="80"
                      value={draft.name} onChange={(event) => setDraft((current) => ({ ...current, name: event.target.value }))}
                      autoFocus required />
                  </label>
                  <label className="category-edit-color" htmlFor={`category-color-${category.id}`}>
                    <span>Renk</span>
                    <input id={`category-color-${category.id}`} type="color" value={draft.color}
                      onChange={(event) => setDraft((current) => ({ ...current, color: event.target.value }))} />
                  </label>
                  <div className="category-manager-actions">
                    <button className="primary-button category-save-button" type="submit" disabled={busyId === category.id}>
                      {busyId === category.id ? 'Kaydediliyor…' : 'Kaydet'}
                    </button>
                    <button className="text-button" type="button" onClick={() => { setEditingId(null); setError(''); }}>
                      Vazgeç
                    </button>
                  </div>
                </form>
              ) : (
                <>
                  <span className="category-manager-name">
                    <i style={{ '--category-color': category.color }} aria-hidden="true" />
                    <strong>{category.name}</strong>
                  </span>
                  <div className="category-manager-actions">
                    <button className="text-button" type="button" onClick={() => startEditing(category)}
                      disabled={busyId !== null} aria-label={`${category.name} kategorisini düzenle`}>
                      Düzenle
                    </button>
                    <button className="text-button category-delete-button" type="button" onClick={() => remove(category)}
                      disabled={busyId !== null} aria-label={`${category.name} kategorisini sil`}>
                      {busyId === category.id ? 'Siliniyor…' : 'Sil'}
                    </button>
                  </div>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

