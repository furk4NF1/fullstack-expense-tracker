# Kişisel Gelir-Gider Takipçisi

React, Java Spring Boot ve PostgreSQL ile hazırlanan katmanlı bir web uygulaması. Gelir ve gider kayıtları tarih ve kategoriyle saklanır; seçilen ayın toplamı ve kategori dağılımı gösterilir.

## Özellikler

- Gelir ve gider ekleme, düzenleme ve silme
- Kategori ekleme, adını ve rengini düzenleme
- Kullanılmayan kategorileri silme; harcama veya bütçede kullanılan kategoriler korunur
- Kategori ve tarihe göre aylık kayıt listesi
- Aylık gelir, gider ve net bakiye özeti
- Gelir ve giderlerin önceki aya göre yüzde değişimi ve net bakiye karşılaştırması
- Kategori bazında aylık dağılım
- Yıla göre aylık gelir-gider grafiği ve yıllık toplamlar
- İşlem açıklamasına veya kategorisine göre arama ve gelir/gider filtresi
- İşlem listesinde kategori filtresi ve tüm filtreleri tek tıkla temizleme
- Her ay ve kategori için ayrı harcama bütçesi ve limit aşımı uyarısı
- Maaş veya kira gibi aylık tekrarlanan işlemleri tanımlama ve seçilen aya uygulama
- Seçili ayın işlem kayıtlarını Excel uyumlu CSV dosyası olarak indirme
- Form doğrulama ve anlaşılır API hata yanıtları
- E-posta ve parola ile hesap oluşturma, güvenli oturum açma ve çıkış
- Her hesabın yalnızca kendi kategori, gelir-gider ve bütçe kayıtlarına erişmesi
- PostgreSQL’de kalıcı veri saklama ve Flyway ile şema oluşturma
- React ve Spring Boot katmanlarının ayrı tutulması

## Teknolojiler

- Java 17, Spring Boot 4.1, Spring Security, Spring Web MVC, Spring Data JPA
- React 19 ve Vite 7
- PostgreSQL 18
- Docker Compose

## Katmanlar

```text
React: component → API client
Spring Boot: controller → service → repository → PostgreSQL
                                   ↘ entity / DTO
```

Controller HTTP isteklerini alır, service iş kurallarını uygular, repository veritabanına erişir. Entity sınıfları veritabanı kayıtlarını; DTO sınıfları ise API’ye giren ve çıkan verileri temsil eder.

## Başlatma

En kolay yöntem [Docker Desktop’ı Windows’a kurduktan](https://docs.docker.com/desktop/setup/install/windows-install/) sonra proje klasöründe terminal açıp şu komutu çalıştırmaktır:

```bash
docker compose up --build
```

İlk çalıştırmada gerekli Docker görüntüleri indirilir ve proje derlenir. Hazır olunca uygulama `http://localhost:5173` adresinde, API ise `http://localhost:8080/api` adresinde açılır. Durdurmak için terminalde `Ctrl+C` tuşlarına basıp ardından `docker compose down` çalıştır.

Docker kullanmadan parçaları ayrı ayrı çalıştırmak için Java 17 veya üzeri ve Maven 3.6.3+, Node.js 20.19+ (npm ile) ve PostgreSQL 18 gerekir. PostgreSQL için `expense_tracker` adlı veritabanı oluştur; backend varsayılan olarak `expense_user` / `expense_password` bilgileriyle `localhost:5432` adresine bağlanır. İstersen bağlantı bilgilerini `DB_URL`, `DB_USER` ve `DB_PASSWORD` ortam değişkenleriyle değiştirebilirsin.

İlk açılışta Flyway mevcut kayıtları koruyarak hesap tablolarını oluşturur. İlk oluşturulan hesap mevcut kategorileri, işlemleri ve bütçeleri devralır; sonraki hesaplar kendilerine ait varsayılan kategorilerle başlar. Her API isteği oturumdaki hesapla sınırlandırılır. Parolalar BCrypt ile özetlenir, oturum çerezi HttpOnly/SameSite ayarlarıyla gönderilir ve değişiklik yapan isteklerde CSRF koruması kullanılır. Veritabanı verileri Docker volume içinde tutulur.

## API uçları

| İşlem | Uç |
| --- | --- |
| CSRF anahtarını al | `GET /api/auth/csrf` |
| Hesap oluştur | `POST /api/auth/register` |
| Oturum aç | `POST /api/auth/login` |
| Açık oturumu getir | `GET /api/auth/me` |
| Oturumu kapat | `POST /api/auth/logout` |
| Düzenli işlemleri listele veya oluştur | `GET /api/recurring-transactions`, `POST /api/recurring-transactions` |
| Düzenli işlemi düzenle veya sil | `PUT /api/recurring-transactions/{id}`, `DELETE /api/recurring-transactions/{id}` |
| Düzenli işlemleri seçili aya uygula | `POST /api/recurring-transactions/apply?month=2026-11` |
| Kategorileri listele | `GET /api/categories` |
| Kategori ekle | `POST /api/categories` |
| Kategori düzenle | `PUT /api/categories/{id}` |
| Kullanılmayan kategoriyi sil | `DELETE /api/categories/{id}` |
| Masrafları listele | `GET /api/expenses?month=2026-11` |
| Aylık özeti getir | `GET /api/expenses/summary?month=2026-11` |
| Yıllık özeti getir | `GET /api/expenses/annual-summary?year=2026` |
| Aylık kategori bütçelerini listele | `GET /api/budgets?month=2026-11` |
| Kategori bütçesi belirle veya güncelle | `PUT /api/budgets/{categoryId}?month=2026-11` |
| Kategori bütçesini kaldır | `DELETE /api/budgets/{categoryId}?month=2026-11` |
| Masraf ekle | `POST /api/expenses` |
| Masraf düzenle | `PUT /api/expenses/{id}` |
| Masraf sil | `DELETE /api/expenses/{id}` |

Hesap oluşturma ve giriş istekleri `email` ve `password` alanlarını kullanır. Yeni parolalar en az 10 karakter olmalıdır. `POST`, `PUT` ve `DELETE` çağrılarından önce CSRF anahtarı `GET /api/auth/csrf` üzerinden alınır; React istemcisi bunu otomatik yapar.

Aylık tekrarlanan işlemler kullanıcı tarafından seçilen aya uygulanır; otomatik zamanlama yapılmaz. Bir şablon aynı aya tekrar uygulanırsa ikinci bir kayıt oluşturulmaz. Ayda bulunmayan günler ayın son gününe alınır. Şablonu silmek önceden oluşturulmuş işlemleri silmez.

`POST /api/expenses` için örnek JSON:

```json
{
  "type": "EXPENSE",
  "amount": 250.50,
  "description": "Market alışverişi",
  "expenseDate": "2026-11-05",
  "categoryId": 1
}
```

`POST /api/categories` için örnek JSON:

```json
{
  "name": "Eğitim",
  "color": "#527a68"
}
```

`PUT /api/budgets/1?month=2026-11` için örnek JSON:

```json
{
  "amount": 5000.00
}
```

## Proje yapısı

```text
backend/src/main/java/com/portfolio/expensetracker/
├── config/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
└── service/
frontend/src/
├── api/
├── components/
└── App.jsx
```

## Güvenlik notu

Bu proje öğrenme ve yerel kullanım içindir. İnternette yayınlamadan önce HTTPS, güçlü veritabanı parolaları, e-posta doğrulama, gerçek SMTP teslimatı ve giriş deneme sınırı gibi üretim ihtiyaçları ayrıca yapılandırılmalıdır.
## Birikim hedefleri

Kullanıcılar **Birikim hedefleri** bölümünde ad, hedef tutarı, mevcut birikim ve isteğe bağlı hedef tarihiyle kişisel hedef oluşturabilir; düzenleyebilir ve silebilir. İlerleme yüzdesi, kalan tutar ve tamamlanma durumu gösterilir. Hedef tarihi geçen tamamlanmamış hedefler ayrıca işaretlenir.

Birikim tutarı elle güncellenen toplamdır; gelir-gider kayıtlarından otomatik hesaplanmaz ve bakiyeyi değiştirmez. Hedefler ay filtresinden bağımsızdır. Hedef tutarından fazla birikim kaydedilebilir.

API: `GET/POST /api/savings-goals`, `PUT/DELETE /api/savings-goals/{id}`. Güncellemede son yanıttaki `version` gönderilir; eski sürümle güncelleme 409 döndürür. Tüm işlemler oturum sahibine göre sınırlandırılır. V5 geçişi mevcut kayıtları değiştirmeden yeni tabloyu oluşturur.

## Şifremi unuttum

Giriş ekranından sıfırlama bağlantısı istenebilir. E-postadaki bağlantı 30 dakika geçerlidir ve tek kullanımlıktır. Yeni bağlantı önceki bağlantıyı geçersiz kılar. Şifre yenilendiğinde mevcut oturumlar sonraki istekte kapatılır; kullanıcı yeniden giriş yapar. Mevcut finans kayıtları korunur.

- `POST /api/auth/forgot-password`: `{"email":"kullanici@example.com"}`
- `POST /api/auth/reset-password`: `{"token":"epostadaki-token","password":"yeni-sifre"}`
- Her iki istek de CSRF belirteci gerektirir. En az 10 karakter ve en fazla 72 UTF-8 bayt şifre kabul edilir.
- Rastgele 256 bit belirteçlerin yalnızca SHA-256 özetleri veritabanında tutulur. Ham bağlantı API yanıtına veya uygulama günlüğüne yazılmaz; e-postayla iletilir.
- E-posta adresinin kayıtlı olup olmadığı açıklanmaz. Teslimat sınırlı bir arka plan kuyruğunda yapılır. Her 15 dakikada IP başına 10 istek, adres başına 3 e-posta ve IP başına 30 sıfırlama denemesi sınırı vardır.
- Sınırlar tek uygulama sürecine aittir ve yeniden başlatmada sıfırlanır. Birden fazla sunucu için ortak sınırlandırıcı gerekir. İstemcinin gönderdiği yönlendirme başlıklarına güvenilmez; yerel Vite proxy üzerinden istekler aynı IP sınırını paylaşabilir.

### Gerçek e-posta gönderimi

Varsayılan olarak gönderim kapalıdır; istek tüm adreslerde açıklayıcı bir 503 yanıtı verir. SMTP kurmadan gönderildiği iddia edilmez. Sunucuyu başlatmadan önce şu ortam değişkenlerini ayarlayın:

| Değişken | Açıklama |
| --- | --- |
| PASSWORD_RESET_ENABLED | `true` |
| PASSWORD_RESET_BASE_URL | Güvenilir uygulama adresi; yerelde `http://localhost:4173/`, Docker'da `http://localhost:5173/`; internette HTTPS |
| PASSWORD_RESET_FROM | Gönderici e-posta adresi |
| SMTP_HOST / SMTP_PORT | Posta sağlayıcısının sunucusu ve STARTTLS portu (varsayılan 587) |
| SMTP_USERNAME / SMTP_PASSWORD | Posta sağlayıcısının kullanıcı adı ve uygulama şifresi |
| SMTP_AUTH / SMTP_STARTTLS | Gerçek posta hizmetinde `true`; yalnızca yerel deneme SMTP sunucusunda `false` |

Posta sağlayıcısının uygulama şifresini kullanın; sırları GitHub'a yüklemeyin. Docker Compose değişkenleri kökteki git tarafından yok sayılan `.env` dosyasından alabilir; doğrudan Maven çalıştırırken ortam değişkenlerini ayrıca ayarlayın. Bağlantı adresi isteğin Host başlığından üretilmez. Adresi değiştirince backend'i yeniden başlatın.

Posta bağlantısı için [Spring Boot e-posta yapılandırması](https://docs.spring.io/spring-boot/reference/io/email.html) kullanılır; bağlantı, okuma ve yazma zaman aşımı 5 saniyedir. Gönderim başarısız olursa belirteç iptal edilir ve kişisel bilgi içermeyen bir uyarı kaydedilir. Yerel SMTP ile teslimat, süre sonu, tek kullanım/eşzamanlı istek, eski bağlantının iptali, eski oturumların kapanması, hesap ayrımı ve CSRF kontrolleri doğrulandı. Gerçek sağlayıcıya teslimat ayrıca doğrulanmalıdır.

