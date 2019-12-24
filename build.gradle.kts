import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		jcenter()
		maven(url = "http://files.minecraftforge.net/maven")
	}
	dependencies {
		classpath(kotlin("gradle-plugin", version = "1.3.41"))
		classpath("net.minecraftforge.gradle:ForgeGradle:3.+")
	}
}

plugins {
	kotlin("jvm") version "1.3.41"
}
apply(plugin = "net.minecraftforge.gradle")

val group = "com.maienm.accessibilitymod"
val archivesBaseName = "accessibilitymod"
val version = "0.1.0-SNAPSHOT"

object versions {
	val forge = "1.14.4-28.1.107"
	val forge_mappings = arrayOf("snapshot", "20190719-1.14.3")
}

repositories {
	mavenLocal()
	jcenter()
	mavenCentral()
	flatDir {
		dir("mods")
	}
}

configure<UserDevExtension> {
	mappings(versions.forge_mappings[0], versions.forge_mappings[1])

	runs {
		val sharedConfig = Action<RunConfig> {
			workingDirectory(project.file("run"))
			properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP,COREMOD"
			properties["forge.logging.console.level"] = "debug"
			mods {
				create (project.name) {
					source(java.sourceSets["main"])
				}
			}
		}

		create("client", sharedConfig)
		create("server", sharedConfig)
		create("data") {
			sharedConfig.execute(this)
			args("--mod", "accessibilitymod", "--all", "--output", file("src/generated/resources/"))
		}
	}
}

dependencies {
	"minecraft"("net.minecraftforge:forge:${versions.forge}")

	implementation("io.opencubes:boxlin:3.0.1")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.41")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.41")

	// To include other mods while testing, without actually depending on them.
	// val fg = project.extensions.findByType(DependencyManagementExtension::class.java)!!
	// implementation(fg.deobf("foo:foo-1.14.4:1.2.3-alpha")) -> "mods/foo-1.14.4-1.2.3-alpha.jar"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
