# CODING_STANDARDS.MD — Mini E-Ticaret Mikroservis Platformu

Bu dosya, projedeki tüm servisler için geçerli Java kodlama standartlarını tanımlar.  
Her kural doğru ve yanlış kullanım örnekleriyle desteklenmiştir. Mimari kurallar için [ARCHITECTURE.MD](./ARCHITECTURE.MD) dosyasına bakınız.

---

## 1. Genel İsimlendirme Kuralları

### 1.1 Sınıflar — PascalCase

```java
// DOGRU
public class ProductService { }
public class OrderController { }
public class ProductNotFoundException extends RuntimeException { }

// YANLIS
public class productService { }
public class order_controller { }
public class product_not_found_exception extends RuntimeException { }
```

### 1.2 Metotlar ve Değişkenler — camelCase

```java
// DOGRU
public Product findById(UUID id) { }
private String orderStatus;
private int stockQuantity;

// YANLIS
public Product FindById(UUID id) { }
private String OrderStatus;
private int stock_quantity;
```

### 1.3 Sabitler — UPPER_SNAKE_CASE

```java
// DOGRU
public static final int MAX_PAGE_SIZE = 50;
public static final String DEFAULT_SORT_FIELD = "name";

// YANLIS
public static final int maxPageSize = 50;
public static final String defaultSortField = "name";
```

### 1.4 Paketler — lowercase, tek kelime tercih

```java
// DOGRU
package com.turkcell.productservice.domain.model;
package com.turkcell.orderservice.adapter.in.web;

// YANLIS
package com.turkcell.productService.Domain.Model;
package com.turkcell.order_service.adapter_in.Web;
```

### 1.5 Boolean Değişkenler — is/has/can Prefix

```java
// DOGRU
private boolean active;         // field için prefix opsiyonel
public boolean isActive() { }  // getter'da "is" prefix zorunlu

private boolean hasStock;
public boolean hasStock() { }

// YANLIS
public boolean getActive() { }  // boolean getter'da "get" kullanılmaz
private boolean stockAvailable;  // anlam belirsiz — "has" olmalı
```

---

## 2. Sınıf İsimlendirme Konvansiyonları

| Tür | Suffix / Pattern | Paket | Örnek |
|---|---|---|---|
| REST Controller | `*Controller` | `adapter.in.web` | `ProductController` |
| Inbound Port (use case interface) | `*Service` | `domain.port.in` | `ProductService` |
| Use Case Implementation | `*ServiceImpl` | `application.service` | `ProductServiceImpl` |
| Outbound Port (repository) | `*Repository` | `domain.port.out` | `ProductRepository` |
| Outbound Port (external service) | `*Client` | `domain.port.out` | `ProductClient` |
| JPA Repository (Spring Data) | `*JpaRepository` | `adapter.out.persistence` | `ProductJpaRepository` |
| Persistence Adapter | `*PersistenceAdapter` | `adapter.out.persistence` | `ProductPersistenceAdapter` |
| Persistence Mapper | `*PersistenceMapper` | `adapter.out.persistence` | `ProductPersistenceMapper` |
| Feign Client | `*FeignClient` | `adapter.out.feign` | `ProductFeignClient` |
| Feign Adapter | `*FeignAdapter` | `adapter.out.feign` | `ProductFeignAdapter` |
| JPA Entity | `*JpaEntity` | `adapter.out.persistence` | `ProductJpaEntity` |
| Domain Model | Suffix yok | `domain.model` | `Product`, `Order` |
| Request DTO | `*Request` | `adapter.in.web.dto.request` | `ProductCreateRequest` |
| Response DTO | `*Response` | `adapter.in.web.dto.response` | `ProductResponse` |
| Page Response DTO | `*Page` / `*PageResponse` | `adapter.in.web.dto.response` | `ProductPageResponse` |
| Web Mapper | `*WebMapper` | `adapter.in.web.mapper` | `ProductWebMapper` |
| Error Response | `ErrorResponse` | `adapter.in.web.dto.response` | `ErrorResponse` |
| Exception | `*Exception` | `domain.model` | `ProductNotFoundException` |
| Config | `*Config` | `config` | `SecurityConfig` |
| Enum | Suffix yok, PascalCase | `domain.model` | `OrderStatus` |

### 2.1 Doğru / Yanlış Örnekler

```java
// DOGRU — net suffix, doğru paket
// domain/port/in/ProductService.java
public interface ProductService { }

// application/service/ProductServiceImpl.java
@Service
public class ProductServiceImpl implements ProductService { }

// adapter/out/persistence/ProductJpaEntity.java
@Entity
public class ProductJpaEntity { }

// adapter/out/persistence/ProductJpaRepository.java
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> { }

// YANLIS — karışık suffix, yanlış paket
// service/ProductServiceImpl.java  → paket yanlış
public class ProductServiceImplementation implements ProductService { }  // Suffix "Impl" olmalı

// entity/Product.java  → JPA entity ama "JpaEntity" suffix'i yok
@Entity
public class Product { }  // "ProductJpaEntity" olmalı

// repository/ProductRepository.java  → Spring Data + port ayrımı yok
public interface ProductRepository extends JpaRepository<Product, UUID> { }
// Port: domain/port/out/ProductRepository.java (interface, Spring Data extend etmez)
// Spring Data: adapter/out/persistence/ProductJpaRepository.java
```

---

## 3. DTO Kuralları

### 3.1 Paket Yapısı

- Request DTO: `adapter.in.web.dto.request`
- Response DTO: `adapter.in.web.dto.response`
- Feign DTO: `adapter.out.feign.dto`

### 3.2 Validation

Jakarta Bean Validation annotation'ları **yalnızca request DTO'larında** kullanılır.

```java
// DOGRU — validation request DTO'da
package com.turkcell.productservice.adapter.in.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    // getter, setter
}

// YANLIS — validation domain model'de
package com.turkcell.productservice.domain.model;

import jakarta.validation.constraints.NotBlank;  // Domain'de framework import YASAK

public class Product {
    @NotBlank
    private String name;  // Domain model'de validation annotation kullanılmaz
}
```

### 3.3 DTO Tanımlama Stili

- DTO'lar `class` olarak tanımlanır (`record` kullanılmaz).
- Lombok kullanılmaz; getter/setter manuel yazılır.
- DTO'larda iş mantığı bulunmaz.

```java
// DOGRU
public class ProductResponse {

    private UUID id;
    private String name;
    private BigDecimal price;

    public ProductResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // ... diğer getter/setter
}

// YANLIS — record kullanımı
public record ProductResponse(UUID id, String name, BigDecimal price) { }

// YANLIS — Lombok kullanımı
@Data
public class ProductResponse {
    private UUID id;
    private String name;
}
```

---

## 4. Exception Handling Standardı

### 4.1 Genel Yapı

- Her servis **tek bir** `GlobalExceptionHandler` kullanır.
- Handler `adapter.in.web` paketinde yer alır.
- `@RestControllerAdvice` annotation'ı ile tanımlanır.
- Tüm hata yanıtları `ErrorResponse` DTO üzerinden döndürülür.

### 4.2 ErrorResponse Yapısı

```java
// adapter/in/web/dto/response/ErrorResponse.java
package com.turkcell.productservice.adapter.in.web.dto.response;

public class ErrorResponse {

    private String code;
    private String message;
    private Object details;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    // getter, setter
}
```

### 4.3 Exception Handler

HTTP status kodu `ResponseEntity` ile açıkça döndürülür. `@ResponseStatus` annotation'ı **exception sınıflarında veya handler metotlarında kullanılmaz**.

```java
// DOGRU — ResponseEntity ile açık status
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "PRODUCT_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));

        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed",
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

// YANLIS — @ResponseStatus kullanımı
@ResponseStatus(HttpStatus.NOT_FOUND)   // KULLANILMAZ
@ExceptionHandler(ProductNotFoundException.class)
public ErrorResponse handleProductNotFound(ProductNotFoundException ex) {
    return new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage(), null);
}
```

### 4.4 Domain Exception Tanımı

Domain exception'lar `domain.model` paketinde tanımlanır. Spring veya Jakarta bağımlılığı içermez.

```java
// DOGRU — framework-free domain exception
package com.turkcell.productservice.domain.model;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}

// YANLIS — Spring annotation'ı ile domain exception
package com.turkcell.productservice.domain.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)  // Domain'de framework annotation YASAK
public class ProductNotFoundException extends RuntimeException { }
```

---

## 5. Dependency Injection

### 5.1 Constructor Injection Zorunludur

- `@Autowired` annotation'ı kullanılmaz.
- Lombok `@RequiredArgsConstructor` kullanılmaz.
- Bağımlılıklar `final` field olarak tanımlanır.

```java
// DOGRU — constructor injection, final field
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}

// YANLIS — @Autowired field injection
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;  // field injection YASAK
}

// YANLIS — Lombok
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;  // Lombok YASAK
}
```

### 5.2 Tek Constructor Kuralı

Spring, tek constructor varsa `@Autowired` olmadan injection yapar. Bu davranış tercih edilir.

```java
// DOGRU — tek constructor, @Autowired gereksiz
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
}

// YANLIS — tek constructor'da gereksiz @Autowired
public class ProductController {

    private final ProductService productService;

    @Autowired  // Gereksiz — tek constructor'da Spring otomatik inject eder
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
}
```

---

## 6. Service Katmanı

### 6.1 Inbound Port (Use Case Interface)

- `domain.port.in` paketinde tanımlanır.
- `*Service` suffix kullanır.
- Framework annotation içermez.

```java
// DOGRU
package com.turkcell.productservice.domain.port.in;

import com.turkcell.productservice.domain.model.Product;
import java.util.UUID;

public interface ProductService {

    Product createProduct(Product product);
    Product findById(UUID id);
    Product updateProduct(UUID id, Product product);
    void deleteProduct(UUID id);
}
```

### 6.2 Use Case Implementation

- `application.service` paketinde tanımlanır.
- `*ServiceImpl` suffix kullanır.
- `@Service` annotation'ı bu sınıfa eklenir.
- `@Transactional` bu seviyede uygulanır.
- Outbound erişim yalnızca `domain.port.out` interface'leri üzerinden yapılır.

```java
// DOGRU
package com.turkcell.productservice.application.service;

import com.turkcell.productservice.domain.model.Product;
import com.turkcell.productservice.domain.model.ProductNotFoundException;
import com.turkcell.productservice.domain.port.in.ProductService;
import com.turkcell.productservice.domain.port.out.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
}

// YANLIS — doğrudan JPA repository kullanımı
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductJpaRepository jpaRepository;  // YASAK — port.out üzerinden erişilmeli

    @Override
    public Product findById(UUID id) {
        return jpaRepository.findById(id)  // Adapter bypass edilmiş
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }
}
```

### 6.3 Outbound Port (Repository Interface)

- `domain.port.out` paketinde tanımlanır.
- Spring Data `JpaRepository` extend **etmez**.
- Framework annotation içermez.

```java
// DOGRU — saf Java interface
package com.turkcell.productservice.domain.port.out;

import com.turkcell.productservice.domain.model.Product;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);
    Optional<Product> findById(UUID id);
    void deleteById(UUID id);
    boolean existsBySku(String sku);
}

// YANLIS — Spring Data extend eden port
package com.turkcell.productservice.domain.port.out;

import org.springframework.data.jpa.repository.JpaRepository;  // Domain'de Spring YASAK

public interface ProductRepository extends JpaRepository<Product, UUID> { }
```

---

## 7. Controller Katmanı

### 7.1 Annotation Yapısı

```java
// DOGRU
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;
    private final ProductWebMapper mapper;

    public ProductController(ProductService productService, ProductWebMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Product.Create')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        Product product = mapper.toDomain(request);
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }
}
```

### 7.2 Kurallar

- `@RestController` ve `@RequestMapping` sınıf seviyesinde.
- `@Validated` sınıf seviyesinde.
- Metot dönüş tipi: `ResponseEntity<T>`.
- `@PreAuthorize` claim'leri DECISIONS.MD D009/D012 tablosundan alınır.
- Public endpoint'ler `@PreAuthorize` olmadan tanımlanır (DECISIONS.MD D010).
- Swagger: `@Operation` ve `@ApiResponse` annotation'ları ile dokümante edilir.

```java
// DOGRU — Swagger annotation
@Operation(summary = "Ürün detayı getir")
@ApiResponse(responseCode = "200", description = "Başarılı")
@ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) { }

// YANLIS — Swagger annotation eksik
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) { }
```

---

## 8. Entity ve Domain Model

### 8.1 Domain Model

- Framework-free POJO; `domain.model` paketinde.
- JPA, Spring, Jakarta persistence annotation'ı **kullanılmaz**.
- Manuel getter/setter (Lombok yok).
- Primary key tipi: `UUID`.

```java
// DOGRU
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

    public Product() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    // ... diğer getter/setter
}
```

### 8.2 JPA Entity

- `adapter.out.persistence` paketinde; `*JpaEntity` suffix.
- JPA annotation'ları (`@Entity`, `@Table`, `@Id`, `@Column`) bu sınıfta kullanılır.
- Primary key: `UUID` + `@GeneratedValue(strategy = GenerationType.UUID)`.

```java
// DOGRU
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

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, unique = true)
    private String sku;

    // getter, setter
}
```

---

## 9. Mapper

### 9.1 Genel Kurallar

- Mapper'lar `@Component` annotation'ı ile Spring bean olarak tanımlanır.
- MapStruct veya ModelMapper **kullanılmaz**; hand-written mapper tercih edilir.
- Instance metot kullanılır (static metot tercih edilmez).

### 9.2 İki Tür Mapper

| Tür | Suffix | Paket | Dönüşüm |
|---|---|---|---|
| Web Mapper | `*WebMapper` | `adapter.in.web.mapper` | Request DTO → Domain, Domain → Response DTO |
| Persistence Mapper | `*PersistenceMapper` | `adapter.out.persistence` | Domain → JPA Entity, JPA Entity → Domain |

### 9.3 Web Mapper Örneği

```java
// DOGRU
package com.turkcell.productservice.adapter.in.web.mapper;

import com.turkcell.productservice.adapter.in.web.dto.request.ProductCreateRequest;
import com.turkcell.productservice.adapter.in.web.dto.response.ProductResponse;
import com.turkcell.productservice.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductWebMapper {

    public Product toDomain(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSku(product.getSku());
        response.setActive(product.isActive());
        return response;
    }
}
```

### 9.4 Yanlış Kullanımlar

```java
// YANLIS — static metot
public class ProductWebMapper {

    public static ProductResponse toResponse(Product product) {  // static YASAK
        // ...
    }
}

// YANLIS — MapStruct
@Mapper(componentModel = "spring")
public interface ProductWebMapper {  // MapStruct KULLANILMAZ
    ProductResponse toResponse(Product product);
}

// YANLIS — controller içinde inline dönüşüm
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
    Product product = productService.findById(id);
    ProductResponse response = new ProductResponse();  // Mapper kullanılmalı
    response.setId(product.getId());
    response.setName(product.getName());
    return ResponseEntity.ok(response);
}
```

---

## 10. Kod Formatı

### 10.1 Satır Uzunluğu

- Maksimum **120 karakter**.
- Uzun metot imzaları parametre başına satıra bölünür.

```java
// DOGRU — parametre başına satır
public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody ProductCreateRequest request) {
    // ...
}

// YANLIS — 120 karakteri aşan tek satır
public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request, @RequestHeader String authorization) {
    // ...
}
```

### 10.2 Import Kuralları

- **Wildcard import yasaktır** (`import java.util.*` kullanılmaz).
- Import sırası:

```
1. java.*
2. jakarta.*
3. org.*
4. com.*
```

```java
// DOGRU
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import org.springframework.stereotype.Service;

import com.turkcell.productservice.domain.model.Product;

// YANLIS — wildcard import
import java.util.*;       // YASAK
import jakarta.persistence.*;  // YASAK
```

### 10.3 Boş Satır Kuralları

- Sınıf seviyesi üyeler (field, constructor, metot) arasında **tek boş satır**.
- Metot içinde mantıksal bloklar arasında tek boş satır.
- Dosya sonunda tek boş satır.

### 10.4 Parantez Stili — K&R

Açılış parantezi aynı satırda.

```java
// DOGRU — K&R stili
public class ProductController {

    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return ResponseEntity.ok(mapper.toResponse(productService.findById(id)));
    }
}

// YANLIS — Allman stili
public class ProductController
{
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
}
```

---

## 11. Test İsimlendirme

### 11.1 Test Sınıfı

- Suffix: `*Test` (unit test), `*IntegrationTest` (integration test).
- Test paketi: ana kaynak ile aynı paket yapısını takip eder.

```java
// DOGRU
// test/java/com/turkcell/productservice/application/service/ProductServiceImplTest.java
package com.turkcell.productservice.application.service;

class ProductServiceImplTest { }

// test/java/com/turkcell/productservice/adapter/in/web/ProductControllerTest.java
package com.turkcell.productservice.adapter.in.web;

class ProductControllerTest { }
```

### 11.2 Test Metodu İsimlendirme

Pattern: `should_DoSomething_When_Condition`

```java
// DOGRU
@Test
void should_ReturnProduct_When_ValidIdProvided() { }

@Test
void should_ThrowProductNotFoundException_When_IdNotExists() { }

@Test
void should_CreateProduct_When_RequestIsValid() { }

@Test
void should_ReturnBadRequest_When_NameIsBlank() { }

// YANLIS
@Test
void testGetProduct() { }     // "test" prefix gereksiz; koşul belirsiz

@Test
void getProductById() { }     // "should" prefix eksik; test olduğu anlaşılmaz

@Test
void itShouldWork() { }       // ne yaptığı belirsiz
```

### 11.3 Test Yapısı — Given/When/Then

```java
// DOGRU — Given/When/Then blokları açık
@Test
void should_ReturnProduct_When_ValidIdProvided() {
    // given
    UUID id = UUID.randomUUID();
    Product expected = new Product();
    expected.setId(id);
    expected.setName("Test Product");

    when(productRepository.findById(id)).thenReturn(Optional.of(expected));

    // when
    Product result = productService.findById(id);

    // then
    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals("Test Product", result.getName());
    verify(productRepository).findById(id);
}
```
