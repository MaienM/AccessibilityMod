import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FilterReader
import java.io.PipedReader
import java.io.PipedWriter
import java.io.Reader
import java.io.Writer
import java.util.function.BiConsumer

buildscript {
	repositories {
		jcenter()
		mavenCentral()
		maven(url = "http://files.minecraftforge.net/maven")
	}
	dependencies {
		classpath(kotlin("gradle-plugin", version = "1.3.41"))
		classpath("net.minecraftforge.gradle:ForgeGradle:3.+")
		classpath("org.yaml:snakeyaml:1.25")
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

tasks.withType<ProcessResources> {
	// Flatten yaml language files and convert them to json.
	// { "foo": { "bar": "Hello", "baz": "World" } } becomes { "foo.bar": "Hello", "foo.baz": "World" }
	filesMatching("**/lang/*.yml") {
		relativePath = relativePath.replaceLastName(relativePath.lastName.replace("\\.yml$".toRegex(), ".json"))

		fun transform(reader: Reader, writer: Writer) {
			val config: HashMap<String, *> = org.yaml.snakeyaml.Yaml().load(reader)
			val flattenedConfig = LinkedHashMap<String, Any>()
			walkNestedMaps(config) { keys: List<String>, value: Any ->
				flattenedConfig.put(keys.joinToString("."), value)
			}
			com.google.gson.Gson().toJson(flattenedConfig, writer)
		}
		filter(mutableMapOf("transform" to ::transform), TransformFilter::class.java)
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

/**
 * A FilterReader that allows transforming the entire file at once, instead of line-by-line.
 */
class TransformFilter(val originalReader: Reader): FilterReader(PipedReader()) {
	fun setTransform(transform: (Reader, Writer) -> Unit) {
		val writer = PipedWriter()
		(this.`in` as PipedReader).connect(writer)
		transform(this.originalReader, writer)
		writer.close()
	}
}

/**
 * Walk over all non-map values in a nested map.
 *
 * Take the following map:
 *   { "top1": { "sub1": "foo", "sub2": "bar" }, "top2": "baz" }.
 * This will invoke the callable 3 times:
 *   callable(["top1", "sub1"], "foo")
 *   callable(["top1", "sub2"], "bar")
 *   callable(["top2"], "baz")
 *
 * @param map The map to walk over.
 * @param callback A callback that will be invoked with a list of keys and a value for each value in the nested maps.
 */
fun walkNestedMaps(map: Map<String, Any>, keys: MutableList<String> = ArrayList(), callback: (List<String>, Any) -> Unit) {
	map.entries.forEach { entry ->
		keys.add(keys.size, entry.key)
		val entryMap = entry.value as? Map<String, Any>
		if (entryMap != null) {
			walkNestedMaps(entryMap, keys, callback)
		} else {
			callback(keys, entry.value)
		}
		keys.removeAt(keys.size - 1)
	}
}
