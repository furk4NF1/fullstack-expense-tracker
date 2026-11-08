import { useEffect, useState } from 'react';
import { expenseApi } from './api/expenseApi.js';
import BudgetPanel from './components/BudgetPanel.jsx';
import ExpenseForm from './components/ExpenseForm.jsx';
import ExpenseList from './components/ExpenseList.jsx';
import SummaryPanel from './components/SummaryPanel.jsx';
import { downloadMonthlyCsv } from './utils/exportCsv.js';

function currentMonth() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

function monthTitle(value) {
  const [year, month] = value.split('-').map(Number);
  return new Intl.DateTimeFormat('tr-TR', { month: 'long', year: 'numeric' })
    .format(new Date(year, month - 1, 1));
}

export default function App() {
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);
  const [categories, setCategories] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [summary, setSummary] = useState(null);
  const [editingExpense, setEditingExpense] = useState(null);
  const [formKey, setFormKey] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [savingBudgetCategoryId, setSavingBudgetCategoryId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadDashboard() {
    setIsLoading(true);
    setError('');
    try {
      const [categoryData, expenseData, summaryData, budgetData] = await Promise.all([
        expenseApi.getCategories(),
        expenseApi.getExpenses(selectedMonth),
        expenseApi.getSummary(selectedMonth),
        expenseApi.getBudgets(selectedMonth)
      ]);
      setCategories(categoryData);
      setExpenses(expenseData);
      setSummary(summaryData);
      setBudgets(budgetData);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, [selectedMonth]);

  async function saveExpense(payload) {
    setIsSaving(true);
    setError('');
    setNotice('');
    try {
      if (editingExpense) {
        await expenseApi.updateExpense(editingExpense.id, payload);
      } else {
        await expenseApi.createExpense(payload);
      }
      setEditingExpense(null);
      setFormKey((key) => key + 1);
      await loadDashboard();
      setNotice('İşlem kaydedildi.');
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setIsSaving(false);
    }
  }

  async function deleteExpense(expense) {
    const confirmed = window.confirm(`“${expense.description}” kaydını silmek istiyor musun?`);
    if (!confirmed) return;
    setError('');
    setNotice('');
    try {
      await expenseApi.deleteExpense(expense.id);
      if (editingExpense?.id === expense.id) setEditingExpense(null);
      await loadDashboard();
      setNotice('İşlem silindi.');
    } catch (deleteError) {
      setError(deleteError.message);
    }
  }

  async function saveBudget(categoryId, amount) {
    setSavingBudgetCategoryId(categoryId);
    setError('');
    setNotice('');
    try {
      await expenseApi.saveBudget(categoryId, selectedMonth, amount);
      await loadDashboard();
      setNotice('Aylık bütçe kaydedildi.');
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setSavingBudgetCategoryId(null);
    }
  }

  async function deleteBudget(budget) {
    const confirmed = window.confirm(`${budget.categoryName} kategorisinin bu ayki bütçe limitini kaldırmak istiyor musun?`);
    if (!confirmed) return;
    setSavingBudgetCategoryId(budget.categoryId);
    setError('');
    setNotice('');
    try {
      await expenseApi.deleteBudget(budget.categoryId, selectedMonth);
      await loadDashboard();
      setNotice('Aylık bütçe kaldırıldı.');
    } catch (deleteError) {
      setError(deleteError.message);
    } finally {
      setSavingBudgetCategoryId(null);
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#top" aria-label="Harcama Defterim ana sayfa">
          <span className="brand-mark" aria-hidden="true">₺</span>
          <span>harcama<span>defterim</span></span>
        </a>

        <div className="sidebar-section-label">ÇALIŞMA ALANI</div>
        <a className="nav-link active" href="#dashboard"><span aria-hidden="true">▦</span> Genel bakış</a>
        <a className="nav-link" href="#budgets"><span aria-hidden="true">◷</span> Bütçeler</a>
        <div className="sidebar-note">
          <span className="note-icon" aria-hidden="true">✦</span>
          <strong>Kontrol sende</strong>
          <p>Gelir ve giderlerini tek bir yerde takip et.</p>
        </div>

        <div className="sidebar-footer">
          <span className="status-dot" /> Veriler PostgreSQL’de saklanır
        </div>
      </aside>

      <main className="main-content" id="top">
        <header className="page-header" id="dashboard">
          <div>
            <p className="eyebrow">KİŞİSEL FİNANS</p>
            <h1>Genel bakış</h1>
            <p className="page-subtitle">{monthTitle(selectedMonth)} için gelir ve gider durumun.</p>
          </div>
          <div className="header-actions">
            <label className="month-picker">
              <span>Dönem</span>
              <input type="month" value={selectedMonth} onChange={(event) => setSelectedMonth(event.target.value)} />
            </label>
            <button className="export-button" type="button" disabled={isLoading}
              aria-label="Seçili ayın gelir ve gider kayıtlarını CSV olarak indir"
              onClick={() => downloadMonthlyCsv(expenses, selectedMonth)}>
              ↓ CSV indir
            </button>
          </div>
        </header>

        {error && <div className="alert error-alert" role="alert"><span>{error}</span><button type="button" onClick={loadDashboard}>Yeniden dene</button></div>}
        {notice && <div className="alert success-alert" role="status">{notice}</div>}

        <SummaryPanel summary={summary} />
        <BudgetPanel budgets={budgets} selectedMonth={selectedMonth} onSave={saveBudget}
          onDelete={deleteBudget} savingCategoryId={savingBudgetCategoryId} />

        <div className="content-grid">
          <ExpenseList expenses={expenses} onEdit={setEditingExpense} onDelete={deleteExpense} />
          <ExpenseForm key={formKey} categories={categories} selectedExpense={editingExpense} selectedMonth={selectedMonth} onSave={saveExpense}
            onCancel={() => setEditingExpense(null)} isSaving={isSaving} />
        </div>

        {isLoading && <div className="loading-indicator" role="status">Veriler yükleniyor…</div>}
        <footer className="page-footer">Gelir ve gider takibi · Kişisel kullanım için</footer>
      </main>
    </div>
  );
}
