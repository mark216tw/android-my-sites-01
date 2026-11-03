# 變更紀錄

本專案遵循簡化的版本紀錄格式。日期為西元年-月-日。

## [1.4.0] - 2026-11-03（versionCode 7）

### 新增

- 離開站台時自動記住目前網址，30 分鐘內重新開啟同一站台會從該網址繼續瀏覽
- 切換到背景或關閉螢幕時也會保存目前網址
- 點擊瀏覽頁的站台名稱可回到站台設定的網址

### 變更

- 刪除站台、修改站台網址或匯入備份時，會清除對應的記住網址

### 建置

- 版本名稱：`1.4.0-prerelease`
- Version Code：`7`
- 產出：`app-prerelease.apk`
- APK 大小：1,484,885 bytes
- SHA-256：`6B1635F4A047DA361B7725370F0D0EAE71B6527BA35475C85A68D7B96F2B7359`

### 測試

- 30 項 JVM 單元測試通過
- Android Lint 通過

## [1.3.0] - 2026-11-02（更新 2，versionCode 6）

### 變更

- 首頁右上角新增更多（⋮）選單，匯入 JSON、匯出 JSON 與設定改由選單操作
- 移除首頁的匯入、匯出與設定獨立圖示

### 建置

- 版本名稱：`1.3.0-prerelease`
- Version Code：`6`
- 產出：`app-prerelease.apk`
- APK 大小：1,484,465 bytes
- SHA-256：`C86308CE8000B2F6EF06A2E21151C0E64384B738A6A979E19D5CC8D2AA77E974`

## [1.3.0] - 2026-11-02

### 新增

- 站台標籤
  - 每個站台可選擇多個標籤
  - 標籤可設定名稱、16 種顏色，並可拖曳自訂排序
  - 標籤可新增、編輯、刪除
  - 刪除標籤時會同步從站台移除
- 站台最愛
  - 站台卡片可切換最愛狀態
  - 首頁可篩選「全部」、「最愛」與各標籤
- 首頁排序
  - 搜尋列右側新增排序選單
  - 支援自訂順序、名稱（A→Z／Z→A）、最近使用（新→舊／舊→新）、加入時間（新→舊／舊→新）
  - 排序選擇會保存
- 標籤管理入口
  - 站台編輯的標籤選擇視窗可直接新增／管理標籤
  - 設定頁也可開啟標籤管理

### 變更

- JSON 備份格式提升為 version 2，包含標籤、最愛、最近使用與站台標籤關聯
- 舊版 version 1 備份與原始陣列格式仍可匯入
- 站台卡片新增最愛星號與標籤顯示

### 建置

- 版本名稱：`1.3.0-prerelease`
- Version Code：`5`
- 產出：`app-prerelease.apk`
- APK 大小：1,484,465 bytes
- SHA-256：`46A3DB73494BA423448079CC02168A4B8BB4A67D8E427EE29957EE5FCA69BB53`

### 測試

- 30 項 JVM 單元測試通過
- Android Lint 通過

## [1.2.0-prerelease] - 2026-11-02

### 新增

- 瀏覽頁放大字型（A+）按鈕右側新增重新載入按鈕
- 每個站台各自保存字型倍率
  - 倍率改為存在站台資料中，切換站台時自動套用該站台自己的設定
  - 新站台與既有站台預設為 100%，範圍 50%～200%
  - 匯出／匯入 JSON 會一併帶走字型倍率
- 重新開啟 App 時回到上次瀏覽的站台
  - 明確保存最後開啟的站台，不必依賴系統的導覽狀態還原
  - 站台資料載入完成後才建立瀏覽頁，標題列與網頁同時出現，不再先顯示內容後才補上標題

### 變更

- 移除全域字型倍率設定，改由各站台獨立管理
- 啟動時先顯示載入指示，待站台資料就緒後再進入畫面
- 回到首頁時會清除「上次站台」紀錄

### 建置

- 版本名稱：`1.2.0-prerelease`
- Version Code：`4`
- 產出：`app-prerelease.apk`
- APK 大小：1,421,801 bytes
- SHA-256：`71905D32FD7018E649D4A578139C2413856F319A678F254410ECC414CDDB23F8`

### 測試

- 共 20 項 JVM 單元測試通過
- Android Lint 通過

## [1.1.0-prerelease] - 2026-10-21

### 新增

- 站台瀏覽頁字型縮放
  - 標題列加入縮小／放大兩個圖示按鈕
  - 支援 50%～200%，每次調整 10%
  - 倍率全域保存，重新開啟 App 後仍會保留

### 變更

- 首頁站台卡片改用較淡的主題色底色，跟隨主題設定與深色模式
- 新增站台按鈕從右下角移至右上角工具列
- 更新首頁空清單提示與相關文件

### 建置

- 版本名稱：`1.1.0-prerelease`
- Version Code：`2`
- 產出：`app-prerelease.apk`

### 測試

- 共 18 項 JVM 單元測試通過
- Android Lint 通過

## [1.0.0-prerelease] - 2026-10-18

### 新增

- 站台清單：新增、編輯、刪除站台
- 站台名稱與網址搜尋
- 長按拖曳排序並保存順序
- App 內 WebView 瀏覽
  - 精簡站台標題列與返回箭頭
  - 載入進度、錯誤提示與重試
  - 外部 scheme 交由系統處理
  - 檔案下載
- JSON 匯出與匯入
  - 使用系統 SAF，不需要儲存權限
  - 匯入前確認取代現有資料
- 設定頁
  - 顯示模式：系統／淺色／深色（預設系統）
  - 6 種預設主題色
  - Hue 彩色滑桿與即時色彩預覽
  - 系統狀態列與導覽列圖示跟隨主題
- 自訂 Adaptive App 圖示
  - 地球儀圖案、粗線條卡通風格
  - 前景、背景與 monochrome 圖層
  - 核心圖案 56dp（66dp 安全區的 85%）

### 建置

- 新增 `prerelease` Build Type
- `prerelease` 啟用 R8 壓縮與資源縮減
- `prerelease` 使用 Debug 金鑰簽署
- 版本名稱：`1.0.0-prerelease`
- 產出：`app-prerelease.apk`

### 測試

- URL 正規化測試
- JSON 備份編解碼測試
- 主題色彩對比度測試
- 共 18 項 JVM 單元測試通過
- Android Lint 通過

### 已知限制

- Pre-release 版本使用 Debug 金鑰簽署，非正式上線版本
- 尚未在實機或模擬器上完成視覺與瀏覽行為驗證
- 部分網站可能因登入、憑證、X-Frame-Options 或 WebView 相容性而無法正常顯示
- 允許 `http://` 站台，使用時請注意連線安全

[1.4.0]: https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.4.0-prerelease
[1.3.0]: https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.3.0-prerelease
[1.2.0-prerelease]: https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.2.0-prerelease
[1.1.0-prerelease]: https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.1.0-prerelease
[1.0.0-prerelease]: https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.0.0-prerelease
