import { formatMoney } from './ExpenseList.jsx';

function comparison(currentValue, previousValue, kind, previousMonthLabel) {
  const current = Number(currentValue ?? 0);
  const previous = Number(previousValue ?? 0);
  const difference = current - previous;
  const monthLabel = previousMonthLabel || 'önceki ay';

  if (difference === 0) {
    return { text: `${monthLabel} ile aynı`, tone: 'neutral' };
  }

  const isFavorable = kind === 'expense' ? difference < 0 : difference > 0;
  const arrow = difference > 0 ? '↑' : '↓';

  if (kind === 'balance') {
    const crossedZero = previous !== 0 && current !== 0 && Math.sign(previous) !== Math.sign(current);
    const canShowPercent = previous !== 0 && !crossedZero;
    if (canShowPercent) {
      const percent = Math.abs(difference / previous) * 100;
      const formattedPercent = new Intl.NumberFormat('tr-TR', { maximumFractionDigits: 1 }).format(percent);
      return {
        text: `${arrow} %${formattedPercent} önceki aya göre (${monthLabel})`,
        tone: isFavorable ? 'good' : 'bad'
      };
    }

    const signChange = previous < 0 && current > 0
      ? 'Artıya geçti'
      : previous > 0 && current < 0
        ? 'Eksiye geçti'
        : `${formatMoney(Math.abs(difference))} değişim`;
    return { text: `${arrow} ${signChange} · ${monthLabel}`, tone: isFavorable ? 'good' : 'bad' };
  }

  if (previous === 0) {
    return { text: `Yeni: ${formatMoney(current)} · ${monthLabel}`, tone: isFavorable ? 'good' : 'bad' };
  }

  const percent = Math.abs(difference / previous) * 100;
  const formattedPercent = new Intl.NumberFormat('tr-TR', { maximumFractionDigits: 1 }).format(percent);
  return { text: `${arrow} %${formattedPercent} önceki aya göre (${monthLabel})`, tone: isFavorable ? 'good' : 'bad' };
}

function Comparison({ current, previous, kind, previousMonthLabel }) {
  if (previous == null) return null;
  const change = comparison(current, previous, kind, previousMonthLabel);
  return <span className={`summary-comparison summary-comparison-${change.tone}`}>{change.text}</span>;
}

export default function SummaryPanel({ summary, previousSummary, previousMonthLabel }) {
  return (
    <>
      <section className="summary-grid" aria-label="Aylık finans özeti">
        <article className="summary-card balance-card">
          <span className="summary-icon" aria-hidden="true">↗</span>
          <p>Net bakiye</p>
          <strong>{formatMoney(summary?.netBalance ?? 0)}</strong>
          <span className="summary-caption">Seçilen ayın gelir ve gider farkı</span>
          <Comparison current={summary?.netBalance} previous={previousSummary?.netBalance}
            kind="balance" previousMonthLabel={previousMonthLabel} />
        </article>
        <article className="summary-card income-card">
          <span className="summary-icon" aria-hidden="true">＋</span>
          <p>Toplam gelir</p>
          <strong>{formatMoney(summary?.incomeTotal ?? 0)}</strong>
          <span className="summary-caption">Bu ay eklenen gelirler</span>
          <Comparison current={summary?.incomeTotal} previous={previousSummary?.incomeTotal}
            kind="income" previousMonthLabel={previousMonthLabel} />
        </article>
        <article className="summary-card spending-card">
          <span className="summary-icon" aria-hidden="true">−</span>
          <p>Toplam gider</p>
          <strong>{formatMoney(summary?.expenseTotal ?? 0)}</strong>
          <span className="summary-caption">{summary?.transactionCount ?? 0} işlem kaydı</span>
          <Comparison current={summary?.expenseTotal} previous={previousSummary?.expenseTotal}
            kind="expense" previousMonthLabel={previousMonthLabel} />
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

