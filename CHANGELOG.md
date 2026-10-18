# 變更紀錄

本專案遵循簡化的版本紀錄格式。日期為西元年-月-日。

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

[1.0.0-prerelease]: https://github.com/mark216tw/android-my-sites-01/releases/tag/v1.0.0-prerelease
