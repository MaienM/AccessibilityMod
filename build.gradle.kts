import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FilterReader
import java.io.PipedReader
import java.io.PipedWriter
import java.io.Reader
import java.io.Writer
import java.net.URI
import java.time.LocalDateTime

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
	`maven-publish`
}
apply(plugin = "net.minecraftforge.gradle")

object versions {
	val minecraft = "1.14.4"
	val mod = "0.1.0.0-SNAPSHOT"
	// http://export.mcpbot.bspk.rs
	val forge = "${versions.minecraft}-28.1.107"
	val forge_mappings = arrayOf("snapshot", "20190719-1.14.3")
}

version = "${versions.minecraft}-${versions.mod}"
group = "com.maienm"
base.archivesBaseName = "accessibilitymod"

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
			properties["com.maienm.accessibilitymod.debug"] = ""
			mods {
				create(project.name) {
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

tasks.withType<Jar> {
	finalizedBy("reobfJar")

	manifest {
		attributes["Specification-Title"] = project.name
		attributes["Specification-Vendor"] = "MaienM"
		attributes["Specification-Version"] = "1"
		attributes["Implementation-Title"] = project.name
		attributes["Implementation-Vendor"] = "MaienM"
		attributes["Implementation-Version"] = versions.mod
		attributes["Implementation-Timestamp"] = LocalDateTime.now()
	}
}

publishing {
	publications {
		create<MavenPublication>(project.name) {
			from(components["java"])

			pom {
				withXml {
					val root = asNode()
					root.appendNode("name", "AccessibilityMod")
					root.appendNode("description", "A Minecraft mod attempting to make some things a bit more accessible.")
					root.appendNode("url", "https://github.com/MaienM/AccessibilityMod")
				}
				licenses {
					license {
						name.set("MIT License")
						url.set("http://www.opensource.org/licenses/mit-license.php")
						distribution.set("repo")
					}
				}
				developers {
					developer {
						id.set("maienm")
						name.set("Michon van Dooren")
						email.set("michon1992@gmail.com")
					}
				}
				scm {
					url.set("https://github.com/MaienM/AccessibilityMod")
					connection.set("scm:git:git://github.com/MaienM/AccessibilityMod.git")
					developerConnection.set("scm:git:ssh://github.com/MaienM/AccessibilityMod.git")
				}
			}
		}
	}
	repositories {
		maven {
			url = URI.create("file:///${project.projectDir}/mcmodsrepo")
		}
	}
}

dependencies {
	"minecraft"("net.minecraftforge:forge:${versions.forge}")

	implementation("io.opencubes:boxlin:3.0.1")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.41")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.41")

	// Include any mods in the mods/ directory when running, to easily test with other mods present.
	val fg = project.extensions.findByType(DependencyManagementExtension::class.java)!!
	fileTree("mods").also { it.include("*.jar") }.forEach {
		val match = "^(?<B>(?<A>[^-]*)(?:-[0-9.]*)?)-(?<C>.*)$".toPattern().toRegex().find(it.nameWithoutExtension)
		if (match != null) {
			val name = "${match.groups.get("A")!!.value}:${match.groups.get("B")!!.value}:${match.groups.get("C")!!.value}"
			implementation(fg.deobf(name))
		}
	}
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

/**
 * A FilterReader that allows transforming the entire file at once, instead of line-by-line.
 */
class TransformFilter(val originalReader: Reader) : FilterReader(PipedReader(65536)) {
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
fun walkNestedMaps(
	map: Map<String, Any>,
	keys: MutableList<String> = ArrayList(),
	callback: (List<String>, Any) -> Unit
) {
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
