plugins {
    id("java")
}

group = "org.jetbrains.research"

repositories {
    mavenCentral()
}

dependencies {
    implementation("junit:junit:4.13")
}

tasks.test {
    useJUnitPlatform()
}
