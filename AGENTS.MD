### AGENTS.MD - Mini E-Ticaret Mikroservis Platformu

Bu repo mini e-ticaret mikroservis ekosistemini barındırır. Bu dosya bu repoda çalışacak AI ve insanlar için bir anayasa hükmündedir.

Teknoloji Yığını: Java 21, Spring Boot 3.4.X, Maven

---

### 1. Nasıl Çalışılır? (Zorunlu İş Akışı)

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

- Bir dosya dökümü hazırla (hangi dosyalar üretilecek/değişecek + neden)

- Dosya dökümüne tüm yeni bağımlılıkları (groupId, artifactId, scope) listele ve her biri için ayrı onay bekle; onaysız pom.xml'e bağımlılık ekleme.

- Herhangi bir detay eksikse sorularını sor.

- Dosya oluşturma işlemini birbiriyle alakalı maksimum 5'er dosyalık gruplar halinde batchlere böl. Her batch arasında benden ek onay bekle.

## 2) CONTRACT FIRST

### 2.1 OpenAPI doğruluğun tek kaynağıdır (Source of Truth)

- API sözleşmeleri şu dizinde bulunur: `/docs/openapi`

- Her servis **kendi OpenAPI dosyasına sahip olmalıdır**:
  - `docs/openapi/product-service.yml`
  - `docs/openapi/order-service.yml`

- Swagger UI, bu sözleşmelerden üretilir.

- Implementasyon **her zaman sözleşmeye uymalıdır**; sözleşme implementasyona göre değiştirilemez.

### 2.2 Kod Üretim Politikası (Code Generation Policy)

- OpenAPI araçları **yalnızca repoda zaten mevcutsa** kullanılabilir.

- Açık onay olmadan **yeni bir OpenAPI generator bağımlılığı eklenmemelidir**.

- Eğer generator yoksa, controller ve DTO’lar **spec’e uygun olacak şekilde manuel olarak implement edilmelidir**.
