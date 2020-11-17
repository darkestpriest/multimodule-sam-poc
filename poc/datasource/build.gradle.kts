plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":poc:model"))
    implementation(project(":poc:support"))

    implementation(platform("software.amazon.awssdk:bom:2.15.21"))
    implementation("software.amazon.awssdk:dynamodb") {
        exclude("software.amazon.awssdk", "apache-client")
        exclude("software.amazon.awssdk", "netty-nio-client")
    }
    implementation("software.amazon.awssdk:url-connection-client")

    testImplementation(kotlin("test-junit"))
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.testcontainers:localstack:1.14.3")

    testRuntimeOnly("com.amazonaws:aws-java-sdk-dynamodb:1.11.689")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}