plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.netcomp"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation(platform("software.amazon.awssdk:bom:2.57.3"))
    implementation("software.amazon.awssdk:bedrockruntime")
    implementation("software.amazon.awssdk:s3")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.hibernate.orm:hibernate-vector")
    implementation(platform("com.google.guava:guava-bom:33.7.2-jre"))
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.poi:poi-ooxml:6.+")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
