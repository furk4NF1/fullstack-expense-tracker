const API_ROOT = '/api';
let csrfToken = null;

async function getCsrfToken() {
  if (csrfToken) return csrfToken;
  const response = await fetch(`${API_ROOT}/auth/csrf`, { credentials: 'include' });
  const body = await response.json().catch(() => ({}));
  if (!response.ok || !body.token) throw new Error('Güvenli bağlantı başlatılamadı. Sayfayı yenileyip tekrar dene.');
  csrfToken = body.token;
  return csrfToken;
}

async function request(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...options.headers
  };
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    headers['X-XSRF-TOKEN'] = await getCsrfToken();
  }

  const response = await fetch(`${API_ROOT}${path}`, {
    ...options,
    method,
    credentials: 'include',
    headers
  });

  if (response.status === 204) return null;
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const details = Object.values(body.fieldErrors || {}).join(' ');
    const error = new Error(details || body.message || 'İstek tamamlanamadı.');
    error.status = response.status;
    throw error;
  }
  return body;
}

export const expenseApi = {
  forgotPassword: (email) => request('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) }),
  resetPassword: (token, password) => request('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, password }) }),
  getSavingsGoals: () => request('/savings-goals'),
  createSavingsGoal: (goal) => request('/savings-goals', { method: 'POST', body: JSON.stringify(goal) }),
  updateSavingsGoal: (id, goal) => request(`/savings-goals/${id}`, { method: 'PUT', body: JSON.stringify(goal) }),
  deleteSavingsGoal: (id) => request(`/savings-goals/${id}`, { method: 'DELETE' }),
  getCurrentUser: async () => {
    try {
      return await request('/auth/me');
    } catch (error) {
      if (error.status === 401) return null;
      throw error;
    }
  },
  login: (credentials) => request('/auth/login', {
    method: 'POST', body: JSON.stringify(credentials)
  }),
  register: (credentials) => request('/auth/register', {
    method: 'POST', body: JSON.stringify(credentials)
  }),
  logout: async () => {
    await request('/auth/logout', { method: 'POST' });
    csrfToken = null;
  },
  getCategories: () => request('/categories'),
  createCategory: (category) => request('/categories', {
    method: 'POST', body: JSON.stringify(category)
  }),
  updateCategory: (id, category) => request(`/categories/${id}`, {
    method: 'PUT', body: JSON.stringify(category)
  }),
  deleteCategory: (id) => request(`/categories/${id}`, { method: 'DELETE' }),
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
  getRecurringTransactions: () => request('/recurring-transactions'),
  createRecurringTransaction: (transaction) => request('/recurring-transactions', {
    method: 'POST', body: JSON.stringify(transaction)
  }),
  updateRecurringTransaction: (id, transaction) => request(`/recurring-transactions/${id}`, {
    method: 'PUT', body: JSON.stringify(transaction)
  }),
  deleteRecurringTransaction: (id) => request(`/recurring-transactions/${id}`, { method: 'DELETE' }),
  applyRecurringTransactions: (month) => request(
    `/recurring-transactions/apply?month=${encodeURIComponent(month)}`,
    { method: 'POST' }
  ),
  createExpense: (expense) => request('/expenses', {
    method: 'POST', body: JSON.stringify(expense)
  }),
  updateExpense: (id, expense) => request(`/expenses/${id}`, {
    method: 'PUT', body: JSON.stringify(expense)
  }),
  deleteExpense: (id) => request(`/expenses/${id}`, { method: 'DELETE' })
};
