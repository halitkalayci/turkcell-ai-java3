---
agent: agent
description: Sisteme yeni bir Operation-Claim ekler; DECISIONS.MD günceller, auth-service seed data'sını genişletir ve ilgili endpoint'i korur.
---

# Prompt: Yeni Claim Ekleme

Aşağıdaki bilgileri sağla, ardından adımları sırasıyla uygula.

## Girdi Parametreleri

```
Claim Adı     : <örn. Product.Export>
Açıklama      : <kısa açıklama — örn. "Ürün listesini dışa aktarma">
Servis        : <product-service | order-service>
HTTP Metot    : <GET | POST | PUT | PATCH | DELETE>
Endpoint      : <örn. /api/v1/products/export>
```

---

## Adım 1: Ön Kontrol — DECISIONS.MD

`.ai/governance/DECISIONS.MD` dosyasını oku.

Kontrol et:
- D012'de bu claim zaten var mı? → Varsa DUR, kullanıcıya bildir.
- D009 tablosunda bu endpoint + metot çifti var mı? → Varsa DUR, tutarsızlığı açıkla.

> Devam etmek için insan onayı al.

---

## Adım 2: DECISIONS.MD Güncelleme

**D012** tablosuna yeni satır ekle:

```markdown
| `<Claim Adı>` | <Açıklama> |
```

**D009** tablosuna yeni satır ekle:

```markdown
| `<Claim Adı>` | <HTTP Metot> | `<Endpoint>` |
```

Her iki girişte de `**Son Güncelleme**` tarihini güncelle.

---

## Adım 3: OpenAPI Spec Güncelleme

`docs/openapi/<servis>-v1.yaml` dosyasına yeni endpoint'i ekle.

Kural:
- Endpoint tanımı spec'e uygun olmalı (request/response şeması eksiksiz).
- `security: - bearerAuth: []` mutlaka eklenmelidir.
- Bu endpoint D010'da public değilse `security: []` kullanılamaz.

> Spec değişikliği tamamlanmadan implementasyona geçilemez (CONTRACT FIRST — AGENTS.MD Bölüm 2).

---

## Adım 4: auth-service Seed Data Güncelleme

`auth-service` içindeki claim seed data kaynağını oku (entity, data.sql veya V*.sql migration dosyası).

Yeni claim'i seed data'ya ekle:

```sql
INSERT INTO claims (name, description) VALUES ('<Claim Adı>', '<Açıklama>');
```

veya ilgili Java config/entity yapısına göre uygun formatta ekle.

---

## Adım 5: Endpoint Koruma

`protect-endpoint.prompt.md` dosyasındaki adımları şu parametrelerle uygula:

```
Servis     : <servis>
HTTP Metot : <metot>
Endpoint   : <endpoint>
Claim      : <Claim Adı>
```

---

## Çıktı Formatı

**Dosya Dökümü:**
- Değiştirilen/oluşturulan dosyalar (yol yok, sadece dosya adı)

**Özet:**
- D009 ve D012'ye eklenen girişler
- Hangi endpoint korundu

**Sorular (varsa):**
- Spec'te eksik request/response şema detayları

**Happy-path test:**
```bash
# Yeni claim atanmış token al ve endpoint'i çağır
```
