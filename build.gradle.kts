import com.diffplug.spotless.changelog.Changelog
import com.diffplug.spotless.changelog.ChangelogAndNext
import com.diffplug.spotless.changelog.NextVersionCfg
import com.diffplug.spotless.changelog.NextVersionFunction.NonSemver
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
		classpath("com.diffplug.spotless-changelog:spotless-changelog-lib:1.1.0")
	}
}

plugins {
	kotlin("jvm") version "1.3.41"
	`maven-publish`
	id("com.github.breadmoirai.github-release") version "2.2.11"
	id("com.matthewprenger.cursegradle") version "1.4.0"
}
apply(plugin = "net.minecraftforge.gradle")

// Get a value from either the environment or gradle properties.
fun getProp(key: String): String? {
	val value = System.getenv(key) ?: project.findProperty(key) as? String
	value ?: println("Cannot find $key in environment or gradle properties.")
	return value
}

// Pattern that can break the version number + tag info up into segments.
// Example: 1.14.4-1.0.2.3-alpha1-24-fdf3d3
// mc: 1.14.4
// mod: 1.0.2.3-alpha1-24-fdf3d3
// sem: 1.0.2.3
// pre: -alpha1
// snap: -24-fdf3d3
val VERSION_PATTERN =
	"(?<mc>\\d+\\.\\d+\\.\\d+)-(?<mod>(?<sem>\\d+\\.\\d+\\.\\d+\\.\\d+)(?<pre>-[a-z][^-]*)?(?<snap>-[0-9].*)?)"

// Read the changelog. Used for release notes or to determine the snapshot version, depending on which is needed.
val nextVersionFunction = object : NonSemver() {
	private val ifFoundBumpMod = listOf("**BREAKING**")
	private val ifFoundBumpApi = listOf("**BREAKING API**")
	private val ifFoundBumpMinor = listOf("### Added")

	private fun isFound(unreleasedChanges: String, lookFor: List<String>) =
		lookFor.any { unreleasedChanges.contains(it, false) }

	override fun nextVersion(unreleasedChanges: String, lastVersion: String?): String? {
		val lastMatch = VERSION_PATTERN.toPattern().matcher(lastVersion ?: "0.0.0.0")
		if (!lastMatch.matches()) {
			throw GradleException("Unable to parse last version ($lastVersion} from changelog")
		}

		// If the previous version was marked as prerelease, assume the version number was already incremented correctly.
		if (lastMatch.group("pre") != null) {
			return lastMatch.group("sem")
		}

		val parts = lastMatch.group("sem").split(".").map(String::toInt).toMutableList()

		if (isFound(unreleasedChanges, ifFoundBumpMod)) {
			parts[0] += 1
			parts[1] = 0
			parts[2] = 0
			parts[3] = 0
		} else if (isFound(unreleasedChanges, ifFoundBumpApi)) {
			parts[1] += 1
			parts[2] = 0
			parts[3] = 0
		} else if (isFound(unreleasedChanges, ifFoundBumpMinor)) {
			parts[2] += 1
			parts[3] = 0
		} else {
			parts[3] += 1
		}

		return "${parts[0]}.${parts[1]}.${parts[2]}.${parts[3]}"
	}
}
val nextVersionCfg = NextVersionCfg().also { it.function = nextVersionFunction }
val changelogFile = project.file(ChangelogAndNext.DEFAULT_FILE)
val changelogModel = ChangelogAndNext.calculate(changelogFile, nextVersionCfg)

// Grab & parse tag info. This gives both the name of the last tag, as well as an extra marker if the current commit is
// not the one that is tagged, which is used for the snapshot names.
val gitDescribe = System.getenv("GIT_TAG") ?: "git describe --tags".runCommand()!!.trim()
val match = "^v$VERSION_PATTERN$".toPattern().matcher(gitDescribe)!!
if (!match.matches()) {
	throw Exception("Unable to parse version from git tag ($gitDescribe).")
}

// Determine the current version based on the changelog and git tag info collected above.
// If we're on a tag, then this tag is the version, simple as that.
// If we're not, bump the version using the changelog, and add the snapshot info collected from git.
val minecraftVersion = match.group("mc")!!
val isSnapshot = match.group("snap") != null
val isPreRelease = match.group("pre") != null
val modVersion = if (!isSnapshot) match.group("mod")!! else "${changelogModel.versions().next()}${match.group("snap")}"
println("Version $modVersion for minecraft $minecraftVersion (snapshot: $isSnapshot, prerelease: $isPreRelease)")

// Accessor for the current changelog entry. Uses reflection because the library only exposes unreleased changes.
val currentChangelog by lazy {
	val versionsRawField = Changelog::class.java.getDeclaredField("versionsRaw")
	versionsRawField.isAccessible = true
	val versionsRaw = versionsRawField.get(changelogModel.changelog()) as List<Changelog.VersionEntry>
	if (isSnapshot) {
		versionsRaw.find { it.isUnreleased } ?: throw GradleException("Changelog should contain an [Unreleased] section")
	} else {
		versionsRaw.find { !it.isUnreleased && it.version().toString() == version }
			?: throw GradleException("Changelog should contain an [$version] section")
	}
}

version = "$minecraftVersion-$modVersion"
group = "com.maienm"
base.archivesBaseName = "accessibilitymod"

val forgeVersion = "$minecraftVersion-28.1.107"
val forgeMappingsVersion = arrayOf("snapshot", "20190719-1.14.3") // http://export.mcpbot.bspk.rs

repositories {
	mavenLocal()
	jcenter()
	mavenCentral()
	flatDir {
		dir("mods")
	}
}

configure<UserDevExtension> {
	mappings(forgeMappingsVersion[0], forgeMappingsVersion[1])

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
		attributes["Implementation-Version"] = modVersion
		attributes["Implementation-Timestamp"] = LocalDateTime.now()
	}
}

task("changelogCheck") {
	group = "documentation"

	doFirst {
		val errors = changelogModel.changelog().errors()
		if (!errors.isEmpty()) {
			val path = getProject().getRootDir().toPath().relativize(changelogFile.toPath()).toString()
			val allErrors = errors
				.map { (index, error) -> "$path${if (index != -1) ":$index" else ""}: $error" }
				.joinToString("\n")
			throw GradleException(allErrors)
		}
	}
}
tasks["check"].dependsOn("changelogCheck")

publishing {
	publications {
		create<MavenPublication>(project.name) {
			from(components["java"])

			groupId = group.toString()
			artifactId = "AccessibilityMod"

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
			name = "Github"
			url = URI.create("https://maven.pkg.github.com/MaienM/AccessibilityMod")
			credentials {
				username = "MaienM"
				password = getProp("GITHUB_TOKEN")
			}
		}
	}
}
githubRelease {
	repo("AccessibilityMod")
	owner("MaienM")
	setToken(getProp("GITHUB_TOKEN"))
	targetCommitish(gitDescribe)
	releaseName(version as String)
	prerelease(isPreRelease)
	body { PatchedString(currentChangelog.changes().trim().toString()) }
	releaseAssets(tasks["jar"].outputs.files.files)
	dryRun(true)
}
curseforge {
	this.project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
		apiKey = getProp("CURSEFORGE_API_KEY")
		id = "363598"
		changelog = currentChangelog.changes().trim()
		releaseType = if (isPreRelease) "beta" else "release"
	})
}
task("release") {
	group = "publishing"

	dependsOn("jar")
	doFirst {
		if (isSnapshot) {
			throw GradleException("Current commit is not properly tagged as release")
		}
		if (currentChangelog.isUnreleased) {
			throw GradleException("Latest entry in changelog is still marked as unreleased")
		}
		if (currentChangelog.version().toString() != version) {
			throw GradleException("Version of last entry in changelog (${currentChangelog.version()}) should be $version")
		}
		finalizedBy("githubRelease", "curseforge")
	}
}

dependencies {
	"minecraft"("net.minecraftforge:forge:$forgeVersion")

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

/**
 * Run a command from within the project directory root.
 *
 * From https://stackoverflow.com/a/41495542.
 */
fun String.runCommand(): String? {
	try {
		val parts = this.split("\\s".toRegex())
		val proc = ProcessBuilder(*parts.toTypedArray())
			.directory(projectDir)
			.redirectOutput(ProcessBuilder.Redirect.PIPE)
			.redirectError(ProcessBuilder.Redirect.PIPE)
			.start()

		proc.waitFor(10, TimeUnit.SECONDS)
		return proc.inputStream.bufferedReader().readText()
	} catch (e: java.io.IOException) {
		e.printStackTrace()
		return null
	}
}

/**
 * GithubPublish uses a method that doesn't exist, which causes problems. This wrapper class provides this method.
 */
class PatchedString(private val string: String) : CharSequence by string {
	fun replace(replacements: Map<String, String>) = replacements.entries.fold(string) { str, entry ->
		str.replace(Regex.fromLiteral(entry.key), entry.value)
	}
}
