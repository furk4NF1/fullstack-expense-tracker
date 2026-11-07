import { useEffect, useState } from 'react';
import { formatMoney } from './ExpenseList.jsx';

export default function BudgetPanel({ budgets, selectedMonth, onSave, onDelete, savingCategoryId }) {
  const [drafts, setDrafts] = useState({});

  useEffect(() => {
    setDrafts(Object.fromEntries(budgets.map((budget) => [
      budget.categoryId,
      budget.budgetAmount == null ? '' : String(budget.budgetAmount)
    ])));
  }, [budgets, selectedMonth]);

  return (
    <section className="panel budget-panel" id="budgets" aria-labelledby="budget-title">
      <div className="panel-heading budget-heading">
        <div>
          <p className="eyebrow">AYLIK PLAN</p>
          <h2 id="budget-title">Kategori bütçeleri</h2>
          <p className="budget-subtitle">Her kategori için bir harcama limiti belirle.</p>
        </div>
      </div>

      <ul className="budget-list">
        {budgets.map((budget) => {
          const hasBudget = budget.budgetAmount != null;
          const spent = Number(budget.spentAmount);
          const limit = hasBudget ? Number(budget.budgetAmount) : 0;
          const usedPercent = hasBudget && limit > 0 ? (spent / limit) * 100 : 0;
          const barPercent = Math.min(100, Math.max(0, usedPercent));
          const isSaving = savingCategoryId === budget.categoryId;

          return (
            <li className="budget-item" key={budget.categoryId}>
              <div className="budget-item-heading">
                <span className="budget-category-name">
                  <i style={{ '--category-color': budget.categoryColor }} />
                  <strong>{budget.categoryName}</strong>
                </span>
                <span className="budget-spending">
                  {formatMoney(spent)}{hasBudget ? ` / ${formatMoney(limit)}` : ' harcandı'}
                </span>
              </div>

              {hasBudget ? (
                <>
                  <div className={`budget-progress ${budget.exceeded ? 'over-budget' : ''}`}
                    role="progressbar"
                    aria-label={`${budget.categoryName} bütçe kullanımı`}
                    aria-valuemin="0"
                    aria-valuemax="100"
                    aria-valuenow={Math.round(barPercent)}>
                    <span style={{ width: `${barPercent}%` }} />
                  </div>
                  <p className={`budget-status ${budget.exceeded ? 'over-budget-text' : ''}`}>
                    {budget.exceeded
                      ? `Limit ${formatMoney(Math.abs(Number(budget.remainingAmount)))} aşıldı.`
                      : `${Math.round(usedPercent)}% kullanıldı · ${formatMoney(Number(budget.remainingAmount))} kaldı.`}
                  </p>
                </>
              ) : (
                <p className="budget-status">Bu ay için henüz limit belirlenmedi.</p>
              )}

              <form className="budget-form" onSubmit={(event) => {
                event.preventDefault();
                onSave(budget.categoryId, Number(drafts[budget.categoryId]));
              }}>
                <label className="budget-input-label">
                  <span>Aylık limit</span>
                  <span className="budget-input-wrap">
                    <input
                      aria-label={`${budget.categoryName} aylık bütçe limiti`}
                      type="number"
                      min="0.01"
                      max="9999999999.99"
                      step="0.01"
                      required
                      value={drafts[budget.categoryId] ?? ''}
                      onChange={(event) => setDrafts((current) => ({
                        ...current,
                        [budget.categoryId]: event.target.value
                      }))}
                    />
                    <span>₺</span>
                  </span>
                </label>
                <button className="budget-save-button" type="submit" disabled={isSaving}>
                  {isSaving ? 'Kaydediliyor…' : hasBudget ? 'Güncelle' : 'Limit belirle'}
                </button>
                {hasBudget && (
                  <button className="budget-delete-button" type="button" disabled={isSaving}
                    onClick={() => onDelete(budget)}>
                    Kaldır
                  </button>
                )}
              </form>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
