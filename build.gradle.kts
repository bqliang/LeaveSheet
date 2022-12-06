// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}
extra.apply {
    val gitCommitId = ProcessBuilder("git", "rev-parse", "--short", "HEAD").execute()
    val gitCommitCount = ProcessBuilder("git", "rev-list", "--count", "HEAD").execute().toInt()
    val gitTag = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").execute().let { tag ->
        if (tag.isEmpty()) "" else "$tag."
    }
    set("appVersionCode", gitCommitCount)
    set("appVersionName", "${gitTag}r${gitCommitCount}.${gitCommitId}")
}

fun ProcessBuilder.execute() = this.start().inputStream.bufferedReader().readText().trim()