import { useState } from 'react';
import { expenseApi } from '../api/expenseApi.js';

export default function PasswordRecovery({ token = null, onBack }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [done, setDone] = useState(false);
  const resetting = token !== null;
  const validToken = resetting && /^[A-Za-z0-9_-]{43}$/.test(token);
  async function submit(event) {
    event.preventDefault(); setError(''); setNotice('');
    if (resetting && password !== confirm) { setError('Şifreler eşleşmiyor.'); return; }
    if (resetting && new TextEncoder().encode(password).length > 72) {
      setError('Şifre en fazla 72 bayt olabilir. Türkçe karakterler birden fazla bayt kullanabilir.'); return;
    }
    setBusy(true);
    try {
      const result = resetting ? await expenseApi.resetPassword(token, password) : await expenseApi.forgotPassword(email);
      setNotice(result.message); setDone(true); setPassword(''); setConfirm('');
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }
  return <main className="auth-page"><section className="auth-card" aria-labelledby="recovery-title">
    <p className="eyebrow">HARCAMA DEFTERİM</p>
    <h1 id="recovery-title">{resetting ? 'Yeni şifreni belirle' : 'Şifreni mi unuttun?'}</h1>
    <p className="auth-subtitle">{resetting ? 'En az 10 karakter kullan. Şifren yenilendiğinde açık oturumların sona erer.' : 'Hesabının e-posta adresini yaz; sana 30 dakika geçerli bir bağlantı gönderelim.'}</p>
    {error && <div className="auth-error" role="alert">{error}</div>}
    {notice && <p className="recurring-notice" role="status">{notice}</p>}
    {resetting && !validToken && <p className="auth-error" role="alert">Bağlantı geçersiz. Giriş ekranından yeni bir bağlantı iste.</p>}
    {!done && (!resetting || validToken) && <form className="auth-form" onSubmit={submit}>
      {resetting ? <>
        <label htmlFor="reset-password">Yeni şifre</label>
        <input id="reset-password" type="password" autoComplete="new-password" minLength="10" maxLength="72" required disabled={busy} value={password} onChange={e => setPassword(e.target.value)} />
        <label htmlFor="reset-confirm">Yeni şifre tekrar</label>
        <input id="reset-confirm" type="password" autoComplete="new-password" minLength="10" maxLength="72" required disabled={busy} value={confirm} onChange={e => setConfirm(e.target.value)} />
      </> : <>
        <label htmlFor="recovery-email">E-posta</label>
        <input id="recovery-email" type="email" autoComplete="email" maxLength="254" required disabled={busy} value={email} onChange={e => setEmail(e.target.value)} />
      </>}
      <button className="primary-button auth-submit" disabled={busy}>{busy ? 'Lütfen bekle…' : resetting ? 'Şifremi yenile' : 'Bağlantı gönder'}</button>
    </form>}
    <p className="auth-switch"><button disabled={busy} onClick={onBack}>Giriş ekranına dön</button></p>
  </section></main>;
}
