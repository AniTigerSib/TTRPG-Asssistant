plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "12.4.0"
	id("io.freefair.lombok") version "9.4.0"
}

group = "ttrpg"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
	java.srcDir("src/integrationTest/java")
	resources.srcDir("src/integrationTest/resources")

	compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
	runtimeClasspath += output + compileClasspath
}

configurations[integrationTestSourceSet.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTestSourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.springframework.security:spring-security-crypto")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.13"
}

tasks.named<Test>("test") {
	finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
	dependsOn(tasks.named<Test>("test"))

	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}
}

tasks.register<Test>("integrationTest") {
	description = "Runs integration tests against a running application instance."
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	testClassesDirs = integrationTestSourceSet.output.classesDirs
	classpath = integrationTestSourceSet.runtimeClasspath
	useJUnitPlatform()
	shouldRunAfter(tasks.test)

	systemProperty(
		"integration.base-url",
		providers.gradleProperty("integrationBaseUrl")
			.orElse(providers.environmentVariable("INTEGRATION_BASE_URL"))
			.orElse("http://localhost:8080")
			.get()
	)
}

flyway {
	url = "jdbc:postgresql://${System.getenv("DB_HOST")}:${System.getenv("DB_PORT")}/${System.getenv("DB_NAME")}?currentSchema=${System.getenv("DB_SCHEMA")}"
	user = System.getenv("DB_FLYWAY_USER")
	password = System.getenv("DB_FLYWAY_PASSWORD")
	defaultSchema = System.getenv("DB_SCHEMA")
	locations = arrayOf("classpath:db/migration")
	baselineOnMigrate = true
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	archiveFileName.set("app.jar")
}

tasks.named<Jar>("jar") {
	enabled = false
}
