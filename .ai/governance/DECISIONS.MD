### DECISIONS.MD 

ADR'lerin (Artichectural Decision Record) yer aldığı dosya.

Bu döküman insan onayından geçmiş teknik kararları içerir ve konfirme eder.

---

#### D001 - Build Tool

**Son Güncelleme**: 04.03.2026

**Karar:** Maven

**Neden:** Takım standartlarına uygunluk.

**Alternatifler:** Gradle

---

#### D002 - Java Version

**Son Güncelleme**: 04.03.2026

**Karar:** 21

**Neden:** LTS + Modern dil desteği.

---

#### D003 - Database

**Son Güncelleme**: 04.03.2026

**Karar:** H2 In-Memory

**Neden:** Kolay kurulum

---

#### D004 - Swagger Libraries

**Son Güncelleme**: 04.03.2026

**Karar:** springdoc-openapi-starter-webmvc-ui - 2.3.0

**Neden:** En uyumlu versiyon

---

#### D005 - Registry Server

**Son Güncelleme**: 04.03.2026

**Karar:** Netflix Eurea Client

**Neden:** En yaygın ve problemsiz kütüphane

---

#### D005 - Spring Cloud 

**Son Güncelleme**: 04.03.2026

**Karar:** 2024.0.0

**Neden:** En yaygın ve problemsiz versiyon.

---

#### D006 - Sync İletişim Kütüphanesi

**Son Güncelleme**: 05.03.2026

**Karar:** `spring-cloud-starter-openfeign` (Spring Cloud BOM 2024.0.1 kapsamında)

**Neden:** Eureka servis keşfiyle entegre, deklaratif HTTP client tanımı, Spring Boot ekosistemiyle tam uyumlu. Hardcoded URL yerine `name = "<service-name>"` ile Eureka üzerinden çözümleme yapılır.

**Alternatifler:** RestTemplate (manuel URL yönetimi gerektirir), WebClient (reaktif bağımlılık gerektirir — mevcut projede reaktif yok)

---

#### D007 - Auth Framework

**Son Güncelleme**: 05.03.2026

**Karar:** Spring Authorization Server — proje içinde bağımsız `auth-service` modülü olarak konuşlandırılır.

**Neden:** Pure Spring ekosistemi, ek altyapı (Docker, harici IdP) gerektirmez, tam OAuth2/OIDC desteği sunar. Proje standartlarıyla (Maven, Java 21, H2) tam uyumludur.

**Alternatifler:** Keycloak (Docker gerektirir, overkill), Basit JWT/custom (OAuth2 standardına uymaz)

---

#### D008 - Token Doğrulama Katmanı

**Son Güncelleme**: 05.03.2026

**Karar:** Gateway + her servis (defense-in-depth). Gateway OAuth2 Resource Server olarak JWT doğrular; `product-service` ve `order-service` de ayrı ayrı OAuth2 Resource Server olarak yapılandırılır.

**Neden:** Servisler gateway bypass edilerek direkt çağrılsa dahi (servisler arası Feign çağrıları dahil) korunur. Tek nokta güvenliğe (sadece gateway) göre daha güvenli.

**Alternatifler:** Sadece Gateway (servisler direkt erişime açık kalır)

**Yapılandırma:** Her servisin `application.yml`'ine `spring.security.oauth2.resourceserver.jwt.issuer-uri` eklenmelidir. Hardcoded URI yasaktır; config üzerinden yönetilir.

---

#### D009 - Yetkilendirme Modeli

**Son Güncelleme**: 05.03.2026

**Karar:** Operation-Claim Based yetkilendirme. JWT `authorities` claim'i içinde operasyon adları taşınır. Erişim kontrolü `@PreAuthorize("hasAuthority('<Claim>')")` anotasyonu ile her endpoint'te açıkça tanımlanır.

**Neden:** Rol bazlı yetkilendirmeye göre daha ince taneli (fine-grained) kontrol sağlar. Her operasyon bağımsız olarak atanabilir/kaldırılabilir.

**Claim Formatı:** `<Servis>.<Operasyon>` — örnek: `Product.Create`, `Order.Cancel`

**Claim → Endpoint Eşlemesi:**

| Claim | HTTP Metot | Endpoint |
|---|---|---|
| `Product.Read` | GET | `/api/v1/products` |
| `Product.Read` | GET | `/api/v1/products/{id}` |
| `Product.Create` | POST | `/api/v1/products` |
| `Product.Update` | PUT | `/api/v1/products/{id}` |
| `Product.Delete` | DELETE | `/api/v1/products/{id}` |
| `Order.Read` | GET | `/api/v1/orders` |
| `Order.Read` | GET | `/api/v1/orders/{id}` |
| `Order.Create` | POST | `/api/v1/orders` |
| `Order.Cancel` | PATCH | `/api/v1/orders/{id}/cancel` |
| `Product.UpdateStock` | PUT | `/api/v1/products/update-stock` |

**Not:** Public endpoint'ler bu tabloda yer almaz (bkz. D010).

---

#### D010 - Public Endpointler

**Son Güncelleme**: 05.03.2026

**Karar:** Aşağıdaki endpointler authentication gerektirmez:

- `GET /api/v1/products`
- `GET /api/v1/products/{id}`

Diğer tüm endpointler geçerli JWT token gerektirir.

**Neden:** Ürün listeleme/detay görüntüleme misafir kullanıcılara açık olmalıdır; sipariş ve ürün yönetimi operasyonları kimlik doğrulaması gerektirir.

**OpenAPI Yansıması:** Bu iki endpoint `security: []` ile, diğerleri `security: [bearerAuth: []]` ile işaretlenmelidir.

---

#### D011 - auth-service User Store

**Son Güncelleme**: 05.03.2026

**Karar:** H2 In-Memory

**Neden:** D003 kararıyla tutarlıdır. Ek altyapı gerektirmez.

**Alternatifler:** PostgreSQL (Docker gerektirir), InMemoryUserDetailsManager (kalıcı değil, seed data desteği kısıtlı)

---

#### D012 - Claim Kümesi

**Son Güncelleme**: 05.03.2026

**Karar:** 8 claim — tüm operasyonlar için tam kapsam.

| Claim | Açıklama |
|---|---|
| `Product.Read` | Ürün listeleme ve detay görüntüleme |
| `Product.Create` | Yeni ürün oluşturma |
| `Product.Update` | Mevcut ürün güncelleme |
| `Product.Delete` | Ürün silme |
| `Product.UpdateStock` | Ürün stok güncelleme |
| `Order.Read` | Sipariş listeleme ve detay görüntüleme |
| `Order.Create` | Yeni sipariş oluşturma |
| `Order.Cancel` | Sipariş iptal etme |

**Genişletme Kuralı:** Yeni bir claim eklendiğinde bu tablo güncellenmeli, ilgili endpoint D009 tablosuna eklenmeli ve OpenAPI spec güncellenmelidir. `add-claim.prompt.md` kullanılabilir.

---

#### D013 - Test Dosyaları Dizin Yapısı

**Son Güncelleme**: 05.03.2026

**Karar:** Postman collection dosyaları `.ai/test/postman` dizininde tutulur.

**Neden:** Test artefaktları `.ai` dizini altında merkezi olarak yönetilir; proje kök dizininde veya `docs/` altında dağınık tutulmaz.