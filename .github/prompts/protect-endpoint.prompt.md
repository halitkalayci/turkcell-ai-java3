---
agent: agent
description: Belirtilen endpoint'i Operation-Claim modeline göre korumaya alır; controller, SecurityConfig ve OpenAPI spec'i günceller.
---

# Prompt: Endpoint Koruma

Aşağıdaki bilgileri sağla, ardından adımları sırasıyla uygula.

## Girdi Parametreleri

```
Servis        : <product-service | order-service>
HTTP Metot    : <GET | POST | PUT | PATCH | DELETE>
Endpoint      : <örn. /api/v1/products/{id}>
Claim         : <D012'den — örn. Product.Update>
```

---

## Adım 1: Doğrulama — DECISIONS.MD Kontrolü

1. `.ai/governance/DECISIONS.MD` dosyasını oku.
2. D009 tablosunda bu endpoint + HTTP metot + claim üçlüsünün eşleşip eşleşmediğini doğrula.
3. D012'de bu claim'in tanımlı olduğunu doğrula.
4. D010'da bu endpoint'in public listede olmadığını doğrula.

> Eğer herhangi bir tutarsızlık varsa DUR ve kullanıcıya bildir.

---

## Adım 2: Controller Güncelleme

İlgili controller dosyasını oku (`src/main/java/.../controller/<Servis>Controller.java`).

Controller metoduna `@PreAuthorize` anotasyonu ekle:

```java
@PreAuthorize("hasAuthority('<Claim>')")
```

Kural:
- Anotasyon metot düzeyinde tanımlanır, sınıf düzeyinde değil.
- Claim adı D012'den birebir alınır; yorumlanamaz.
- Metodun üzerindeki mevcut Swagger anotasyonları (`@Operation` vb.) korunur.

---

## Adım 3: SecurityFilterChain Kontrolü

`SecurityConfig.java` dosyasını oku.

Kontrol et:
- `anyRequest().authenticated()` mevcut mu?
- Bu endpoint `permitAll()` listesinde yanlışlıkla yer alıyor mu? → Varsa kaldır.

Değişiklik gerekiyorsa düzelt.

---

## Adım 4: OpenAPI Spec Güncelleme

`docs/openapi/<servis>-v1.yaml` dosyasını oku.

İlgili endpoint'in path + method bloğuna şunu ekle:

```yaml
security:
  - bearerAuth: []
```

`components.securitySchemes.bearerAuth` tanımlı değilse şunu ekle:

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

---

## Adım 5: Doğrulama Adımı

Aşağıdaki senaryoları listele (implementasyon değil, test rehberi):

| Senaryo | Beklenen HTTP Kodu |
|---|---|
| Geçerli JWT + doğru claim | 200 / 201 |
| Geçerli JWT + yanlış claim | 403 |
| Token yok | 401 |

---

## Çıktı Formatı

**Dosya Dökümü:**
- Değiştirilen dosyalar (yol yok, sadece dosya adı)

**Özet:**
- Hangi DECISIONS.MD kararı gereği yapıldı (D009, D012 referansları)

**Happy-path test:**
```bash
# Geçerli token al ve endpoint'i çağır
```
