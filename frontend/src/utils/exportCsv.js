function csvCell(value) {
  const text = String(value ?? '');
  const safeText = /^[\u0000-\u0020]*[=+@-]/.test(text) ? `'${text}` : text;
  return `"${safeText.replaceAll('"', '""')}"`;
}

function formatCsvAmount(value) {
  return Number(value).toFixed(2).replace('.', ',');
}

function csvNumericCell(value) {
  return `"${formatCsvAmount(value)}"`;
}

function formatCsvDate(value) {
  const [year, month, day] = value.split('-');
  return `${day}.${month}.${year}`;
}

function downloadCsv(rows, fileName) {
  const contents = `\uFEFF${rows.map((row) => row.map((cell) => (
    typeof cell === 'number' ? csvNumericCell(cell) : csvCell(cell)
  )).join(';')).join('\r\n')}`;
  const file = new Blob([contents], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(file);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.hidden = true;
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function downloadMonthlyCsv(expenses, month) {
  const rows = [
    ['İşlem Türü', 'Tutar (TL)', 'Açıklama', 'Tarih', 'Kategori'],
    ...expenses.map((expense) => [
      expense.type === 'INCOME' ? 'Gelir' : 'Gider',
      formatCsvAmount(expense.amount),
      expense.description,
      formatCsvDate(expense.expenseDate),
      expense.categoryName
    ])
  ];
  downloadCsv(rows, `gelir-gider-${month}.csv`);
}

export function downloadAnnualCsv(report) {
  const monthTotals = new Map((report.months ?? []).map((month) => [month.month, month]));
  const rows = [
    [`Ay (${report.year})`, 'Gelir (TL)', 'Gider (TL)', 'Net Bakiye (TL)'],
    ...Array.from({ length: 12 }, (_, index) => {
      const monthNumber = index + 1;
      const month = monthTotals.get(monthNumber) ?? {};
      const income = Number(month.incomeTotal ?? 0);
      const expense = Number(month.expenseTotal ?? 0);
      const label = new Intl.DateTimeFormat('tr-TR', { month: 'long' })
        .format(new Date(report.year, index, 1));
      return [label, income, expense, income - expense];
    }),
    [
      'Yıllık toplam',
      Number(report.incomeTotal),
      Number(report.expenseTotal),
      Number(report.netBalance)
    ]
  ];
  downloadCsv(rows, `gelir-gider-yillik-${report.year}.csv`);
}

