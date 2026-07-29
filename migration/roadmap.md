# Modernization roadmap

## S01: BOM, bootstrap, and domain models
- scope: pom.xml, src/main/java/com/redhat/coolstore/CartServiceApplication.java, src/main/java/com/redhat/coolstore/rest/JerseyConfig.java, src/main/java/com/redhat/coolstore/model/Product.java, src/main/java/com/redhat/coolstore/model/Promotion.java, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java, src/main/java/com/redhat/coolstore/model/ShoppingCart.java
- findings: javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000, springboot-annotations-to-quarkus-00000, springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100, springboot-metrics-to-quarkus-0200, removed-javaee-modules-00020, springboot-di-to-quarkus-00000, springboot-web-to-quarkus-00000, spring-components-00001, spring-components-00002, demo-env-integration-00001, localhost-http-00001
- depends: -
- deploy: false
- done: Maven build succeeds with Quarkus BOM and plugins; application bootstrap removed; model classes compile unchanged
- rationale: Foundation work per dependency-order.md lines 2-6. BOM and plugins must be in place before any code changes. Application bootstrap (CartServiceApplication, JerseyConfig) removed per architecture-profile §7. HARVEST classes (models) carried forward unchanged to establish stable domain layer.

## S02: Service interfaces and external integration
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartService.java, src/main/java/com/redhat/coolstore/service/CatalogService.java
- findings: -
- depends: S01
- deploy: false
- done: Service interfaces converted to CDI; CatalogService preserves CATALOG_ENDPOINT configuration; Feign client prepared for Quarkus REST client migration
- rationale: Service interfaces and external integration points per dependency-order.md lines 7, 12. CatalogService preserves environment-driven config (demo-env-integration-00001). Interface-first approach allows implementation modernization in next story.

## S03: Core pricing services (tightly coupled group)
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java, src/main/java/com/redhat/coolstore/service/PromoService.java, src/main/java/com/redhat/coolstore/service/ShippingService.java
- findings: springboot-di-to-quarkus-00003
- depends: S02
- deploy: false
- done: All three services use CDI constructor injection; Pricing logic preserved (25% off "329299", tiered shipping 2.99/4.99/6.99/8.99/10.99, free shipping over $75); ShoppingCartServiceImpl thread-safe with ConcurrentHashMap
- rationale: Tightly coupled conversion group per architecture-profile §2. PromoService, ShippingService, and ShoppingCartServiceImpl have bidirectional dependencies through shared pricing logic (dependency-order.md lines 8-10, 15-17). Must be modernized together to maintain cart pricing behavior.

## S04: JAX-RS endpoint modernization
- scope: src/main/java/com/redhat/coolstore/rest/CartEndpoint.java
- findings: jakarta-jaxrs-to-quarkus-00010
- depends: S03
- deploy: true
- done: Endpoint uses JAX-RS (not Spring MVC) with constructor injection; GET returns 404 on missing cart; POST endpoints validate input and return 400 on invalid data; service-level and integration tests pass
- rationale: REST surface conversion per dependency-order.md line 11. Endpoint depends on ShoppingCartService which is finalized in S03. Target contract from architecture-profile §7: GET-idempotent (404, never creates), validation + error mapping enabled.
