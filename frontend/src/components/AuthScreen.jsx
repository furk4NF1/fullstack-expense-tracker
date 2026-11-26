import { useState } from 'react';
import { expenseApi } from '../api/expenseApi.js';

export default function AuthScreen({ onAuthenticated, initialError = '' }) {
  const [isRegistering, setIsRegistering] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(initialError);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      const user = await (isRegistering
        ? expenseApi.register({ email, password })
        : expenseApi.login({ email, password }));
      onAuthenticated(user);
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="auth-title">
        <a className="brand auth-brand" href="#top" aria-label="Harcama Defterim">
          <span className="brand-mark" aria-hidden="true">₺</span>
          <span>harcama<span>defterim</span></span>
        </a>
        <p className="eyebrow">KİŞİSEL FİNANS</p>
        <h1 id="auth-title">{isRegistering ? 'Hesabını oluştur' : 'Tekrar hoş geldin'}</h1>
        <p className="auth-subtitle">
          {isRegistering ? 'Gelir ve giderlerini güvenle takip etmeye başla.' : 'Devam etmek için hesabına giriş yap.'}
        </p>

        {isRegistering && (
          <p className="legacy-data-note">
            Bu veritabanındaki mevcut kayıtlar ilk oluşturulan hesaba aktarılır. Sonraki hesaplar kendi verilerini görür.
          </p>
        )}

        {error && <div className="auth-error" role="alert">{error}</div>}

        <form className="auth-form" onSubmit={submit}>
          <label htmlFor="auth-email">E-posta</label>
          <input id="auth-email" type="email" autoComplete="email" required maxLength="254"
            value={email} onChange={(event) => setEmail(event.target.value)} />
          <label htmlFor="auth-password">Şifre</label>
          <input id="auth-password" type="password"
            autoComplete={isRegistering ? 'new-password' : 'current-password'} required
            minLength={isRegistering ? 10 : undefined} maxLength="72"
            value={password} onChange={(event) => setPassword(event.target.value)} />
          {isRegistering && <p className="password-hint">En az 10 karakter kullan.</p>}
          <button className="primary-button auth-submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Lütfen bekle…' : isRegistering ? 'Hesap oluştur' : 'Giriş yap'}
          </button>
        </form>

        <p className="auth-switch">
          {isRegistering ? 'Zaten hesabın var mı?' : 'Henüz hesabın yok mu?'}{' '}
          <button type="button" onClick={() => { setIsRegistering(!isRegistering); setError(''); }}>
            {isRegistering ? 'Giriş yap' : 'Hesap oluştur'}
          </button>
        </p>
      </section>
    </main>
  );
}
