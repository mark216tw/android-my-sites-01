# 我的站台（My Sites）

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Release](https://img.shields.io/badge/release-v1.1.0--prerelease-orange)](https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.1.0-prerelease)

「我的站台」是一款以 Kotlin 與 Jetpack Compose 開發的 Android App，讓使用者把自己常用的網站收進清單，並直接在 App 內以 WebView 瀏覽。站台資料完全保存在裝置本機，可透過 JSON 匯出與匯入備份。

## 專案簡介

- **收藏站台**：輸入名稱與網址即可新增站台。
- **App 內瀏覽**：點擊站台後以全螢幕 WebView 開啟，保留精簡標題列。
- **個人化外觀**：支援系統／淺色／深色模式，以及 6 種預設主題色與 Hue 自訂色彩。
- **本機優先**：不需要帳號、不需要雲端服務，也不含分析或追蹤功能。

> **Pre-release 提醒**：目前提供的 `app-prerelease.apk` 是測試版本，使用 Debug 金鑰簽署並啟用 R8 壓縮，**不是正式上線版本**，不建議作為長期使用或正式發佈。

## 功能特色

- 新增、編輯、刪除站台
- 首頁站台卡片使用淡主題色底色，跟隨主題設定
- 首頁即時搜尋站台名稱與網址
- 長按拖曳排序，順序會自動保存
- App 內 WebView 瀏覽，支援載入進度、錯誤重試、外部連結與檔案下載
- 站台頁可放大／縮小字型（50%～200%），倍率會自動保存
- 站台頁左上角返回清單；Android 返回鍵會先回上一頁網頁，沒有上一頁才回清單
- JSON 匯出與匯入（使用系統檔案選擇器，不需要儲存權限）
- 顯示模式：系統、淺色、深色
- 主題色彩：6 個活潑預設色＋Hue 彩色滑桿，立即套用
- 系統狀態列與導覽列圖示會跟隨主介面主題
- 自訂 Adaptive App 圖示：地球儀、粗線條卡通風格、支援 Android 13+ 主題圖示

## 下載 Pre-release

- 版本：`1.1.0-prerelease`
- 發行頁：<https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.1.0-prerelease>
- APK：<https://github.com/mark216tw/android-my-sites-01/releases/download/v1.1.0-prerelease/app-prerelease.apk>
- SHA-256：`CCC8ADAA4E63B00E3D3DC1BE139C3FC09B0691FE319B761E731E9E7ED66C8500`

安裝前請確認 Android 裝置允許安裝未知來源的 APK。此版本使用 Debug 金鑰簽署，僅供測試與驗證。

## 系統需求

| 項目 | 需求 |
| --- | --- |
| Android 版本 | Android 8.0（API 26）以上 |
| 編譯 SDK | API 36 |
| 目標 SDK | API 36 |
| JDK | 17 以上 |
| 建置工具 | Gradle Wrapper 8.14.3、Android Gradle Plugin 8.13.2 |

## 快速開始

### 使用 Android Studio

1. 以 Android Studio 開啟專案根目錄。
2. 等待 Gradle Sync 完成。
3. 選擇 `app` 執行設定，部署到裝置或模擬器。

### 使用命令列

```powershell
git clone https://github.com/mark216tw/android-my-sites-01.git
cd android-my-sites-01
.\gradlew.bat :app:assembleDebug
```

macOS / Linux：

```bash
./gradlew :app:assembleDebug
```

產出位置：`app/build/outputs/apk/debug/app-debug.apk`

### 建置 Pre-release APK

```powershell
.\gradlew.bat :app:assemblePrerelease
```

產出位置：`app/build/outputs/apk/prerelease/app-prerelease.apk`

## 操作概覽

1. 首頁點擊右上角 **+** 新增站台，輸入名稱與網址。
2. 點擊站台卡片，在 App 內開啟網站。
3. 站台頁左上角 **←** 回到清單；Android 返回鍵會先處理 WebView 上一頁。
4. 站台頁右上角 **A−／A+** 可縮小或放大字型，倍率會自動保存。
5. 首頁搜尋列可過濾名稱或網址。
6. 長按站台左側拖曳把手可排序。
7. 站台右側選單可編輯或刪除。
8. 右上角另有匯入 JSON、匯出 JSON 與設定。
9. 設定頁可切換顯示模式、選擇主題色或拖曳 Hue 滑桿。

更完整的使用說明請見 [使用指南](docs/使用指南.md)。

## 專案結構

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/mysites/
│       │   │   ├── MainActivity.kt
│       │   │   ├── MySitesApplication.kt
│       │   │   ├── data/          # 站台、設定、JSON、URL 正規化
│       │   │   └── ui/            # 首頁、站台瀏覽、設定、主題
│       │   └── res/               # 字串、主題、圖示
│       └── test/                  # JVM 單元測試
├── docs/                          # 繁體中文專案文件
├── gradle/                        # Gradle Wrapper
├── build.gradle.kts
├── settings.gradle.kts
├── LICENSE
└── README.md
```

## 文件

- [使用指南](docs/使用指南.md)
- [系統架構與技術文件](docs/系統架構與技術文件.md)
- [系統設計文件](docs/系統設計文件.md)
- [建置與發行指南](docs/建置與發行指南.md)
- [v1.1.0-prerelease 發行說明](docs/發行說明-v1.1.0-prerelease.md)
- [v1.0.0-prerelease 發行說明](docs/發行說明-v1.0.0-prerelease.md)
- [變更紀錄](CHANGELOG.md)

## 建置版本

| Build Type | R8 壓縮 | 簽署 | 版本名稱 | 用途 |
| --- | --- | --- | --- | --- |
| `debug` | 否 | Debug 金鑰 | `1.1.0` | 本機開發 |
| `release` | 否 | 未設定正式簽署 | `1.1.0` | 預留正式發行 |
| `prerelease` | 是（含資源縮減） | Debug 金鑰 | `1.1.0-prerelease` | 測試發行 |

## 驗證狀態

- `:app:assembleDebug`：通過
- `:app:testDebugUnitTest`：18 項測試通過
- `:app:lintDebug`：通過
- `:app:assemblePrerelease`：通過（R8、資源縮減、Debug 簽署）
- 實機／模擬器瀏覽與系統列視覺驗證：目前環境無連接裝置，尚未執行

## 隱私與安全

- 站台資料只儲存在 App 內部儲存空間，不會上傳。
- 僅宣告 `INTERNET` 權限；匯出／匯入使用系統 SAF，不需要儲存權限。
- WebView 關閉檔案與內容存取、不略過 SSL 錯誤、外部 scheme 交由系統處理。
- 為了支援使用者自行輸入的 `http://` 站台（例如區域網路設備），App 允許 cleartext traffic；請自行評估風險。
- Pre-release APK 使用 Debug 金鑰簽署，不可視為正式安全發行。

## 授權

本專案採用 [MIT License](LICENSE)。

```text
Copyright (c) 2026 mark216tw
```

## 作者

- GitHub：[@mark216tw](https://github.com/mark216tw)
- Email：mark216tw@gmail.com
