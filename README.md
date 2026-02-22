# Design Patterns en Kotlin

Exemples de **30 design patterns** implémentés en Kotlin.

Chaque pattern est un module Gradle indépendant avec :
- une implémentation complète dans un domaine métier
- des tests unitaires (minimum 4 par module)
- un README détaillé avec diagramme Mermaid

## Stack technique

| Outil | Version |
|-------|---------|
| Kotlin | 2.3.0 (JVM) |
| Java toolchain | 21 |
| Build | Gradle 9.0 (KTS, multi-modules) |
| Tests | JUnit 5 + kotlin.test |
| Lint | ktlint 1.5.0 via plugin Gradle |

## Commandes

```bash
# Build complet
./gradlew build

# Tous les tests
./gradlew test

# Tests d'un seul module
./gradlew :patterns:<category>:<pattern>:test

# Vérification du style
./gradlew ktlintCheck

# Auto-correction du style
./gradlew ktlintFormat
```

## Patterns

### Creational (5)

| Pattern | Module | Domaine | Concepts Kotlin |
|---------|--------|---------|-----------------|
| [Singleton](patterns/creational/singleton/) | `:patterns:creational:singleton` | E-commerce (config) | `object`, `enum class` |
| [Factory Method](patterns/creational/factory-method/) | `:patterns:creational:factory-method` | Paiement | `sealed interface`, abstract creator |
| [Abstract Factory](patterns/creational/abstract-factory/) | `:patterns:creational:abstract-factory` | Notifications | Familles d'interfaces |
| [Builder](patterns/creational/builder/) | `:patterns:creational:builder` | Logistique | DSL builder, `apply`, `require` |
| [Prototype](patterns/creational/prototype/) | `:patterns:creational:prototype` | Pricing | `data class`, `copy()` |

### Structural (7)

| Pattern | Module | Domaine | Concepts Kotlin |
|---------|--------|---------|-----------------|
| [Adapter](patterns/structural/adapter/) | `:patterns:structural:adapter` | Paiement | Délégation `by` |
| [Bridge](patterns/structural/bridge/) | `:patterns:structural:bridge` | Notifications | Composition, 2 dimensions |
| [Composite](patterns/structural/composite/) | `:patterns:structural:composite` | E-commerce (catalogue) | `sealed interface`, récursion |
| [Decorator](patterns/structural/decorator/) | `:patterns:structural:decorator` | Pricing | Délégation `by`, `fun interface` |
| [Facade](patterns/structural/facade/) | `:patterns:structural:facade` | E-commerce (commande) | Composition simple |
| [Flyweight](patterns/structural/flyweight/) | `:patterns:structural:flyweight` | Logistique | Factory + cache `Map` |
| [Proxy](patterns/structural/proxy/) | `:patterns:structural:proxy` | Authentification | Contrôle d'accès, `enum class` |

### Behavioral (11)

| Pattern | Module | Domaine | Concepts Kotlin |
|---------|--------|---------|-----------------|
| [Strategy](patterns/behavioral/strategy/) | `:patterns:behavioral:strategy` | Pricing | `fun interface`, lambdas |
| [Observer](patterns/behavioral/observer/) | `:patterns:behavioral:observer` | Logistique | `fun interface`, `mutableListOf` |
| [Command](patterns/behavioral/command/) | `:patterns:behavioral:command` | E-commerce | Interface + undo stack |
| [State](patterns/behavioral/state/) | `:patterns:behavioral:state` | Paiement | `sealed class` states |
| [Chain of Responsibility](patterns/behavioral/chain-of-responsibility/) | `:patterns:behavioral:chain-of-responsibility` | Authentification | Chaîne de handlers abstraite |
| [Template Method](patterns/behavioral/template-method/) | `:patterns:behavioral:template-method` | Notifications | `abstract class`, template final |
| [Iterator](patterns/behavioral/iterator/) | `:patterns:behavioral:iterator` | Logistique | `Iterator<T>`, `Iterable<T>` |
| [Mediator](patterns/behavioral/mediator/) | `:patterns:behavioral:mediator` | E-commerce (checkout) | Interface mediator, découplage |
| [Memento](patterns/behavioral/memento/) | `:patterns:behavioral:memento` | Pricing | `data class` snapshot |
| [Visitor](patterns/behavioral/visitor/) | `:patterns:behavioral:visitor` | E-commerce (panier) | `sealed class` + `when` |
| [Interpreter](patterns/behavioral/interpreter/) | `:patterns:behavioral:interpreter` | Pricing | `sealed interface` AST |

### Advanced (7)

| Pattern | Module | Domaine | Concepts Kotlin |
|---------|--------|---------|-----------------|
| [Repository](patterns/advanced/repository/) | `:patterns:advanced:repository` | E-commerce | Interface générique, `MutableMap` |
| [Specification](patterns/advanced/specification/) | `:patterns:advanced:specification` | Logistique | `fun interface`, opérateurs `and`/`or`/`not` |
| [Dependency Injection](patterns/advanced/dependency-injection/) | `:patterns:advanced:dependency-injection` | Authentification | Injection constructeur, sans framework |
| [Unit of Work](patterns/advanced/unit-of-work/) | `:patterns:advanced:unit-of-work` | Paiement (ledger) | `sealed interface`, transaction tracking |
| [Retry with Backoff](patterns/advanced/retry-backoff/) | `:patterns:advanced:retry-backoff` | Paiement | Higher-order functions, `Result<T>` |
| [Circuit Breaker](patterns/advanced/circuit-breaker/) | `:patterns:advanced:circuit-breaker` | Notifications | `enum class` state machine |
| [Event Bus](patterns/advanced/event-bus/) | `:patterns:advanced:event-bus` | Logistique | `inline reified`, `KClass`, `fun interface` |

## Architecture

```
patterns/
├── creational/          # 5 patterns
│   ├── singleton/
│   ├── factory-method/
│   ├── abstract-factory/
│   ├── builder/
│   └── prototype/
├── structural/          # 7 patterns
│   ├── adapter/
│   ├── bridge/
│   ├── composite/
│   ├── decorator/
│   ├── facade/
│   ├── flyweight/
│   └── proxy/
├── behavioral/          # 11 patterns
│   ├── chain-of-responsibility/
│   ├── command/
│   ├── interpreter/
│   ├── iterator/
│   ├── mediator/
│   ├── memento/
│   ├── observer/
│   ├── state/
│   ├── strategy/
│   ├── template-method/
│   └── visitor/
└── advanced/            # 7 patterns
    ├── circuit-breaker/
    ├── dependency-injection/
    ├── event-bus/
    ├── repository/
    ├── retry-backoff/
    ├── specification/
    └── unit-of-work/
```

Chaque module suit la structure :

```
<pattern>/
├── README.md
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/example/patterns/<category>/<pattern>/
    └── test/kotlin/com/example/patterns/<category>/<pattern>/
```

## Principes de conception

- **Aucune dépendance inter-modules** : chaque pattern est autonome
- **`BigDecimal` pour les montants** : pas de `Double` pour les prix
- **Pas de framework DI** : injection par constructeur uniquement
- **Tests sans mocking library** : fakes et stubs simples
- **Domaines métier** : paiement, logistique, e-commerce, auth, pricing, notifications
