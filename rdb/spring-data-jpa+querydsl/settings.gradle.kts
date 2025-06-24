plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include(
    "mini-carrot-market",
    "performance",
    "entity-association",
    "concurrency",
    "transaction"
)