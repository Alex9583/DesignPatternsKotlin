dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "DesignPatternsKotlin"

// Creational
include(":patterns:creational:singleton")
include(":patterns:creational:factory-method")
include(":patterns:creational:abstract-factory")
include(":patterns:creational:builder")
include(":patterns:creational:prototype")

// Structural
include(":patterns:structural:adapter")
include(":patterns:structural:bridge")
include(":patterns:structural:composite")
include(":patterns:structural:decorator")
include(":patterns:structural:facade")
include(":patterns:structural:flyweight")
include(":patterns:structural:proxy")

// Behavioral
include(":patterns:behavioral:strategy")
include(":patterns:behavioral:observer")
include(":patterns:behavioral:command")
include(":patterns:behavioral:state")
include(":patterns:behavioral:chain-of-responsibility")
include(":patterns:behavioral:template-method")
include(":patterns:behavioral:iterator")
include(":patterns:behavioral:mediator")
include(":patterns:behavioral:memento")
include(":patterns:behavioral:visitor")
include(":patterns:behavioral:interpreter")

// Advanced
include(":patterns:advanced:repository")
include(":patterns:advanced:specification")
include(":patterns:advanced:dependency-injection")
include(":patterns:advanced:unit-of-work")
include(":patterns:advanced:retry-backoff")
include(":patterns:advanced:circuit-breaker")
include(":patterns:advanced:event-bus")
