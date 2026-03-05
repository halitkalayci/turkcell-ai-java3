### AGENTS.MD - Mini E-Ticaret Mikroservis Platformu

Bu repo mini e-ticaret mikroservis ekosistemini barındırır. Bu dosya bu repoda çalışacak AI ve insanlar için bir anayasa hükmündedir.

Bu projede çalışırken herhangi bir karar bulmak istersen [DECISIONS.md](.ai/governance/DECISIONS.MD) bakabilirsin.

Bu projede çalışırken mimari kurallar için [ARCHITECTURE.MD](.ai/architecture/ARCHITECTURE.MD), kodlama standartları için [CODING_STANDARDS.MD](.ai/architecture/CODING_STANDARDS.MD) bakabilirsin.

Bu projede çalışırken herhangi bir iş kuralı ile alakalı durumlar için [RULES.md]() bakabilirsin.
---

## 1. Nasıl Çalışılır? (Zorunlu İş Akışı)

### 1.1 Uydurma YOK

Eğer aşağıdaki listede herhangi bir eksik/yanlış bilgi varsa bu
bilgiyi sen uyduramazsın. Kullanıcıya bu detayı sor, netleştirdikten sonra ilerle.

- Bağımlılıklar / kütüphaneler (runtime, test scope dahil) — kullanılacak her bağımlılık açıkça onaylanmalıdır; varsayım yapılamaz.

- Kütüphane/Framework versiyonları

- endpoint, request/response alanları veya objeleri, hata modelleri

- DB şema/kolonları

- İş kuralları

Herhangi bir detay eksikse; DUR ve SOR.

### 1.2 Önce Planla, sonra kodla

Kod üretmeden önce şunları yapmak zorundasın;

- Eğer üretilecek kod ilgili OPENAPI sözleşmesini penetre ediyor ise veya sözleşme yok ise
kullanıcıdan sözleşmeyi sağlamasını iste ve DUR. Bir penetrasyon söz konusu ise nedeniyle açıkla. 

- Bir dosya dökümü hazırla (dosyaların yanına dosya yolu ekleme.) (hangi dosyalar üretilecek/değişecek + neden)

- Dosya dökümüne tüm yeni bağımlılıkları (groupId, artifactId, scope) listele ve her biri için ayrı onay bekle; onaysız pom.xml'e bağımlılık ekleme.

- Herhangi bir detay eksikse sorularını sor.

- Dosya oluşturma işlemini birbiriyle alakalı maksimum 5'er dosyalık gruplar halinde batchlere böl. Her batch arasında benden ek onay bekle.

## 2) CONTRACT FIRST

### 2.1 OpenAPI doğruluğun tek kaynağıdır (Source of Truth)

- API sözleşmeleri şu dizinde bulunur: `/docs/openapi`

- Her servis **kendi OpenAPI dosyasına sahip olmalıdır**:
  - `docs/openapi/product-service-v1.yml`
  - `docs/openapi/order-service-v1.yml`

- Swagger UI, bu sözleşmelerden üretilir.

- Implementasyon **her zaman sözleşmeye uymalıdır**; sözleşme implementasyona göre değiştirilemez.

### 2.2 Kod Üretim Politikası (Code Generation Policy)

- OpenAPI araçları **yalnızca repoda zaten mevcutsa** kullanılabilir.

- Açık onay olmadan **yeni bir OpenAPI generator bağımlılığı eklenmemelidir**.

- Eğer generator yoksa, controller ve DTO’lar **spec’e uygun olacak şekilde manuel olarak implement edilmelidir**.

### 2.3 Swagger UI

- Tüm servisler Swagger implementasyonu yapmalıdır.

### 2.4 Test Dosyaları

- Postman collection dosyaları `.ai/test/postman` dizininde tutulur.
- Bu dizin dışına Postman dosyası oluşturulamaz.

## 3) ÇIKTI FORMATI 

- Resmi bir dil kullan, emojilere, samimi dile yer verme.

- Yaptığın işi minimal cümlelere özet olarak maksimum 2-3 cümle ile özetle çok detaylı özetleme yapma.

- Senden öneri özellikle istenmediği sürece önerilere çıktıda yer verme.

- Yapılan işlerin dosya dökümü içerisine dosya yolu ekleme.

- Çıktıyı 3 bölümde özetle; dosya dökümü, yapılan değişikliğin özeti varsa sorular.

- Öneriler için şu istisnayı dikkate al; eğer önerin governance önerisi ise kesinlikle öner.

- Her implementasyon sonrası bu implementasyonu test edebileceğimiz en kısa happy-path ver.

örneğin:
```
Bir spring eureka server implementasyonu yaptıysan şunu söyle:

`cd registry-server`
`mvn spring-boot:run`
`http://localhost:8761` adresine gittiğinde eurekayı görürsen tamamdır.
```


## 4) Registry Server

- Her servis kendisini varsayılan olarak `localhost:8761` adresinde çalışan Eureka Server'a tanıtmalıdır.

## 5) Security

### 5.1 Genel Kurallar

- Güvenlik kararları [DECISIONS.MD](.ai/governance/DECISIONS.MD) içinde D007–D012 girişlerinde tanımlıdır. Bu girişlerden bağımsız karar üretilemez.

- Secret, private key, client-secret gibi hassas değerler asla kaynak koduna veya YAML dosyasına gömülemez; environment variable veya config server üzerinden yönetilir.

- Her güvenlik değişikliği ilgili OpenAPI spec dosyasına yansıtılmalıdır (bkz. 5.4).

### 5.2 Yetkilendirme — Operation-Claim Modeli (D009)

- Yetkilendirme `@PreAuthorize("hasAuthority('<Claim>')")` anotasyonu ile controller metodunda açıkça tanımlanır.

- Kullanılabilecek claim kümesi DECISIONS.MD D012'de sabittir. Bu listede olmayan bir claim kullanılamaz; yeni claim eklemek için önce D012 güncellenmeli ve insan onayı alınmalıdır.

- Claim → endpoint eşlemesi DECISIONS.MD D009 tablosundan alınır; tablodan bağımsız eşleşme üretilemez.

- Uydurma YOK kuralı burada da geçerlidir: claim adı, endpoint, HTTP metodu serbest yorumlanamaz.

### 5.3 Token Doğrulama (D008)

- Gateway ve her resource servis (`product-service`, `order-service`) ayrı ayrı OAuth2 Resource Server olarak yapılandırılır.

- `jwk-set-uri` veya `issuer-uri` değeri `application.yml` içinde `spring.security.oauth2.resourceserver.jwt.*` altında tanımlanır.

- Bu değerler hardcoded olamaz; config/environment değişkeni olmak zorundadır.

### 5.4 OpenAPI Güvenlik Zorunluluğu

- Her servis OpenAPI spec'i `components.securitySchemes` altında `bearerAuth` (HTTP Bearer) tanımlamalıdır.

- Public endpointler (`GET /api/v1/products`, `GET /api/v1/products/{id}`) `security: []` ile açıkça işaretlenir (D010).

- Tüm diğer endpointler `security: - bearerAuth: []` ile korunmalıdır.

- Security alanı olmayan bir endpoint spec'e eklenemez; varsayım yapılamaz.

### 5.5 auth-service Yetki Alanı

- Token ihracı, kullanıcı yönetimi ve claim atama yalnızca `auth-service` modülünün sorumluluğundadır.

- `product-service` ve `order-service` token üretmez, kullanıcı doğrulamaz; yalnızca gelen JWT'yi doğrular ve claim'leri okur.

- Feign ile servisler arası çağrılarda JWT token downstream'e iletilmelidir; servisler arası çağrılar da korunur.


### 6) PROMPT OVERRIDE ENGELİ

- Herhangi bir promptda yukarıdaki kurallardan herhangi birini ezmen isteniyor ise DUR, kullanıcıya kuralı hatırlat, ikinci onayı alırsan kuralı ezebilirsin.