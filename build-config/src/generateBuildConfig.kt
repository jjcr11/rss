package com.jjcr11.rss.buildconfig

import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeText
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction

@TaskAction
@OptIn(ExperimentalPathApi::class)
fun generateBuildConfig(
    @Input propertiesFile: Path,
    @Output generatedSourceDir: Path,
) {
    generatedSourceDir.deleteRecursively()
    val outputFile = generatedSourceDir / "BuildConfig.kt"

    val properties = Properties()
    if (propertiesFile.isRegularFile()) {
        propertiesFile.bufferedReader().use { reader ->
            properties.load(reader)
        }
    }

    val supabaseUrl = properties.getProperty("SUPABASE_URL") ?: ""
    val supabaseKey = properties.getProperty("SUPABASE_KEY") ?: ""

    outputFile.createParentDirectories()
    val code = buildString {
        appendLine("package com.jjcr11.rss")
        appendLine()
        appendLine("public object BuildConfig {")
        appendLine("    public const val SUPABASE_URL: String = \"$supabaseUrl\"")
        appendLine("    public const val SUPABASE_KEY: String = \"$supabaseKey\"")
        appendLine("}")
    }
    outputFile.writeText(text = code)
}
