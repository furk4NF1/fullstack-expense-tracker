function csvCell(value) {
  const text = String(value ?? '');
  const safeText = /^[\u0000-\u0020]*[=+@-]/.test(text) ? `'${text}` : text;
  return `"${safeText.replaceAll('"', '""')}"`;
}

function formatCsvAmount(value) {
  return Number(value).toFixed(2).replace('.', ',');
}

function formatCsvDate(value) {
  const [year, month, day] = value.split('-');
  return `${day}.${month}.${year}`;
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
  const contents = `\uFEFF${rows.map((row) => row.map(csvCell).join(';')).join('\r\n')}`;
  const file = new Blob([contents], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(file);
  const link = document.createElement('a');
  link.href = url;
  link.download = `gelir-gider-${month}.csv`;
  link.hidden = true;
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
