# S01: BOM, bootstrap, and domain models

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

What this story achieves and why it is next: its place in the roadmap,
what it unblocks, which stories it depends on (cite
dependency-order.md / architecture-profile.md).

This is the foundation story that establishes the Quarkus build platform and carries forward the domain model layer. It converts the Maven POM to use the Quarkus BOM and updates plugins (dependency-order.md lines 2-6), removes Spring Boot bootstrap artifacts (architecture-profile §7), and preserves the HARVEST model classes unchanged. No dependencies - this is the base layer that S02-S04 build upon.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot bootstrap (REMOVED per §7)
  ```java
  package com.redhat.coolstore;

  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.cloud.openfeign.EnableFeignClients;

  @SpringBootApplication
  @EnableFeignClients
  public class CartServiceApplication {

      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — Jersey configuration (REMOVED per §7)
  ```java
  package com.redhat.coolstore.rest;

  import org.glassfish.jersey.server.ResourceConfig;
  import org.springframework.stereotype.Component;

  @Component
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          register(CartEndpoint.class);
      }
  }
  ```

- `pom.xml` — Maven build configuration (QUARKUS BOM + plugins)
  ```xml
  <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>2.7.18</version>
  </parent>
  
  <dependencies>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-actuator</artifactId>
      </dependency>
      <dependency>
          <groupId>io.micrometer</groupId>
          <artifactId>micrometer-registry-prometheus</artifactId>
      </dependency>
      <dependency>
          <groupId>javax.ws.rs</groupId>
          <artifactId>javax.ws.rs-api</artifactId>
          <version>2.0</version>
      </dependency>
  </dependencies>

  <build>
      <plugins>
          <plugin>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-maven-plugin</artifactId>
          </plugin>
      </plugins>
  </build>
  ```

- HARVEST classes (PRESERVED unchanged from architecture-profile §7):
  - `src/main/java/com/redhat/coolstore/model/Product.java` — immutable data carrier
  - `src/main/java/com/redhat/coolstore/model/Promotion.java` — immutable data carrier
  - `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — immutable data carrier
  - `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — immutable data carrier

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service implementations (ShoppingCartServiceImpl, PromoService, ShippingService) - owned by S03
- REST endpoint (CartEndpoint) - owned by S04
- External integration (CatalogService) - owned by S02

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `com.redhat.coolstore.CartServiceApplication` — REDESIGN
  - (REDESIGN only) target: REMOVED - Quarkus auto-discovery subsumes Spring Boot bootstrap (architecture-profile §7)
- `com.redhat.coolstore.rest.JerseyConfig` — REDESIGN  
  - (REDESIGN only) target: REMOVED - Quarkus auto-discovers JAX-RS resources, Jersey config unnecessary (architecture-profile §7)
- `com.redhat.coolstore.model.Product` — HARVEST
- `com.redhat.coolstore.model.Promotion` — HARVEST
- `com.redhat.coolstore.model.ShoppingCartItem` — HARVEST
- `com.redhat.coolstore.model.ShoppingCart` — HARVEST

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Platform conversion (MAPPINGS umbrella rules):**
- Replace Spring Boot parent POM with Quarkus BOM: `com.redhat.quarkus.platform` version `3.27.3.SP1`
- Replace Spring Boot Maven plugin with `quarkus-maven-plugin`
- Adopt Maven Compiler plugin: Java 21 target
- Adopt Maven Surefire plugin: quarkus-junit integration
- Adopt Maven Failsafe plugin: quarkus-junit integration
- Add native build profile
- Use Quarkus junit artifact

**Jakarta namespace jump (MAPPINGS javax→jakarta):**
- Replace `javax.ws.rs` imports with `jakarta.ws.rs` (RECIPE: javax-to-jakarta-import-00001)

**Health and metrics (MAPPINGS Spring→Quarkus):**
- Replace Spring Boot Actuator with `quarkus-smallrye-health` (`/q/health`)
- Replace Micrometer dependency with `quarkus-smallrye-metrics`

**Bootstrap simplification (MAPPINGS Spring→Quarkus):**
- Remove `@SpringBootApplication` + main class (Quarkus auto-discovers)

**Story ordering:** this story handles extensions and models first per SEQUENCING.md §4.

## Contracts owned by this story

- **Findings**: All javaee-pom-to-quarkus-* rules, springboot-* rules, jakarta-jaxrs-to-quarkus-00010, javax-to-jakarta-import-00001
- **Preserve**: the CATALOG_ENDPOINT configuration surface (owned by S02, referenced here only in pom.xml)
- **Behavioral pins**: HARVEST classes preserve LEGACY values - Product/ShoppingCart/ShoppingCartItem/Promotion structures remain identical
- **Forbidden**: getMockProducts, "Fallback to mock" - not applicable to this story

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- `mvn quarkus:dev` starts successfully without CartServiceApplication
- All model classes compile with identical structure to legacy
- POM converts to Quarkus BOM and plugins without errors
- deploy story only: factory pipeline green, deployed, acceptance path
  serving (NOT APPLICABLE - deploy: false)
