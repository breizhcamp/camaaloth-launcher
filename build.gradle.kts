import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.6"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"
}

group = "org.breizhcamp"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:2.0.11")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

	implementation("org.apache.commons:commons-lang3")
	implementation("com.fazecast:jSerialComm:2.7.0") //arduino communication
	implementation("com.dorkbox:SystemTray:4.1")
	implementation("net.twasi:obs-websocket-java:1.3.0-SNAPSHOT")

	implementation("org.webjars.npm:webstomp-client:1.2.6")
	implementation("org.webjars.npm:jquery:3.3.1")
	implementation("org.webjars.npm:sockjs-client:1.3.0")
	implementation("org.webjars.npm:bootstrap:4.1.3")
	implementation("org.webjars.npm:popper.js:1.14.3")
	implementation("org.webjars.npm:vue:2.5.17")
	implementation("org.webjars.npm:axios:0.18.0")
	implementation("org.webjars.npm:vue-moment:4.0.0")
	implementation("org.webjars.npm:moment-duration-format:2.2.2")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	//kapt "org.springframework.boot:spring-boot-configuration-processor"
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
