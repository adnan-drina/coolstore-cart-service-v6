# S01 Plan: BOM, Bootstrap, and Domain Models

## Platform Conversion Strategy

This story implements the foundational Quarkus platform conversion while preserving HARVEST domain models unchanged. The strategy follows dependency-order.md conversion ordering with POM and extensions first, then domain models.

## Class-Level Mapping by Dependency Order

### Build System Modernization

**Class: pom.xml**
- **Type**: rewrite
- **Target**: Quarkus platform BOM with all required plugins
- **Dependencies**: None (foundational)
- **Scope**: Complete Maven transformation including:
  - Replace Spring Boot parent with Quarkus BOM (`com.redhat.quarkus.platform:3.27.3.SP1`)
  - Add quarkus-maven-plugin with proper group ID
  - Adopt Maven Compiler plugin (Java 21 target)
  - Adopt Maven Surefire/Failsafe plugins with Quarkus integration
  - Add native build profile
  - Replace Spring Boot dependencies with Quarkus equivalents

### Bootstrap Simplification

**Class: CartServiceApplication.java**
- **Type**: rewrite  
- **Target**: REMOVED - Quarkus auto-discovery subsumes Spring Boot bootstrap
- **Dependencies**: None
- **Evidence**: Architecture-profile §7: "Quarkus auto-discovery subsumes Spring Boot bootstrap"
- **Action**: Delete entire class file - no replacement needed

**Class: JerseyConfig.java**
- **Type**: rewrite
- **Target**: REMOVED - Quarkus auto-discovers JAX-RS resources  
- **Dependencies**: None
- **Evidence**: Architecture-profile §7: "Quarkus auto-discovers JAX-RS resources, Jersey config unnecessary"
- **Action**: Delete entire class file - no replacement needed

### Domain Model Preservation (HARVEST Classes)

**Class: Product.java**
- **Type**: rewrite
- **Target**: PRESERVE unchanged - immutable data carrier
- **Dependencies**: None
- **Evidence**: Architecture-profile §7: "HARVEST classes preserve LEGACY values - Product/ShoppingCart/ShoppingCartItem/Promotion structures remain identical"
- **Action**: Copy unchanged from `/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`

**Class: Promotion.java**
- **Type**: rewrite
- **Target**: PRESERVE unchanged - immutable data carrier  
- **Dependencies**: None
- **Evidence**: Architecture-profile §7: "HARVEST classes preserve LEGACY values"
- **Action**: Copy unchanged from `/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java`

**Class: ShoppingCartItem.java**
- **Type**: rewrite
- **Target**: PRESERVE unchanged - immutable data carrier
- **Dependencies**: Product (conversion order: 1→4)
- **Evidence**: Architecture-profile §7: "HARVEST classes preserve LEGACY values"
- **Action**: Copy unchanged from `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`

**Class: ShoppingCart.java**
- **Type**: rewrite
- **Target**: PRESERVE unchanged - immutable data carrier
- **Dependencies**: ShoppingCartItem (conversion order: 4→6)
- **Evidence**: Architecture-profile §7: "HARVEST classes preserve LEGACY values"
- **Action**: Copy unchanged from `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java`

## Dependency Management

### Quarkus Extensions Required
- `quarkus-rest` (replaces JAX-RS API dependency)
- `quarkus-smallrye-health` (replaces Spring Boot Actuator)
- `quarkus-smallrye-metrics` (replaces Micrometer)
- `quarkus-micrometer-registry-prometheus` (if metrics needed)

### Jakarta Namespace Updates
- javax→jakarta imports already handled by recipe (migration/recipe-log.md)

### Configuration Surface Preservation  
- `CATALOG_ENDPOINT` environment variable support preserved
- Plain application.properties keys remain functional

## Decided Target Shapes

### POM Structure
```xml
<!-- Replace Spring Boot parent with Quarkus BOM -->
<parent>
    <groupId>com.redhat.quarkus.platform</groupId>
    <artifactId>quarkus-bom</artifactId>
    <version>3.27.3.SP1</version>
</parent>

<!-- Quarkus Maven plugin -->
<plugin>
    <groupId>${quarkus.platform.group-id}</groupId>
    <artifactId>quarkus-maven-plugin</artifactId>
</plugin>

<!-- Quarkus junit (test scope) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
```

### Domain Model Structure
All HARVEST classes maintain identical field structure, constructors, and business methods to legacy implementation.

### Bootstrap Removal
No replacement classes - Quarkus auto-discovery handles all bootstrap concerns.

## Open Design Decisions

### CDI Constructor Injection (springboot-di-to-quarkus-00003)
- **Decision**: Native CDI constructor injection (NOT spring-di extension)
- **Scope**: Future stories (S03-S04) where services are redesigned
- **This story**: No services yet - HARVEST models only

### Health Endpoint Strategy
- **Decision**: Quarkus SmallRye Health (`/q/health`) replaces Spring Boot Actuator
- **Implementation**: Simple health check that verifies application starts successfully

### Metrics Migration  
- **Decision**: MicroProfile Metrics via `quarkus-smallrye-metrics`
- **Implementation**: Metrics conversion scoped to future stories (S03-S04)

## Test Strategy

### Characterization Tests Required
Following PLANNING.md §6: "Characterization tests come EARLY, not as a tail"

**Before HARVEST model conversion:**
- Port legacy test suite to verify model contracts
- Tests must assert LEGACY values for HARVEST classes
- Use test doubles for services not yet converted (S02-S04 owned)

**God Node Tests** (dependency-order.md:8-14):
- ShoppingCart (fan-in: 5) - characterization tests first
- Product (fan-in: 4) - characterization tests first  
- ShoppingCartItem (fan-in: 3) - characterization tests first

### Coverage Requirements
- ≥80% line coverage for all migrated classes
- Model classes and POM conversion fully tested
- Bootstrap removal verification (application starts without Spring Boot)

## Build Verification

Every commit must pass:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test
```

Final story completion:
```bash
mvn -q clean verify
mvn quarkus:dev  # starts successfully without CartServiceApplication
```

## Integration Boundaries

**Preserved for S02:**
- `CATALOG_ENDPOINT` configuration surface
- CatalogService interface (not implemented until S02)

**Preserved for S03:**  
- ShoppingCartService implementation
- PromoService, ShippingService business logic
- Service-level CDI conversion

**Preserved for S04:**
- CartEndpoint JAX-RS resource
- JerseyConfig replacement with native JAX-RS