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