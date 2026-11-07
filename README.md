# Kişisel Gelir-Gider Takipçisi

React, Java Spring Boot ve PostgreSQL ile hazırlanan katmanlı bir web uygulaması. Gelir ve gider kayıtları tarih ve kategoriyle saklanır; seçilen ayın toplamı ve kategori dağılımı gösterilir.

## Özellikler

- Gelir ve gider ekleme, düzenleme ve silme
- Kategori ve tarihe göre aylık kayıt listesi
- Aylık gelir, gider ve net bakiye özeti
- Kategori bazında aylık dağılım
- Her ay ve kategori için ayrı harcama bütçesi ve limit aşımı uyarısı
- Form doğrulama ve anlaşılır API hata yanıtları
- PostgreSQL’de kalıcı veri saklama ve Flyway ile şema oluşturma
- React ve Spring Boot katmanlarının ayrı tutulması

## Teknolojiler

- Java 17, Spring Boot 4.1, Spring Web MVC, Spring Data JPA
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

Veritabanı verileri Docker volume içinde tutulur. Yerel öğrenme ortamındaki PostgreSQL erişimi `localhost:5432` adresindedir. Uygulama başlangıç kategorilerini Flyway migration dosyasından ekler.

## API uçları

| İşlem | Uç |
| --- | --- |
| Kategorileri listele | `GET /api/categories` |
| Masrafları listele | `GET /api/expenses?month=2026-11` |
| Aylık özeti getir | `GET /api/expenses/summary?month=2026-11` |
| Aylık kategori bütçelerini listele | `GET /api/budgets?month=2026-11` |
| Kategori bütçesi belirle veya güncelle | `PUT /api/budgets/{categoryId}?month=2026-11` |
| Kategori bütçesini kaldır | `DELETE /api/budgets/{categoryId}?month=2026-11` |
| Masraf ekle | `POST /api/expenses` |
| Masraf düzenle | `PUT /api/expenses/{id}` |
| Masraf sil | `DELETE /api/expenses/{id}` |

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

## Sonraki geliştirmeler

Kullanıcı hesabı ve oturum açma, CSV dışa aktarma ve çevrimiçi yayınlama sonraki sürümlere eklenebilir. İlk sürüm kişisel kullanım içindir; örnek veritabanı parolalarını internette yayınlanan bir sunucuda kullanma.
