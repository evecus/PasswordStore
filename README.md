# 🔐 密码管理器 (Password Manager)

一款简洁、现代的 Android 密码管理应用，使用 AES-256-GCM 加密存储你的密码。

## ✨ 功能特性

- 🔐 **安全加密** — AES-256-GCM 加密，密钥存储于 Android Keystore
- 📁 **分组管理** — 创建彩色分组，让密码井然有序
- ⭐ **收藏功能** — 快速访问常用密码
- 🔍 **快速搜索** — 实时搜索名称、网站、账号
- 📋 **一键复制** — 轻松复制账号、密码、备注
- 🔑 **密码生成** — 随机生成高强度密码（16位混合字符）
- 🏷️ **字段完整** — 名称、网站/App、账号、密码、备注

## 📱 截图

界面采用白色背景 + Material Design 3 风格，简洁现代。

## 🚀 从 GitHub Releases 下载

前往 [Releases](../../releases) 页面下载最新版本的 APK。

> 安装时请在设置中允许"安装未知来源应用"

## 🔧 GitHub Actions 构建配置

### 第一步：添加 Secrets

在仓库的 **Settings → Secrets and variables → Actions** 中添加以下 4 个 Secret：

| Secret 名称 | 说明 | 值 |
|-------------|------|-----|
| `KEYSTORE_BASE64` | Keystore 文件的 Base64 编码 | 见下方 |
| `KEYSTORE_PASSWORD` | Keystore 密码 | `PassManager2024!` |
| `KEY_ALIAS` | 密钥别名 | `passmanager` |
| `KEY_PASSWORD` | 密钥密码 | `PassManager2024!` |

### KEYSTORE_BASE64 的值

将 `keystore_base64.txt` 文件的完整内容粘贴为 `KEYSTORE_BASE64` 的值。

### 第二步：运行 Workflow

1. 进入仓库的 **Actions** 标签页
2. 选择 **Build & Release APK**
3. 点击 **Run workflow**
4. 填写 Tag（如 `v1.0.0`）和 Release notes
5. 点击绿色的 **Run workflow** 按钮

构建完成后会自动在 Releases 页面发布，附带签名的 arm64 APK。

## 🏗️ 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Repository
- **数据库**: Room (SQLite)
- **加密**: Android Keystore + AES-256-GCM
- **依赖注入**: Hilt
- **导航**: Navigation Compose
- **最低 SDK**: Android 8.0 (API 26)

## 📦 构建说明

```bash
# 仅构建 arm64 release 版本
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/`

## 🔒 安全说明

- 密码使用 Android Keystore 的硬件级 AES-256-GCM 加密
- 密钥永不离开设备
- 应用禁止网络访问（无网络权限）
- 禁止备份（allowBackup=false）
