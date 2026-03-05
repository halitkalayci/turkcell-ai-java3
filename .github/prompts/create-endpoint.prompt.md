---
agent: agent
description: Mevcut bir servise tek veya CRUD seti halinde yeni endpoint ekler; OpenAPI spec'i günceller, DTO/Service/Repository/Controller katmanlarını implement eder.
---

# Prompt: Yeni Endpoint Oluşturma

Aşağıdaki bilgileri sağla, ardından adımları sırasıyla uygula.

## Girdi Parametreleri

```
Servis              : <product-service | order-service>
Kaynak (Resource)   : <örn. products, orders>
Mod                 : <tek endpoint | CRUD seti>
```

### Endpoint Tanımları

Her endpoint için aşağıdaki tabloyu doldur. Tek endpoint modunda bir satır, CRUD setinde birden fazla satır girilir.

```
| HTTP Metot | Endpoint                       | OperationId         | Public mi? | Claim              |
|------------|--------------------------------|---------------------|------------|--------------------|
| GET        | /api/v1/products/export        | exportProducts      | Hayır      | Product.Export     |
| POST       | /api/v1/products               | createProduct       | Hayır      | Product.Create     |
```

> **Public mi?** alanı `Evet` ise Claim alanı boş bırakılır. `Hayır` ise D012'den mevcut bir claim yazılır veya henüz tanımlı değilse `YENİ` yazılır.

### Request Body Şeması (POST / PUT / PATCH endpointleri için)

Her request body gerektiren endpoint için aşağıdaki tabloyu doldur.

```
Endpoint    : <örn. POST /api/v1/products>
Şema Adı    : <örn. ProductCreateRequest>

| Alan Adı     | Tip       | Zorunlu | Constraint                          |
|--------------|-----------|---------|-------------------------------------|
| name         | string    | Evet    | minLength: 2, maxLength: 150        |
| price        | number    | Evet    | exclusiveMinimum: 0                 |
```

### Response Şeması

Her endpoint için aşağıdakilerden birini belirt.

```
Endpoint        : <örn. GET /api/v1/products>
Mevcut Şema mı? : <Evet → şema adı | Hayır → yeni şema tanımı>
Pagination       : <Evet | Hayır>

Yeni şema gerekiyorsa:
Şema Adı    : <örn. ProductExportResponse>

| Alan Adı     | Tip       |
|--------------|-----------|
| id           | string    |
| name         | string    |
```

### Hata Kodları

```
| HTTP Status | Hata Kodu              | Açıklama                          |
|-------------|------------------------|-----------------------------------|
| 404         | PRODUCT_NOT_FOUND      | Ürün bulunamadı                   |
| 400         | VALIDATION_ERROR       | İstek doğrulama hatası            |
```

### Feign Çağrısı (Opsiyonel)

```
Hedef Servis  : <örn. product-service>
Endpoint      : <örn. GET /api/v1/products/{id}>
Amaç          : <örn. Sipariş oluştururken ürün stok kontrolü>
```

---

## Adım 1: Ön Kontrol — DECISIONS.MD

`.ai/governance/DECISIONS.MD` dosyasını oku.

Kontrol et:
- D009 tablosunda girdi olarak verilen endpoint + HTTP metot çifti zaten var mı? → Varsa DUR, kullanıcıya bildir.
- D010'daki public endpoint listesi ile girdi parametrelerindeki `Public mi?` alanı tutarlı mı? → Tutarsızlık varsa DUR, açıkla.
- Korumalı endpointlerin claim'i D012'de tanımlı mı? → `YENİ` yazılmışsa DUR, kullanıcıya `add-claim.prompt.md` ile önce claim eklenmesi gerektiğini bildir.

> Tüm kontroller geçerse devam etmek için insan onayı al.

---

## Adım 2: OpenAPI Spec Güncelleme (CONTRACT FIRST)

`docs/openapi/<servis>-v1.yaml` dosyasını oku.

Her endpoint için `paths` bloğuna yeni path + method tanımı ekle.

Kurallar:
- Mevcut spec'teki kalıplara (tag, response formatı, `$ref` kullanımı) uyum zorunludur.
- Request body gerektiren endpointlerde `requestBody` şeması `components.schemas` altına eklenir.
- Yeni response şeması gerekiyorsa `components.schemas` altına eklenir.
- Pagination dönen endpointlerde mevcut `*Page` şema kalıbı kullanılır (`items`, `page`, `size`, `totalItems`, `totalPages`).
- Hata yanıtları mevcut `ErrorResponse` şemasına `$ref` ile bağlanır.
- UUID alanlar `type: string, format: uuid` ile tanımlanır.
- Tarih alanları `type: string, format: date-time` ile tanımlanır.
- Validation constraint'leri (`minLength`, `maxLength`, `minimum`, `exclusiveMinimum`, `additionalProperties: false`) girdi parametrelerine uygun eklenir.
- Public endpoint → `security: []`
- Korumalı endpoint → `security: - bearerAuth: []`
- `components.securitySchemes.bearerAuth` tanımlı değilse eklenir.

> Spec değişikliği tamamlanmadan implementasyona geçilemez (CONTRACT FIRST — AGENTS.MD Bölüm 2).
> Devam etmek için insan onayı al.

---

## Adım 3: DTO Oluşturma

Girdi parametrelerindeki request/response şemalarına göre DTO dosyalarını oluştur.

**Request DTO** (`dto/request/` paketi):
- Jakarta validation anotasyonları eklenir (`@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@DecimalMin` vb.).
- `additionalProperties: false` → fazla alan kabul edilmez.
- Getter/setter ile plain POJO; Lombok kullanılmaz.

**Response DTO** (`dto/response/` paketi):
- Validation anotasyonu eklenmez.
- Getter/setter ile plain POJO; Lombok kullanılmaz.

**Page Response** (pagination gereken endpointlerde):
- Mevcut `*Page` DTO kalıbı kullanılır; yoksa aynı yapıda yeni oluşturulur (`items`, `page`, `size`, `totalItems`, `totalPages`).

---

## Adım 4: Service Katmanı

`service/<Resource>Service.java` interface dosyasını oku.

Yeni metot imzalarını ekle.

`service/impl/<Resource>ServiceImpl.java` implementasyon dosyasını oku.

Yeni metotları implement et.

Kurallar:
- Read operasyonları → `@Transactional(readOnly = true)`
- Write operasyonları → `@Transactional`
- İş kuralı ihlallerinde domain-spesifik exception fırlat.
- Feign çağrısı gerekiyorsa ilgili Feign client'ı kullan.

---

## Adım 5: Repository Katmanı

`repository/<Resource>Repository.java` dosyasını oku.

Yeni sorgu gerekiyorsa Spring Data method naming veya `@Query` JPQL ile ekle.

Yeni sorgu gerekmiyorsa bu adımı atla.

---

## Adım 6: Mapper Güncelleme

`mapper/<Resource>Mapper.java` dosyasını oku.

Yeni DTO ↔ Entity dönüşüm metotlarını ekle (`toResponse`, `toEntity`, `updateEntity`, `toPage` vb.).

Kurallar:
- Manuel mapping; MapStruct kullanılmaz.
- `@Component` sınıfı kalıbı korunur.

---

## Adım 7: Controller Metot Ekleme

`controller/<Resource>Controller.java` dosyasını oku.

Her endpoint için yeni controller metodu ekle.

Kurallar:
- Korumalı endpoint → `@PreAuthorize("hasAuthority('<Claim>')")` metot düzeyinde eklenir. Claim adı D012'den birebir alınır.
- Public endpoint → `@PreAuthorize` eklenmez.
- POST → `ResponseEntity.created(location).body(response)` ile `Location` header döner, HTTP 201.
- DELETE → `ResponseEntity.noContent().build()`, HTTP 204.
- GET / PUT / PATCH → `ResponseEntity.ok(response)`, HTTP 200.
- `@Valid @RequestBody` request body'li metotlarda zorunludur.
- Pagination parametreleri → `@RequestParam @Min(0) int page`, `@RequestParam @Min(1) @Max(50) int size`.
- Mevcut Swagger anotasyonları (`@Operation`, `@ApiResponse` vb.) varsa kalıp korunur.

---

## Adım 8: Exception Handling

Yeni domain-spesifik hata kodları varsa:

1. `exception/` paketine yeni `RuntimeException` alt sınıfı oluştur.
2. `GlobalExceptionHandler.java`'ya `@ExceptionHandler` metodu ekle; `ErrorResponse` döndür.

Mevcut exception'lar yeterliyse bu adımı atla.

---

## Adım 9: SecurityConfig Kontrolü

Public endpoint eklendiyse:

1. İlgili servisin `config/SecurityConfig.java` dosyasını oku.
2. `permitAll()` listesine yeni public endpoint'i ekle.
3. `gateway-server` altındaki `SecurityConfig.java` dosyasını oku ve aynı endpoint'i `permitAll()` listesine ekle.
4. DECISIONS.MD D010 tablosuna yeni public endpoint'in eklenmesi gerektiğini kullanıcıya bildir ve onay al.

Korumalı endpoint eklendiyse:
- `anyRequest().authenticated()` mevcut olduğunu doğrula.
- Eklenen endpoint'in `permitAll()` listesinde yanlışlıkla yer almadığını kontrol et.

---

## Adım 10: Doğrulama Tablosu

Her endpoint için aşağıdaki test senaryolarını listele:

**Korumalı endpoint:**

| Senaryo | Beklenen HTTP Kodu |
|---|---|
| Geçerli JWT + doğru claim | 200 / 201 / 204 |
| Geçerli JWT + yanlış claim | 403 |
| Token yok | 401 |

**Public endpoint:**

| Senaryo | Beklenen HTTP Kodu |
|---|---|
| Token yok | 200 |
| Geçerli JWT | 200 |

---

## Batch Kuralı

Dosya oluşturma/değiştirme işlemleri birbiriyle alakalı **maksimum 5 dosyalık** gruplar halinde yapılır (AGENTS.MD Bölüm 1.2). Her batch arasında kullanıcıdan onay beklenir.

Önerilen batch sıralaması:
1. **Batch 1:** OpenAPI spec + DECISIONS.MD (varsa D010 güncellemesi)
2. **Batch 2:** Request DTO + Response DTO + Page DTO (varsa)
3. **Batch 3:** Service interface + Service impl + Repository (varsa)
4. **Batch 4:** Mapper + Controller
5. **Batch 5:** Exception sınıfları + GlobalExceptionHandler + SecurityConfig (varsa)

---

## Çıktı Formatı

**Dosya Dökümü:**
- Oluşturulan/değiştirilen dosyalar (yol yok, sadece dosya adı)

**Özet:**
- Hangi endpoint'ler eklendi (metot + path)
- Hangi claim'ler kullanıldı veya `add-claim.prompt.md`'ye yönlendirildi

**Sorular (varsa):**
- Eksik şema detayları, belirsiz iş kuralları

**Happy-path test:**
```bash
# İlgili servisi başlat
cd <servis>
mvn spring-boot:run

# Public endpoint testi (token gerekmez)
curl http://localhost:<port>/api/v1/<resource>

# Korumalı endpoint testi (token gerekir)
curl -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '<request body>' \
     http://localhost:<port>/api/v1/<resource>
```
