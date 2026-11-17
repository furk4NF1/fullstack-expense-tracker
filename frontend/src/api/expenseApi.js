const API_ROOT = '/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_ROOT}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers
    }
  });

  if (response.status === 204) return null;
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const details = Object.values(body.fieldErrors || {}).join(' ');
    throw new Error(details || body.message || 'İstek tamamlanamadı.');
  }
  return body;
}

export const expenseApi = {
  getCategories: () => request('/categories'),
  createCategory: (category) => request('/categories', {
    method: 'POST', body: JSON.stringify(category)
  }),
  getExpenses: (month) => request(`/expenses?month=${encodeURIComponent(month)}`),
  getSummary: (month) => request(`/expenses/summary?month=${encodeURIComponent(month)}`),
  getAnnualSummary: (year) => request(`/expenses/annual-summary?year=${encodeURIComponent(year)}`),
  getBudgets: (month) => request(`/budgets?month=${encodeURIComponent(month)}`),
  saveBudget: (categoryId, month, amount) => request(
    `/budgets/${categoryId}?month=${encodeURIComponent(month)}`,
    { method: 'PUT', body: JSON.stringify({ amount }) }
  ),
  deleteBudget: (categoryId, month) => request(
    `/budgets/${categoryId}?month=${encodeURIComponent(month)}`,
    { method: 'DELETE' }
  ),
  createExpense: (expense) => request('/expenses', {
    method: 'POST', body: JSON.stringify(expense)
  }),
  updateExpense: (id, expense) => request(`/expenses/${id}`, {
    method: 'PUT', body: JSON.stringify(expense)
  }),
  deleteExpense: (id) => request(`/expenses/${id}`, { method: 'DELETE' })
};

