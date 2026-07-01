import com.android.build.api.artifact.SingleArtifact
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val allProperties = mutableMapOf<String, Properties>()
val localProperties = loadPropertiesFile(rootProject.file("local.properties"))

val buildinVersion = getConfigProperty("common.properties", "buildinVersion", "false").toBoolean()
val buildinChannels = getConfigProperty("common.properties", "buildinChannels", "23601|30017")
val defaultChannel = if (buildinVersion) "23601" else "23600"

val executableSuffix = if (OperatingSystem.current().isWindows) ".exe" else ""
val scriptExecutableSuffix = if (OperatingSystem.current().isWindows) ".bat" else ""
val sdkDir = localProperties.getProperty(
    "sdk.dir",
    System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") ?: ""
)
val buildToolsVersionName = localProperties.getProperty("app.buildToolsVersion", "30.0.2")
val rezipWorkDir = layout.buildDirectory.dir("rezip").get().asFile.absolutePath
val sevenZipTool = localProperties.getProperty(
    "app.sevenZipPath",
    if (OperatingSystem.current().isWindows) (System.getenv("7ZIP_PATH") ?: "7za.exe") else "7za"
)
val zipalignTool = if (sdkDir.isNotBlank()) {
    "$sdkDir/build-tools/$buildToolsVersionName/zipalign$executableSuffix"
} else {
    ""
}
val apksignerTool = if (sdkDir.isNotBlank()) {
    "$sdkDir/build-tools/$buildToolsVersionName/apksigner$scriptExecutableSuffix"
} else {
    ""
}

val keyStorePath = getLocalProperty("app.keystore.file", "${projectDir.absolutePath}/demotest.jks")
val keyStorePassword = getLocalProperty("app.keystore.password", "money999999999")
val keyAliasName = getLocalProperty("app.keystore.alias", "demotest")
val keyAliasPassword = getLocalProperty("app.keystore.keyPassword", keyStorePassword)

val preinstallChannelValues = mapOf(
    "23601" to "buildin_int",
    "30017" to "shalltry_int",
    "33601" to "vivopreload_int",
    "33602" to "oppo_int",
    "33610" to "samsung_int",
)

android {
    namespace = "com.onus.demotest"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.onus.demotest"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        maybeCreate("debug").apply {
            storeFile = file(keyStorePath)
            storePassword = keyStorePassword
            keyAlias = keyAliasName
            keyPassword = keyAliasPassword
        }
        maybeCreate("release").apply {
            storeFile = file(keyStorePath)
            storePassword = keyStorePassword
            keyAlias = keyAliasName
            keyPassword = keyAliasPassword
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("boolean", "BUILDIN_VERSION", buildinVersion.toString())
            buildConfigField("int", "BUILD_CHANNEL", defaultChannel)
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("boolean", "BUILDIN_VERSION", buildinVersion.toString())
            buildConfigField("int", "BUILD_CHANNEL", defaultChannel)
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile(
                if (buildinVersion) {
                    "src/main/AndroidManifest_BuildIn.xml"
                } else {
                    "src/main/AndroidManifest.xml"
                }
            )
            java.setSrcDirs(listOf("src/main/kotlin"))
        }
        getByName("test") {
            java.setSrcDirs(listOf("src/test/kotlin"))
        }
        getByName("androidTest") {
            java.setSrcDirs(listOf("src/androidTest/kotlin"))
        }
    }

    if (buildinVersion) {
        flavorDimensions += "distribution"
        productFlavors {
            buildinChannels.split("|").forEach { channel ->
                create(channel) {
                    buildConfigField("int", "BUILD_CHANNEL", channel)
                    manifestPlaceholders["AF_PRE_INSTALL_VALUE"] =
                        preinstallChannelValues[channel] ?: "normal_int"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    androidResources {
        ignoreAssetsPattern = "ic_launcher_foreground_phx.xml"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = !buildinVersion
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.lifecycle:lifecycle-service:2.10.0")
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.firebase:firebase-messaging:25.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.media:media:1.7.1")
    implementation("androidx.media3:media3-session:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")
    implementation("androidx.media3:media3-exoplayer:1.8.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

if (buildinVersion) {
    androidComponents {
        onVariants(selector().withBuildType("release")) { variant ->
            val apkProvider = variant.artifacts.get(SingleArtifact.APK)
            val variantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            val assembleTaskName = "assemble$variantName"
            val postAssembleTask = tasks.register("postAssemble$variantName") {
                dependsOn(assembleTaskName)
                doLast {
                    validateBuildTools()

                    val outputFile = apkProvider.get().asFile
                    println("BuildDebug start ${outputFile.absolutePath}")
                    unzipReleaseApk(outputFile.absolutePath)

                    val channel = detectChannelFromApkName(outputFile.name)
                    val channelFile = File("$rezipWorkDir/unzip/assets", "channel.ini")
                    channelFile.parentFile.mkdirs()
                    channelFile.writeText("CHANNEL=$channel")

                    rezipReleaseApk(channel)
                    zipalignReleaseApk(channel)
                    signReleaseApk(channel)

                    File(rezipWorkDir).deleteRecursively()
                    println("BuildDebug end ${outputFile.absolutePath}")
                }
            }

            tasks.named(assembleTaskName).configure {
                finalizedBy(postAssembleTask)
            }
        }
    }
}

fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key, defaultValue)
}

fun getConfigProperty(fileName: String, propertyName: String, defaultValue: String = ""): String {
    return loadPropertiesFile(rootProject.file(fileName)).getProperty(propertyName, defaultValue)
}

fun loadPropertiesFile(file: File): Properties {
    val existing = allProperties[file.absolutePath]
    if (existing != null) {
        return existing
    }

    val props = Properties()
    if (file.exists()) {
        file.inputStream().use { input ->
            props.load(input)
        }
    }
    allProperties[file.absolutePath] = props
    return props
}

fun detectChannelFromApkName(apkName: String): String {
    return buildinChannels.split("|").firstOrNull { apkName.contains(it) } ?: defaultChannel
}

fun validateBuildTools() {
    if (!sevenZipTool.equals("7za") && !sevenZipTool.equals("7za.exe") && !File(sevenZipTool).exists()) {
        throw GradleException("7za not found: $sevenZipTool")
    }
    if (zipalignTool.isBlank() || !File(zipalignTool).exists()) {
        throw GradleException("zipalign not found, configure sdk.dir or app.buildToolsVersion in local.properties")
    }
    if (apksignerTool.isBlank() || !File(apksignerTool).exists()) {
        throw GradleException("apksigner not found, configure sdk.dir or app.buildToolsVersion in local.properties")
    }
    if (!File(keyStorePath).exists()) {
        throw GradleException("keystore not found: $keyStorePath")
    }
}

fun unzipReleaseApk(apkPath: String) {
    println("unzipReleaseApk:$apkPath")
    copy {
        from(zipTree(apkPath))
        into("$rezipWorkDir/unzip")
    }

    copy {
        from("$rezipWorkDir/unzip")
        into("$rezipWorkDir/arsctmp")
        include("resources.arsc")
    }

    delete(fileTree("$rezipWorkDir/unzip") {
        include("resources.arsc")
    })
}

fun rezipReleaseApk(channel: String) {
    println("rezipReleaseApk:$channel")
    runExternalCommand(
        sevenZipTool,
        listOf(
            "a", "-tzip", "-mx=9",
            "$rezipWorkDir/app_android${channel}.apk_TMP",
            "$rezipWorkDir/unzip/*.*",
            "$rezipWorkDir/unzip/assets",
            "$rezipWorkDir/unzip/kotlin"
        )
    )

    runExternalCommand(
        sevenZipTool,
        listOf(
            "a", "-tzip", "-mx=0",
            "$rezipWorkDir/app_android${channel}.apk_TMP",
            "$rezipWorkDir/unzip/res",
            "$rezipWorkDir/arsctmp/*.*"
        )
    )

    File("$rezipWorkDir/app_android${channel}.apk_TMP")
        .renameTo(File("$rezipWorkDir/app_android_unzipalign${channel}.apk"))
}

fun zipalignReleaseApk(channel: String) {
    runExternalCommand(
        zipalignTool,
        listOf(
            "-v", "-p", "-f", "4",
            "$rezipWorkDir/app_android_unzipalign${channel}.apk",
            "$rezipWorkDir/app_android_unsign${channel}.apk"
        )
    )
}

fun signReleaseApk(channel: String) {
    runExternalCommand(
        apksignerTool,
        listOf(
            "sign",
            "--ks", keyStorePath,
            "--ks-key-alias", keyAliasName,
            "--ks-pass", "pass:$keyStorePassword",
            "--key-pass", "pass:$keyAliasPassword",
            "--in", "$rezipWorkDir/app_android_unsign${channel}.apk",
            "--out", "${layout.buildDirectory.get().asFile.absolutePath}/outputs/apk/app_android${channel}.apk"
        )
    )
}

fun runExternalCommand(executablePath: String, arguments: List<String>) {
    providers.exec {
        commandLine(listOf(executablePath) + arguments)
    }.result.get()
}
