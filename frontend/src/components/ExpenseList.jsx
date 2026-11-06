function formatDate(value) {
  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'short' })
    .format(new Date(year, month - 1, day));
}

function formatMoney(value) {
  return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(value);
}

export default function ExpenseList({ expenses, onEdit, onDelete }) {
  return (
    <section className="panel list-panel" aria-labelledby="list-title">
      <div className="panel-heading list-heading">
        <div>
          <p className="eyebrow">HAREKETLER</p>
          <h2 id="list-title">Son işlemler</h2>
        </div>
        <span className="count-pill">{expenses.length} kayıt</span>
      </div>

      {expenses.length === 0 ? (
        <div className="empty-state">
          <span className="empty-mark" aria-hidden="true">₺</span>
          <h3>Bu ay henüz işlem yok</h3>
          <p>İlk gelir veya gider kaydını formdan ekleyebilirsin.</p>
        </div>
      ) : (
        <ul className="expense-list">
          {expenses.map((expense) => (
            <li className="expense-row" key={expense.id}>
              <span className="category-dot" style={{ '--category-color': expense.categoryColor }} aria-hidden="true" />
              <div className="expense-description">
                <strong>{expense.description}</strong>
                <span>{expense.categoryName} · {formatDate(expense.expenseDate)}</span>
              </div>
              <strong className={`expense-amount ${expense.type === 'INCOME' ? 'positive' : ''}`}>
                {expense.type === 'INCOME' ? '+' : '−'}{formatMoney(expense.amount)}
              </strong>
              <div className="row-actions">
                <button type="button" aria-label={`${expense.description} kaydını düzenle`} onClick={() => onEdit(expense)}>Düzenle</button>
                <button type="button" aria-label={`${expense.description} kaydını sil`} onClick={() => onDelete(expense)}>Sil</button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export { formatMoney };
