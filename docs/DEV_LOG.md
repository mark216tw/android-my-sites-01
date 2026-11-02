# 開發紀錄

最新紀錄放最上面。每次開發工作都要更新，不要事後補寫。

---

## 2026-11-02 - v1.2.0（versionCode 4）

### 目標
- 在瀏覽頁放大字型（A+）按鈕右側新增重新載入按鈕，方便直接重載目前網頁。

### 變更
- `ui/web/SiteWebScreen.kt`：於 A+ 按鈕後新增 `Refresh` 圖示按鈕，點擊時清除錯誤訊息並呼叫 `WebView.reload()`；WebView 尚未建立時停用。
- `res/values/strings.xml`：新增 `refresh` 字串（重新載入）。
- `app/build.gradle.kts`：`versionCode` 由 3 遞增為 4，`versionName` 維持 1.2.0。
- `README.md`、`CHANGELOG.md`、`docs/使用指南.md`、`docs/系統架構與技術文件.md`、`docs/系統設計文件.md`、`docs/發行說明-v1.2.0-prerelease.md`、`docs/建置與發行指南.md`：同步更新功能說明、Version Code、APK 大小與 SHA-256。

### 建置與發行
- 驗證：`:app:assembleDebug`、`:app:testDebugUnitTest`（20 項）、`:app:lintDebug`、`:app:assemblePrerelease` 均通過。
- 產出：`app-prerelease.apk`，1,421,801 bytes。
- 版本：`versionCode 4`、`versionName 1.2.0-prerelease`。
- SHA-256：`71905D32FD7018E649D4A578139C2413856F319A678F254410ECC414CDDB23F8`。
- 已覆蓋 GitHub Release `v1.2.0-prerelease`。

### 問題與解法
- 無。

### 待辦／已知限制
- 尚未實機驗證重新載入後的字型倍率是否維持（沿用既有 `textZoom` 設定，預期維持）。

---

## 2026-10-31 - v1.2.0-prerelease（versionCode 3）

### 目標
- 字型倍率改為每個站台各自保存，不再全域共用。
- 重新開啟 App 時回到上次瀏覽的站台。
- 修正重啟後站台標題列空白數秒、網站內容卻先出現的問題。

### 變更
- `data/Site.kt`：新增 `textZoom` 欄位，預設 100。
- `data/SiteBackupCodec.kt`：匯入時讀取並夾在 50～200；匯出會包含倍率。
- `data/AppSettings.kt`、`data/SettingsStore.kt`：移除全域 `webTextZoom`；新增 `lastSiteId()` 與 `setLastSiteId()`。
- `ui/MySitesApp.kt`：等 `SiteStore.loaded` 後才建立 NavHost；依 `lastSiteId` 決定起始路由；站台頁縮放改為更新該站台資料；回首頁時清除上次站台。
- `ui/web/SiteWebScreen.kt`：改用 `site.textZoom` 套用與調整倍率。
- 新增 `docs/發行說明-v1.2.0-prerelease.md`，並更新 README、CHANGELOG、使用指南、系統設計、架構與建置發行文件。
- 版本：`versionCode 3`、`versionName 1.2.0`；`prerelease` 版號為 `1.2.0-prerelease`。
- 已建置 `app-prerelease.apk`：1,421,397 bytes，SHA-256 `173C7D25070F57992D3189FE2BC2D2C8811532F0EC6396F4A8BA1FE49F201B38`。
- 已發佈 GitHub Release `v1.2.0-prerelease`。

### 問題與解法
- Android 可能還原導覽狀態到站台頁，但 `sites.json` 是非同步載入；站台頁若太早組合，標題會依賴尚未就緒的站台資料。
- 解法：載入完成前只顯示載入指示，資料就緒後才建立 NavHost 與 WebView，讓標題與內容同時出現；並明確保存上次站台，避免只依賴系統狀態還原。
- 以站台為起始路由時，返回鍵若直接 `popBackStack()` 會沒有首頁可回；改為清空堆疊後導向 `home`。

### 待辦／已知限制
- WebView 內的瀏覽歷史不會跨 App 重啟還原，只會回到站台首頁網址。
- 既有站台升級後字型倍率為 100%。
- 目前環境無連接裝置，尚未實機驗證重啟後的畫面時序。

---

## YYYY-MM-DD - vX.Y.Z（versionCode N）

### 目標
（這次要做什麼、為什麼）

### 變更
- （檔案／功能）

### 問題與解法
- （錯誤訊息 → 最後怎麼解）

### 待辦／已知限制
- （尚未完成或未驗證的部分）
