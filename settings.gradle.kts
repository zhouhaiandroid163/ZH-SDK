pluginManagement {
    repositories {
        maven ("https://mirrors.huaweicloud.com/repository/maven/")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven ("https://mirrors.huaweicloud.com/repository/maven/")
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://developer.huawei.com/repo/")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Android-ZH-Watch-SDK-Example"
include(":app")
include(":imagepick")
include(":libopus")
