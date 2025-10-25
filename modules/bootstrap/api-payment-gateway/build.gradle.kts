tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.application)
    implementation(projects.modules.infrastructure.persistence)
    implementation(projects.modules.external.pgClient)
    implementation(projects.modules.common)
    implementation(libs.spring.boot.starter.jpa)
    implementation(libs.bundles.bootstrap)
    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
    testImplementation(libs.spring.mockk)
    testImplementation(libs.database.h2)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
}
