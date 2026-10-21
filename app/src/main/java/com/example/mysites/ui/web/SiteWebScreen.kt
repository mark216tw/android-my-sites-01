package com.example.mysites.ui.web

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.example.mysites.R
import com.example.mysites.data.MAX_WEB_TEXT_ZOOM
import com.example.mysites.data.MIN_WEB_TEXT_ZOOM
import com.example.mysites.data.Site
import com.example.mysites.data.WEB_TEXT_ZOOM_STEP

@Composable
fun SiteWebScreen(
    site: Site,
    textZoom: Int,
    onTextZoomChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(textZoom, webView) {
        webView?.settings?.textZoom = textZoom
    }

    BackHandler {
        val view = webView
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            onBack()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
                Text(
                    text = site.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(
                    onClick = { onTextZoomChange(textZoom - WEB_TEXT_ZOOM_STEP) },
                    enabled = textZoom > MIN_WEB_TEXT_ZOOM,
                ) {
                    Icon(
                        imageVector = Icons.Default.TextDecrease,
                        contentDescription = stringResource(R.string.zoom_out),
                    )
                }
                IconButton(
                    onClick = { onTextZoomChange(textZoom + WEB_TEXT_ZOOM_STEP) },
                    enabled = textZoom < MAX_WEB_TEXT_ZOOM,
                ) {
                    Icon(
                        imageVector = Icons.Default.TextIncrease,
                        contentDescription = stringResource(R.string.zoom_in),
                    )
                }
            }

            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
            ) {
                AndroidView(
                    factory = { ctx ->
                        createWebView(
                            context = ctx,
                            url = site.url,
                            initialTextZoom = textZoom,
                            onProgress = { newProgress ->
                                progress = newProgress
                                if (newProgress == 0) {
                                    errorMessage = null
                                }
                            },
                            onError = { errorMessage = it },
                            onCreated = { webView = it },
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { view ->
                        view.stopLoading()
                        view.destroy()
                    },
                )

                errorMessage?.let { message ->
                    ErrorOverlay(
                        detail = message,
                        onRetry = {
                            errorMessage = null
                            webView?.reload()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorOverlay(
    detail: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.load_error),
                style = MaterialTheme.typography.titleMedium,
            )
            if (detail.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    url: String,
    initialTextZoom: Int,
    onProgress: (Int) -> Unit,
    onError: (String) -> Unit,
    onCreated: (WebView) -> Unit,
): WebView {
    return WebView(context).apply webView@ {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            textZoom = initialTextZoom
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@webView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                onProgress(0)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                onProgress(100)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val target = request.url
                val scheme = target.scheme?.lowercase()
                if (scheme == "http" || scheme == "https") return false

                val intent = Intent(Intent.ACTION_VIEW, target)
                runCatching { context.startActivity(intent) }
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                if (request?.isForMainFrame == true) {
                    onError(error?.description?.toString().orEmpty())
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                handler?.cancel()
                onError(context.getString(R.string.ssl_error))
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgress(newProgress)
            }

            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message,
            ): Boolean {
                val popup = WebView(context)
                popup.settings.javaScriptEnabled = true
                popup.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        popupView: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        view.loadUrl(request.url.toString())
                        popupView.destroy()
                        return true
                    }
                }

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = popup
                resultMsg.sendToTarget()
                return true
            }
        }

        setDownloadListener(
            DownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
                runCatching {
                    val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
                    val request = DownloadManager.Request(downloadUrl.toUri()).apply {
                        setMimeType(mimeType)
                        addRequestHeader("User-Agent", userAgent)
                        setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                fileName,
                            )
                        } else {
                            setDestinationInExternalFilesDir(
                                context,
                                Environment.DIRECTORY_DOWNLOADS,
                                fileName,
                            )
                        }
                    }
                    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(request)
                }
            },
        )

        loadUrl(url)
        onCreated(this)
    }
}
