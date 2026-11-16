import { useEffect, useState } from 'react';
import { expenseApi } from '../api/expenseApi.js';
import { downloadAnnualCsv } from '../utils/exportCsv.js';
import { formatMoney } from './ExpenseList.jsx';

const chartWidth = 760;
const chartHeight = 248;
const chartLeft = 62;
const chartRight = 12;
const chartTop = 12;
const chartBottom = 30;
const plotWidth = chartWidth - chartLeft - chartRight;
const plotHeight = chartHeight - chartTop - chartBottom;
const monthLabels = Array.from({ length: 12 }, (_, index) => (
  new Intl.DateTimeFormat('tr-TR', { month: 'short' })
    .format(new Date(2026, index, 1))
    .replace('.', '')
));
const compactMoney = new Intl.NumberFormat('tr-TR', {
  style: 'currency',
  currency: 'TRY',
  notation: 'compact',
  maximumFractionDigits: 1
});

export default function AnnualReport() {
  const currentYear = new Date().getFullYear();
  const [year, setYear] = useState(currentYear);
  const [report, setReport] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let isCurrent = true;
    setIsLoading(true);
    setError('');

    expenseApi.getAnnualSummary(year)
      .then((data) => {
        if (isCurrent) setReport(data);
      })
      .catch((loadError) => {
        if (isCurrent) setError(loadError.message);
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false);
      });

    return () => {
      isCurrent = false;
    };
  }, [year]);

  const months = report?.months ?? [];
  const maximum = Math.max(0, ...months.flatMap((month) => [
    Number(month.incomeTotal), Number(month.expenseTotal)
  ]));
  const scaleMaximum = maximum || 1;
  const hasTransactions = maximum > 0;
  const years = Array.from({ length: 6 }, (_, index) => currentYear - index);
  const groupWidth = plotWidth / 12;
  const barWidth = Math.min(18, groupWidth * 0.31);
  const barGap = 4;

  return (
    <section className="panel annual-report" aria-labelledby="annual-report-title">
      <div className="panel-heading annual-heading">
        <div>
          <p className="eyebrow">YILLIK RAPOR</p>
          <h2 id="annual-report-title">Gelir ve gider trendi</h2>
          <p className="annual-subtitle">Aylara göre toplam gelir ve gider karşılaştırması</p>
        </div>
        <div className="annual-controls">
          <label className="annual-year-picker">
            <span>Yıl</span>
            <select value={year} onChange={(event) => setYear(Number(event.target.value))}>
              {years.map((option) => <option key={option} value={option}>{option}</option>)}
            </select>
          </label>
          <button className="export-button annual-export-button" type="button"
            disabled={isLoading || Boolean(error) || !report}
            aria-label={`${year} yılı aylık gelir ve gider raporunu CSV olarak indir`}
            onClick={() => downloadAnnualCsv(report)}>
            ↓ Yıllık CSV
          </button>
        </div>
      </div>

      <div className="annual-totals" aria-live="polite">
        <article className="annual-total-card">
          <span>Yıllık gelir</span>
          <strong className="annual-income-total">{formatMoney(report?.incomeTotal ?? 0)}</strong>
        </article>
        <article className="annual-total-card">
          <span>Yıllık gider</span>
          <strong className="annual-expense-total">{formatMoney(report?.expenseTotal ?? 0)}</strong>
        </article>
        <article className="annual-total-card">
          <span>Net bakiye</span>
          <strong>{formatMoney(report?.netBalance ?? 0)}</strong>
        </article>
      </div>

      {error && <div className="alert error-alert annual-error" role="alert">{error}</div>}
      {isLoading && <p className="muted-copy annual-status" role="status">Yıllık rapor yükleniyor…</p>}

      <div className="annual-chart-scroll">
        <svg className="annual-chart" viewBox={`0 0 ${chartWidth} ${chartHeight}`} role="img"
          aria-label={`${year} yılına göre aylık gelir ve gider grafiği`}>
          <title>{year} yılı için aylık gelir ve gider</title>
          <desc>Yeşil sütunlar geliri, turuncu sütunlar gideri gösterir.</desc>
          {[0, 1, 2, 3, 4].map((tick) => {
            const ratio = tick / 4;
            const y = chartTop + plotHeight * ratio;
            const value = maximum * (1 - ratio);
            return (
              <g key={tick}>
                <line className="annual-gridline" x1={chartLeft} x2={chartWidth - chartRight}
                  y1={y} y2={y} />
                <text className="annual-axis-label" x={chartLeft - 8} y={y + 3}
                  textAnchor="end">{maximum ? compactMoney.format(value) : '0'}</text>
              </g>
            );
          })}
          {monthLabels.map((label, index) => {
            const month = months[index] ?? { incomeTotal: 0, expenseTotal: 0 };
            const income = Number(month.incomeTotal);
            const expense = Number(month.expenseTotal);
            const incomeHeight = income / scaleMaximum * plotHeight;
            const expenseHeight = expense / scaleMaximum * plotHeight;
            const center = chartLeft + index * groupWidth + groupWidth / 2;
            const baseline = chartTop + plotHeight;

            return (
              <g key={index}>
                <rect className="annual-income-bar" x={center - barWidth - barGap / 2}
                  y={baseline - incomeHeight} width={barWidth} height={incomeHeight} rx="3">
                  <title>{label} gelir: {formatMoney(income)}</title>
                </rect>
                <rect className="annual-expense-bar" x={center + barGap / 2}
                  y={baseline - expenseHeight} width={barWidth} height={expenseHeight} rx="3">
                  <title>{label} gider: {formatMoney(expense)}</title>
                </rect>
                <text className="annual-month-label" x={center} y={chartHeight - 8}
                  textAnchor="middle">{label}</text>
              </g>
            );
          })}
        </svg>
      </div>

      <div className="annual-legend" aria-label="Grafik açıklaması">
        <span><i className="annual-legend-income" />Gelir</span>
        <span><i className="annual-legend-expense" />Gider</span>
      </div>
      {!isLoading && !error && !hasTransactions && (
        <p className="muted-copy annual-status">Bu yıl için henüz kayıt yok. Gelir veya gider eklediğinde grafik burada görünecek.</p>
      )}
    </section>
  );
}

