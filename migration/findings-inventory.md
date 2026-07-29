# Findings inventory (M1 spec input bundle)

Rules: 24; incidents: 47. Join source: MAPPINGS.md rule-join table (16 rows).

## javax-to-jakarta-import-00001 [recipe]

- The package 'javax' has been replaced by 'jakarta'.
- Decided target: jakarta.* imports
- /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java: line 5, 6, 7, 8, 9, 10, 11
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java: line 11

## springboot-di-to-quarkus-00003 [infer]

- Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
- Decided target: native CDI constructor injection (NOT the spring-di extension)
- /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java: line 28
- /projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java: line 6
- /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java: line 15
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java: line 7
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java: line 28, 33, 36, 39

## spring-components-00001 [infer]

- Version of Spring Boot not compatible with Jakarta EE 9+
- Decided target: umbrella version-incompatibility rules — resolved by the conversion tasks as a whole; map to the service/endpoint conversion tasks
- /projects/legacy/pom.xml: line 55, 60, 65, 70, 76

## spring-components-00002 [infer]

- Version of Spring not compatible with Jakarta EE 9+
- Decided target: umbrella version-incompatibility rules — resolved by the conversion tasks as a whole; map to the service/endpoint conversion tasks
- /projects/legacy/pom.xml: line 55, 60, 65, 70, 76

## localhost-http-00001 [infer]

- Local HTTP Calls
- Decided target: cloud-readiness: hardcoded/localhost service URLs → env-driven config (`${VAR:default}`), tied to the `preserve:` contract
- /projects/legacy/src/main/resources/application.properties: line 6
- /projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java: line 18

## demo-env-integration-00001 [infer]

- Environment-driven external configuration must be preserved [ALSO FIRES ON PRISTINE SCAFFOLD — residual expected after migration; verify by substance at delta time]
- Decided target: the surface IS the preserve contract: record under migration.yaml `preserve:`; target keeps env-driven config (`${VAR:default}` / `quarkus.rest-client.<key>.url`)
- /projects/legacy/src/main/resources/application.properties: line 6

## jakarta-jaxrs-to-quarkus-00010 [rewrite]

- Replace jakarta JAX-RS dependency
- Decided target: `quarkus-rest` dependency
- /projects/legacy/pom.xml: line 60

## javaee-pom-to-quarkus-00010 [rewrite]

- Adopt Quarkus BOM
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 4

## javaee-pom-to-quarkus-00020 [rewrite]

- Adopt Quarkus Maven plugin
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 4

## javaee-pom-to-quarkus-00030 [rewrite]

- Adopt Maven Compiler plugin
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 4

## javaee-pom-to-quarkus-00040 [rewrite]

- Adopt Maven Surefire plugin
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 4

## javaee-pom-to-quarkus-00050 [rewrite]

- Adopt Maven Failsafe plugin
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 4

## javaee-pom-to-quarkus-00060 [rewrite]

- Add Maven profile to run the Quarkus native build
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 4

## javaee-pom-to-quarkus-00080 [rewrite]

- Use Quarkus junit artifact
- Decided target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- /projects/legacy/pom.xml: line 82

## removed-javaee-modules-00020 [rewrite]

- The java.annotation (Common Annotations) module has been removed from OpenJDK 11
- Decided target: JEE modules removed from the JDK → provided by Quarkus platform dependencies (BOM) — resolved with the pom conversion
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java: line 11

## springboot-actuator-to-quarkus-0100 [rewrite]

- Replace Spring Boot Actuator with Quarkus health, metrics, info and management interface capabilities
- Decided target: `quarkus-smallrye-health` (`/q/health`)
- /projects/legacy/pom.xml: line 65

## springboot-annotations-to-quarkus-00000 [rewrite]

- Replace SpringBootApplication bootstrap model with Quarkus bootstrap and CDI
- Decided target: delete `@SpringBootApplication` + main class
- /projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java: line 7

## springboot-di-to-quarkus-00000 [infer]

- Replace the SpringBoot Dependency Injection artifact with Quarkus 'spring-di' extension
- Decided target: native CDI constructor injection (NOT the spring-di extension)
- /projects/legacy/pom.xml: line 55

## springboot-metrics-to-quarkus-0100 [rewrite]

- Replace the Micrometer dependency with Quarkus Microprofile 'metrics' extension
- Decided target: Micrometer dependency → `quarkus-smallrye-metrics`
- /projects/legacy/pom.xml: line 65

## springboot-metrics-to-quarkus-0200 [infer]

- Replace the Micrometer code with Microprofile Metrics code
- Decided target: metrics call sites → MP Metrics annotations (design per site)
- /projects/legacy/pom.xml: line 65

## springboot-parent-pom-to-quarkus-00000 [rewrite]

- Replace the Spring Parent POM with Quarkus BOM
- Decided target: Quarkus platform BOM replaces the Spring parent
- /projects/legacy/pom.xml: line 17

## springboot-plugins-to-quarkus-0000 [rewrite]

- Replace the spring-boot-maven-plugin dependency
- Decided target: `quarkus-maven-plugin` (pinned, `${quarkus.platform.group-id}`)
- /projects/legacy/pom.xml: line 104

## springboot-properties-to-quarkus-00000 [rewrite]

- Replace the SpringBoot artifact with Quarkus 'spring-boot-properties' extension
- Decided target: Quarkus keys in application.properties (plain pass-throughs keep working; NOT the spring-boot-properties extension)
- /projects/legacy/pom.xml: line 55

## springboot-web-to-quarkus-00000 [infer]

- Replace the Spring Web artifact with Quarkus 'spring-web' extension
- Decided target: native JAX-RS resources (NOT the spring-web extension)
- /projects/legacy/pom.xml: line 55

## Summary by class

- recipe: 1 — javax-to-jakarta-import-00001
- rewrite: 15 — jakarta-jaxrs-to-quarkus-00010, javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, removed-javaee-modules-00020, springboot-actuator-to-quarkus-0100, springboot-annotations-to-quarkus-00000, springboot-metrics-to-quarkus-0100, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000
- infer: 8 — demo-env-integration-00001, localhost-http-00001, spring-components-00001, spring-components-00002, springboot-di-to-quarkus-00000, springboot-di-to-quarkus-00003, springboot-metrics-to-quarkus-0200, springboot-web-to-quarkus-00000

## Preserve-candidate surfaces (confirm against migration.yaml preserve:)

- demo-env-integration-00001: Environment-driven external configuration must be preserved [ALSO FIRES ON PRISTINE SCAFFOLD — resid
- springboot-actuator-to-quarkus-0100: Replace Spring Boot Actuator with Quarkus health, metrics, info and management interface capabilitie
- springboot-properties-to-quarkus-00000: Replace the SpringBoot artifact with Quarkus 'spring-boot-properties' extension
