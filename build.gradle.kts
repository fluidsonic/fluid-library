import com.github.benmanes.gradle.versions.updates.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "1.1.23"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "1.5.10"
	`kotlin-dsl`
	`maven-publish`
	signing
	id("com.github.ben-manes.versions") version "0.39.0"
	id("com.gradle.plugin-publish") version "0.15.0"
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
}

gradlePlugin {
	plugins {
		register("io.fluidsonic.gradle") {
			displayName = "fluidsonic library gradle configurator"
			description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
			id = "io.fluidsonic.gradle"
			implementationClass = "io.fluidsonic.gradle.LibraryPlugin"
		}
	}
}

pluginBundle {
	website = "https://github.com/fluidsonic/fluid-gradle"
	vcsUrl = "https://github.com/fluidsonic/fluid-gradle.git"
	description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
	tags = listOf("fluid-libraries")

	plugins {
		named("io.fluidsonic.gradle") {
			displayName = "fluidsonic library gradle configurator"
		}
	}
}

kotlin {
	explicitApi()
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

sourceSets {
	getByName("main") {
		kotlin.srcDirs(listOf("sources"))
	}
}

tasks {
	withType<KotlinCompile> {
		sourceCompatibility = "1.8"
		targetCompatibility = "1.8"

		kotlinOptions.apiVersion = "1.5"
		kotlinOptions.jvmTarget = "1.8"
		kotlinOptions.languageVersion = "1.5"
	}

	withType<Wrapper> {
		distributionType = Wrapper.DistributionType.ALL
		gradleVersion = "7.1"
	}
}

dependencyUpdates {
	gradleReleaseChannel = "current"
	outputFormatter = null

	rejectVersionIf {
		isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
	}
}


val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
if (ossrhUsername != null && ossrhPassword != null) {
	val javadocJar by tasks.creating(Jar::class) {
		archiveClassifier.set("javadoc")
		from(tasks["javadoc"])
	}

	val sourcesJar by tasks.creating(Jar::class) {
		archiveClassifier.set("sources")
		from(sourceSets["main"].allSource)
	}

	artifacts {
		archives(javadocJar)
		archives(sourcesJar)
	}

	publishing {
		repositories {
			maven {
				setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
				credentials {
					username = ossrhUsername
					password = ossrhPassword
				}
			}
		}

		publications {
			create<MavenPublication>("pluginMaven") {
				artifact(javadocJar)
				artifact(sourcesJar)
			}

			withType<MavenPublication> {
				pom {
					name.set(project.name)
					description.set(project.description)
					url.set("https://github.com/fluidsonic/${project.name}")
					developers {
						developer {
							id.set("fluidsonic")
							name.set("Marc Knaup")
							email.set("marc@knaup.io")
						}
					}
					licenses {
						license {
							name.set("Apache License 2.0")
							url.set("https://github.com/fluidsonic/${project.name}/blob/master/LICENSE")
						}
					}
					scm {
						connection.set("scm:git:https://github.com/fluidsonic/${project.name}.git")
						developerConnection.set("scm:git:git@github.com:fluidsonic/${project.name}.git")
						url.set("https://github.com/fluidsonic/${project.name}")
					}
				}
			}
		}
	}

	signing {
		sign(publishing.publications)
	}
}


val SourceSet.kotlin
	get() = withConvention(KotlinSourceSet::class) { kotlin }


fun dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) =
	tasks.withType(configuration)


fun isUnstableVersion(version: String) =
	Regex("\\b(alpha|beta|dev|eap|m|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
