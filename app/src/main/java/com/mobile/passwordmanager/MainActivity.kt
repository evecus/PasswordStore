package com.mobile.passwordmanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.mobile.passwordmanager.ui.AppRoot
import com.mobile.passwordmanager.ui.theme.PasswordManagerTheme

/**
 * 单 Activity 架构入口：所有界面由 Jetpack Compose 承载，Navigation Compose 负责页面切换。
 * 继承 FragmentActivity 以兼容 BiometricPrompt 1.1.0（其构造函数要求 FragmentActivity）。
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PasswordManagerTheme {
                AppRoot()
            }
        }
    }
}
