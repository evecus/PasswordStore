// Top-level build file
plugins {
    id("com.android.application") version "9.0.1" apply false
    // AGP 9.0 内置 Kotlin；Compose 编译器插件版本与内置 KGP(2.2.10) 对齐
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
