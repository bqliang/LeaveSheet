pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") //增加 jitPack Maven 仓库
        maven("https://developer.huawei.com/repo/") // 配置 HMS Core SDK 的 Maven 仓地址
    }
}
rootProject.name = "Leave Sheet"
include(":app")