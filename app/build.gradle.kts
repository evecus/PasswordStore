plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.mobile.passwordmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mobile.passwordmanager"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 只保留中文（含简繁）与英文资源，裁剪其它语言（Compose/ androidx 自带几十种语言）
        resourceConfigurations += listOf("zh", "zh-rCN", "zh-rTW", "en")
    }

    signingConfigs {
        create("release") {
            // 这些值从环境变量读取，本地不设置也不影响 debug 构建
            // CI 上通过 GitHub Secrets 注入
            val storeFilePath = System.getenv("RELEASE_STORE_FILE")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // R8 代码压缩 + 资源压缩
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 只有在环境变量存在（即 CI 配置了签名）时才应用签名配置
            if (System.getenv("RELEASE_STORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // BOM 锁定 Compose 1.11.x，兼容 AGP 9.0.1 + compileSdk 36（1.12.x 要求 AGP 9.1/SDK 37）
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.biometric:biometric:1.1.0")
    // FragmentActivity 作为单 Activity 基类，满足 BiometricPrompt 1.1.0 构造函数要求
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    // 离线图标字体库:用于分组/密码条目选择品牌图标(Simple Icons)
    implementation("com.mikepenz:iconics-core:5.5.0")
    implementation("com.mikepenz:simple-icons-typeface:17.0.0.1@aar")
}
