# ARCHITECTURE.MD — Mini E-Ticaret Mikroservis Platformu

Bu dosya, projedeki mimari kararları, katman sorumlulukları, paket yapısı ve bağımlılık kurallarını tanımlar.  
Tüm servisler için bağlayıcıdır. Mimari kararlar [DECISIONS.MD](../governance/DECISIONS.MD) ile uyumlu olmalıdır.

---

## 1. Genel Bakış

### 1.1 Mimari Yaklaşım

Proje **Hexagonal Architecture (Ports & Adapters)** mimari desenini temel alır.  
Bu desen, iş mantığını (domain) altyapısal bağımlılıklardan (framework, veritabanı, HTTP) izole ederek test edilebilirliği ve değiştirilebilirliği artırır.

### 1.2 Uygulama Kapsamı

| Servis | Mimari Desen | Gerekçe |
|---|---|---|
| `product-service` | **Hexagonal Architecture** | İş mantığı içerir; domain izolasyonu gereklidir |
| `order-service` | **Hexagonal Architecture** | İş mantığı içerir; domain izolasyonu gereklidir |
| `auth-service` | Layered (mevcut yapı) | Authorization Server altyapı servisi; hexagonal dönüşüm kapsamı dışında |
| `gateway-server` | Flat / Config-only | Routing ve security config; iş mantığı barındırmaz |
| `registry-server` | Flat / Config-only | Eureka Server; iş mantığı barındırmaz |

### 1.3 Temel İlkeler

1. **Domain koruması**: Domain katmanı hiçbir framework'e bağımlı olamaz.
2. **Bağımlılık yönü**: Dış katmanlar içe doğru bağımlıdır; iç katmanlar dışı bilmez.
3. **Port/Adapter ayrımı**: Dış dünya ile etkileşim yalnızca port interface'leri ve adapter implementasyonları üzerinden gerçekleşir.
4. **Sözleşme önceliği**: OpenAPI spec implementasyonun tek kaynağıdır (bkz. AGENTS.MD §2).

---

## 2. Katman Tanımları ve Sorumlulukları

### 2.1 Domain Katmanı

İş mantığının çekirdeğidir. Framework bağımlılığı **kesinlikle yasaktır**.

| Paket | Sorumluluk | İçerik |
|---|---|---|
| `domain.model` | İş varlıkları ve değer nesneleri | Domain entity, value object, enum, domain exception |
| `domain.port.in` | Gelen port interface'leri (use case tanımları) | `*Service` interface'leri |
| `domain.port.out` | Giden port interface'leri (altyapı sözleşmeleri) | `*Repository`, `*Client` interface'leri |

**Kurallar:**
- `domain` paketinde `javax.*`, `jakarta.persistence.*`, `org.springframework.*` import'u bulunamaz.
- `jakarta.validation.*` yalnızca domain model üzerinde **kullanılmaz**; validation web katmanında (adapter.in.web) uygulanır.
- Domain exception'lar `domain.model` paketinde (veya büyürse `domain.exception` alt paketinde) tanımlanır.

### 2.2 Application Katmanı

Use case orchestration katmanıdır. Domain port'larını kullanarak iş akışlarını koordine eder.

| Paket | Sorumluluk | İçerik |
|---|---|---|
| `application.service` | Use case implementasyonları | `*ServiceImpl` sınıfları |

**Kurallar:**
- `@Service` ve `@Transactional` annotation'ları bu katmanda uygulanır.
- Application service yalnızca `domain.port.in` interface'ini implement eder.
- Outbound işlemler yalnızca `domain.port.out` interface'leri üzerinden yapılır; doğrudan JPA repository çağrısı yasaktır.

### 2.3 Adapter Katmanı — Inbound (adapter.in)

Dış dünyadan gelen istekleri domain'e yönlendirir.

| Paket | Sorumluluk | İçerik |
|---|---|---|
| `adapter.in.web` | REST controller'lar ve exception handler | `*Controller`, `GlobalExceptionHandler` |
| `adapter.in.web.dto.request` | İstek DTO'ları | `*Request` sınıfları |
| `adapter.in.web.dto.response` | Yanıt DTO'ları | `*Response` sınıfları |
| `adapter.in.web.mapper` | DTO ↔ Domain dönüşümü | `*WebMapper` sınıfları |

**Kurallar:**
- Controller metotları inbound port (`domain.port.in`) üzerinden domain'e erişir; application service'e doğrudan bağımlı olmaz.
- Request DTO'larında Jakarta Bean Validation annotation'ları kullanılır.
- Controller metotları `ResponseEntity<T>` döndürür.
- `@PreAuthorize` annotation'ları controller seviyesinde tanımlanır (DECISIONS.MD D009 uyumlu).

### 2.4 Adapter Katmanı — Outbound (adapter.out)

Domain'in dış kaynaklara erişim taleplerini karşılar.

| Paket | Sorumluluk | İçerik |
|---|---|---|
| `adapter.out.persistence` | Veritabanı erişimi | `*JpaEntity`, `*JpaRepository`, `*PersistenceAdapter`, `*PersistenceMapper` |
| `adapter.out.feign` | Servisler arası senkron iletişim | Feign client'lar, Feign DTO'ları, `*FeignAdapter` |

**Kurallar:**
- `*PersistenceAdapter` sınıfı `domain.port.out` interface'ini implement eder.
- JPA entity (`*JpaEntity`) ve domain model ayrı sınıflardır; `*PersistenceMapper` ile dönüşüm yapılır.
- JPA entity üzerinde `jakarta.persistence.*` annotation'ları kullanılır; domain model'de kullanılmaz.
- Feign client DTO'ları adapter katmanında kalır; domain katmanına sızmaz.
- JWT token relay Feign interceptor üzerinden sağlanır (AGENTS.MD §5.5 uyumlu).

### 2.5 Config Katmanı

| Paket | Sorumluluk | İçerik |
|---|---|---|
| `config` | Spring konfigürasyon sınıfları | `SecurityConfig`, `FeignClientConfig` vb. |

**Kurallar:**
- Security, Feign, Swagger gibi framework-specific konfigürasyonlar bu pakette tutulur.
- Config sınıfları domain katmanına bağımlı olmaz.

---

## 3. Bağımlılık Kuralları

### 3.1 Bağımlılık Yönü

```
┌─────────────────────────────────────────────┐
│               adapter.in.web                │  ← Dış dünya (HTTP)
│  (Controller, DTO, WebMapper, ExHandler)    │
└──────────────────────┬──────────────────────┘
                       │ kullanır
                       ▼
┌─────────────────────────────────────────────┐
│            domain.port.in                   │  ← Inbound Port (interface)
│          (Use Case tanımları)               │
└──────────────────────┬──────────────────────┘
                       │ implement eder
                       ▼
┌─────────────────────────────────────────────┐
│          application.service                │  ← Use Case Impl
│           (*ServiceImpl)                    │
└──────────────────────┬──────────────────────┘
                       │ kullanır
                       ▼
┌─────────────────────────────────────────────┐
│            domain.port.out                  │  ← Outbound Port (interface)
│     (Repository / Client interface)         │
└──────────────────────┬──────────────────────┘
                       │ implement eder
                       ▼
┌─────────────────────────────────────────────┐
│   adapter.out.persistence / adapter.out.feign│  ← Dış dünya (DB, diğer servis)
│  (JpaEntity, JpaRepo, PersistenceAdapter)   │
└─────────────────────────────────────────────┘
```

### 3.2 Yasak Bağımlılıklar

| Kaynak | Hedef | Durum |
|---|---|---|
| `domain.*` | `adapter.*` | **YASAK** |
| `domain.*` | `application.*` | **YASAK** |
| `domain.*` | `config` | **YASAK** |
| `domain.*` | `org.springframework.*` | **YASAK** |
| `domain.*` | `jakarta.persistence.*` | **YASAK** |
| `application.*` | `adapter.*` | **YASAK** |
| `adapter.in.*` | `adapter.out.*` | **YASAK** |
| `adapter.out.*` | `adapter.in.*` | **YASAK** |

### 3.3 İzin Verilen Bağımlılıklar

| Kaynak | Hedef | Durum |
|---|---|---|
| `adapter.in.web` | `domain.port.in`, `domain.model` | **İZİNLİ** |
| `adapter.out.persistence` | `domain.port.out`, `domain.model` | **İZİNLİ** |
| `adapter.out.feign` | `domain.port.out`, `domain.model` | **İZİNLİ** |
| `application.service` | `domain.port.in`, `domain.port.out`, `domain.model` | **İZİNLİ** |
| `config` | `adapter.*`, `application.*` | **İZİNLİ** |

---

## 4. Örnek Paket Yapısı

### 4.1 product-service (Hexagonal)

```
com.turkcell.productservice
├── domain
│   ├── model
│   │   ├── Product.java                      // Domain entity (framework-free POJO)
│   │   ├── ProductNotFoundException.java      // Domain exception
│   │   └── SkuAlreadyExistsException.java     // Domain exception
│   └── port
│       ├── in
│       │   └── ProductService.java            // Inbound port (use case interface)
│       └── out
│           └── ProductRepository.java         // Outbound port (repository interface)
├── application
│   └── service
│       └── ProductServiceImpl.java            // Use case implementation
├── adapter
│   ├── in
│   │   └── web
│   │       ├── ProductController.java         // REST controller
│   │       ├── GlobalExceptionHandler.java    // @RestControllerAdvice
│   │       ├── dto
│   │       │   ├── request
│   │       │   │   ├── ProductCreateRequest.java
│   │       │   │   ├── ProductUpdateRequest.java
│   │       │   │   └── StockUpdateRequest.java
│   │       │   └── response
│   │       │       ├── ProductResponse.java
│   │       │       ├── ProductPageResponse.java
│   │       │       ├── StockUpdateResponse.java
│   │       │       └── ErrorResponse.java
│   │       └── mapper
│   │           └── ProductWebMapper.java      // DTO ↔ Domain
│   └── out
│       └── persistence
│           ├── ProductJpaEntity.java          // JPA entity
│           ├── ProductJpaRepository.java      // Spring Data interface
│           ├── ProductPersistenceAdapter.java // Outbound port impl
│           └── ProductPersistenceMapper.java  // Domain ↔ JPA entity
└── config
    └── SecurityConfig.java
```

### 4.2 order-service (Hexagonal)

```
com.turkcell.orderservice
├── domain
│   ├── model
│   │   ├── Order.java                         // Domain entity
│   │   ├── OrderItem.java                     // Domain entity
│   │   ├── OrderStatus.java                   // Enum
│   │   ├── OrderNotFoundException.java        // Domain exception
│   │   ├── InsufficientStockException.java    // Domain exception
│   │   └── InvalidStatusTransitionException.java
│   └── port
│       ├── in
│       │   └── OrderService.java              // Inbound port
│       └── out
│           ├── OrderRepository.java           // Outbound port (persistence)
│           └── ProductClient.java             // Outbound port (feign)
├── application
│   └── service
│       └── OrderServiceImpl.java              // Use case implementation
├── adapter
│   ├── in
│   │   └── web
│   │       ├── OrderController.java
│   │       ├── GlobalExceptionHandler.java
│   │       ├── dto
│   │       │   ├── request
│   │       │   │   ├── OrderCreateRequest.java
│   │       │   │   └── OrderItemCreateRequest.java
│   │       │   └── response
│   │       │       ├── OrderResponse.java
│   │       │       ├── OrderItemResponse.java
│   │       │       ├── OrderPage.java
│   │       │       └── ErrorResponse.java
│   │       └── mapper
│   │           └── OrderWebMapper.java
│   └── out
│       ├── persistence
│       │   ├── OrderJpaEntity.java
│       │   ├── OrderItemJpaEntity.java
│       │   ├── OrderJpaRepository.java
│       │   ├── OrderPersistenceAdapter.java
│       │   └── OrderPersistenceMapper.java
│       └── feign
│           ├── ProductFeignClient.java        // @FeignClient
│           ├── ProductFeignAdapter.java       // Outbound port impl
│           └── dto
│               └── ProductFeignResponse.java  // Feign-specific DTO
└── config
    ├── SecurityConfig.java
    └── FeignClientConfig.java
```

### 4.3 Muaf Servisler (Flat / Layered)

```
# registry-server — flat yapı
com.turkcell.registryserver
└── RegistryServerApplication.java

# gateway-server — flat yapı
com.turkcell.gatewayserver
├── GatewayServerApplication.java
└── config
    └── SecurityConfig.java

# auth-service — layered yapı (mevcut)
com.turkcell.authservice
├── AuthServiceApplication.java
├── config/
├── entity/
├── repository/
├── service/
```

---

## 5. Domain Model vs JPA Entity Ayrımı

Hexagonal Architecture'da domain model ve JPA entity **ayrı sınıflar** olarak tanımlanır. Bu ayrım domain katmanının framework bağımsızlığını garanti eder.

### 5.1 Domain Model

```java
// domain/model/Product.java — Framework-free POJO
package com.turkcell.productservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String sku;
    private boolean active;

    // Constructor, getter, setter — framework annotation yok
    public Product() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // ... diğer getter/setter'lar
}
```

### 5.2 JPA Entity

```java
// adapter/out/persistence/ProductJpaEntity.java — JPA annotation'lı
package com.turkcell.productservice.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int stockQuantity;

    @Column(nullable = false, unique = true)
    private String sku;

    private boolean active;

    // Constructor, getter, setter
}
```

### 5.3 Persistence Mapper

```java
// adapter/out/persistence/ProductPersistenceMapper.java
package com.turkcell.productservice.adapter.out.persistence;

import com.turkcell.productservice.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceMapper {

    public Product toDomain(ProductJpaEntity entity) {
        Product product = new Product();
        product.setId(entity.getId());
        product.setName(entity.getName());
        product.setDescription(entity.getDescription());
        product.setPrice(entity.getPrice());
        product.setStockQuantity(entity.getStockQuantity());
        product.setSku(entity.getSku());
        product.setActive(entity.isActive());
        return product;
    }

    public ProductJpaEntity toJpaEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setStockQuantity(product.getStockQuantity());
        entity.setSku(product.getSku());
        entity.setActive(product.isActive());
        return entity;
    }
}
```

### 5.4 Persistence Adapter

```java
// adapter/out/persistence/ProductPersistenceAdapter.java
package com.turkcell.productservice.adapter.out.persistence;

import com.turkcell.productservice.domain.model.Product;
import com.turkcell.productservice.domain.port.out.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    public ProductPersistenceAdapter(ProductJpaRepository jpaRepository,
                                     ProductPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toJpaEntity(product);
        ProductJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    // ... diğer metotlar
}
```

---

## 6. Servisler Arası İletişim

### 6.1 Feign Client Yapısı

Servisler arası senkron iletişim `spring-cloud-starter-openfeign` ile sağlanır (DECISIONS.MD D006).  
Feign client'lar `adapter.out.feign` paketinde tanımlanır.

```java
// adapter/out/feign/ProductFeignClient.java
package com.turkcell.orderservice.adapter.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", configuration = FeignClientConfig.class)
public interface ProductFeignClient {

    @GetMapping("/api/v1/products/{id}")
    ProductFeignResponse getProductById(@PathVariable UUID id);
}
```

### 6.2 Feign Adapter

```java
// adapter/out/feign/ProductFeignAdapter.java
package com.turkcell.orderservice.adapter.out.feign;

import com.turkcell.orderservice.domain.model.Product;
import com.turkcell.orderservice.domain.port.out.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductFeignAdapter implements ProductClient {

    private final ProductFeignClient feignClient;

    public ProductFeignAdapter(ProductFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public Product getProductById(UUID id) {
        ProductFeignResponse response = feignClient.getProductById(id);
        // Feign DTO → Domain dönüşümü burada yapılır
        Product product = new Product();
        product.setId(response.getId());
        product.setName(response.getName());
        product.setPrice(response.getPrice());
        product.setStockQuantity(response.getStockQuantity());
        return product;
    }
}
```

### 6.3 JWT Token Relay

Feign çağrılarında JWT token downstream servise iletilir. Bu, AGENTS.MD §5.5 gereği zorunludur.

```java
// config/FeignClientConfig.java
package com.turkcell.orderservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor jwtRelayInterceptor() {
        return requestTemplate -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
```

---

## 7. Muaf Servisler — Yapısal Kurallar

### 7.1 registry-server

- Yalnızca `@EnableEurekaServer` annotation'ı ile çalışır.
- Ek paket veya sınıf gerektirmez.
- `localhost:8761` portunda çalışır (AGENTS.MD §4).

### 7.2 gateway-server

- Spring Cloud Gateway (reactive) tabanlıdır.
- `config/SecurityConfig.java` ile OAuth2 Resource Server konfigürasyonu barındırır.
- Route tanımları `application.yml` üzerinden yapılır; kod tarafında route tanımı yoktur.
- Hexagonal yapıya geçirilmez; iş mantığı barındırmaz.

### 7.3 auth-service

- Spring Authorization Server tabanlı altyapı servisidir.
- Token ihracı, kullanıcı yönetimi ve claim atama yalnızca bu serviste yapılır (AGENTS.MD §5.5).
- Mevcut layered yapısını (`config/`, `entity/`, `repository/`, `service/`) korur.
- Hexagonal yapıya geçirilmez; mimarisi Authorization Server framework'üne sıkı bağımlıdır.
