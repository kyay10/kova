plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotest)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-opt-in=arrow.core.raise.ExperimentalRaiseAccumulateApi",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.arrow.core)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.arrow)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.framework.engine)
}

kotlin {
    jvmToolchain(17)
}
