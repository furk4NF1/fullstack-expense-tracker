import { formatMoney } from './ExpenseList.jsx';

export default function SummaryPanel({ summary }) {
  return (
    <>
      <section className="summary-grid" aria-label="Aylık finans özeti">
        <article className="summary-card balance-card">
          <span className="summary-icon" aria-hidden="true">↗</span>
          <p>Net bakiye</p>
          <strong>{formatMoney(summary?.netBalance ?? 0)}</strong>
          <span className="summary-caption">Seçilen ayın gelir ve gider farkı</span>
        </article>
        <article className="summary-card income-card">
          <span className="summary-icon" aria-hidden="true">＋</span>
          <p>Toplam gelir</p>
          <strong>{formatMoney(summary?.incomeTotal ?? 0)}</strong>
          <span className="summary-caption">Bu ay eklenen gelirler</span>
        </article>
        <article className="summary-card spending-card">
          <span className="summary-icon" aria-hidden="true">−</span>
          <p>Toplam gider</p>
          <strong>{formatMoney(summary?.expenseTotal ?? 0)}</strong>
          <span className="summary-caption">{summary?.transactionCount ?? 0} işlem kaydı</span>
        </article>
      </section>

      <section className="panel category-panel" aria-labelledby="category-title">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">HARCAMA ALIŞKANLIĞI</p>
            <h2 id="category-title">Gider kategorileri</h2>
          </div>
        </div>
        {summary?.categoryTotals?.length ? (
          <ul className="category-list">
            {summary.categoryTotals.map((category) => {
              const share = summary.expenseTotal > 0
                ? Math.round((category.totalAmount / summary.expenseTotal) * 100)
                : 0;
              return (
                <li className="category-item" key={category.categoryId}>
                  <div className="category-label">
                    <span><i style={{ '--category-color': category.categoryColor }} />{category.categoryName}</span>
                    <strong>{formatMoney(category.totalAmount)}</strong>
                  </div>
                  <div className="progress-track" role="progressbar" aria-label={`${category.categoryName} gider oranı`}
                    aria-valuemin="0" aria-valuemax="100" aria-valuenow={share}>
                    <span style={{ width: `${share}%`, '--category-color': category.categoryColor }} />
                  </div>
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="muted-copy">Gider ekledikçe kategori dağılımın burada görünür.</p>
        )}
      </section>
    </>
  );
}
