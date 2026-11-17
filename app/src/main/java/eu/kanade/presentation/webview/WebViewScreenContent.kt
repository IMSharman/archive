package eu.kanade.presentation.webview

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Message
import android.webkit.JsResult
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kevinnzou.web.AccompanistWebChromeClient
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.WarningBanner
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.util.system.getHtml
import eu.kanade.tachiyomi.util.system.setDefaultSettings
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun WebViewScreenContent(
    onNavigateUp: () -> Unit,
    initialTitle: String?,
    url: String,
    onShare: (String) -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onClearCookies: (String) -> Unit,
    headers: Map<String, String> = emptyMap(),
    onUrlChange: (String) -> Unit = {},
) {
    val state = rememberWebViewState(url = url, additionalHttpHeaders = headers)
    val navigator = rememberWebViewNavigator()
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    var currentUrl by remember { mutableStateOf(url) }
    var showCloudflareHelp by remember { mutableStateOf(false) }
    var blockPopupsAndAds by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val chromeClient = remember {
        object : AccompanistWebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?,
            ): Boolean {
                if (blockPopupsAndAds) {
                    if (isUserGesture && view != null) {
                        val hitTest = view.hitTestResult
                        val targetUrl = hitTest.extra
                        val isAnchor = hitTest.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                            hitTest.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        if (
                            isAnchor &&
                            !targetUrl.isNullOrBlank() &&
                            WebViewPopupBlocker.isSameBaseDomain(view.url, targetUrl) &&
                            !WebViewPopupBlocker.isAdOrPopupUrl(targetUrl.toUri(), view.url)
                        ) {
                            view.loadUrl(targetUrl, headers)
                        }
                    }
                    return false
                }
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            }

            override fun onJsBeforeUnload(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?,
            ): Boolean {
                if (blockPopupsAndAds) {
                    result?.confirm()
                    return true
                }
                return super.onJsBeforeUnload(view, url, message, result)
            }
        }
    }

    val webClient = remember {
        object : AccompanistWebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    currentUrl = it
                    onUrlChange(it)
                }
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                scope.launch {
                    val html = view.getHtml()
                    showCloudflareHelp = "window._cf_chl_opt" in html || "Ray ID is" in html
                }
            }

            override fun doUpdateVisitedHistory(
                view: WebView,
                url: String?,
                isReload: Boolean,
            ) {
                super.doUpdateVisitedHistory(view, url, isReload)
                url?.let {
                    currentUrl = it
                    onUrlChange(it)
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?,
            ): WebResourceResponse? {
                if (blockPopupsAndAds && request != null) {
                    val pageUrl = request.requestHeaders["Referer"] ?: currentUrl
                    if (WebViewPopupBlocker.isAdOrPopupUrl(request.url, pageUrl)) {
                        return WebViewPopupBlocker.emptyResponse()
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                request?.let {
                    val urlString = it.url.toString()
                    // Don't attempt to open blobs as webpages
                    if (urlString.startsWith("blob:http")) {
                        return false
                    }

                    // Block non-http/https schemes (intent://, market://, tel:, mailto:, etc.)
                    val scheme = it.url.scheme?.lowercase()
                    if (scheme != "http" && scheme != "https") {
                        return true
                    }

                    // Block navigations/redirects to known ad & popup domains
                    if (blockPopupsAndAds && WebViewPopupBlocker.isAdOrPopupUrl(it.url, currentUrl)) {
                        return true
                    }

                    // Continue with request, but with custom headers
                    if (it.isForMainFrame) {
                        view?.loadUrl(urlString, headers)
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    Scaffold(
        topBar = {
            Box {
                Column {
                    AppBar(
                        title = state.pageTitle ?: initialTitle,
                        subtitle = currentUrl,
                        navigateUp = onNavigateUp,
                        navigationIcon = Icons.Outlined.Close,
                        actions = {
                            AppBarActions(
                                persistentListOf(
                                    AppBar.Action(
                                        title = stringResource(MR.strings.action_webview_back),
                                        icon = Icons.Outlined.ArrowBack,
                                        onClick = {
                                            if (navigator.canGoBack) {
                                                navigator.navigateBack()
                                            }
                                        },
                                        enabled = navigator.canGoBack,
                                    ),
                                    AppBar.Action(
                                        title = stringResource(MR.strings.action_webview_forward),
                                        icon = Icons.Outlined.ArrowForward,
                                        onClick = {
                                            if (navigator.canGoForward) {
                                                navigator.navigateForward()
                                            }
                                        },
                                        enabled = navigator.canGoForward,
                                    ),
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_webview_refresh),
                                        onClick = { navigator.reload() },
                                    ),
                                    AppBar.OverflowAction(
                                        title = if (blockPopupsAndAds) {
                                            "Block popups & ads: ON"
                                        } else {
                                            "Block popups & ads: OFF"
                                        },
                                        onClick = {
                                            blockPopupsAndAds = !blockPopupsAndAds
                                            webViewRef?.settings?.apply {
                                                javaScriptCanOpenWindowsAutomatically = !blockPopupsAndAds
                                                setSupportMultipleWindows(blockPopupsAndAds)
                                            }
                                            navigator.reload()
                                        },
                                    ),
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_share),
                                        onClick = { onShare(currentUrl) },
                                    ),
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_open_in_browser),
                                        onClick = { onOpenInBrowser(currentUrl) },
                                    ),
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.pref_clear_cookies),
                                        onClick = { onClearCookies(currentUrl) },
                                    ),
                                ),
                            )
                        },
                    )

                    if (showCloudflareHelp) {
                        Surface(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            WarningBanner(
                                textRes = MR.strings.information_cloudflare_help,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        uriHandler.openUri(
                                            "https://aniyomi.org/docs/guides/troubleshooting/#cloudflare",
                                        )
                                    },
                            )
                        }
                    }
                }
                when (val loadingState = state.loadingState) {
                    is LoadingState.Initializing -> LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                    )
                    is LoadingState.Loading -> LinearProgressIndicator(
                        progress = { (loadingState as? LoadingState.Loading)?.progress ?: 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                    )
                    else -> {}
                }
            }
        },
    ) { contentPadding ->
        WebView(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            navigator = navigator,
            onCreated = { webView ->
                webViewRef = webView
                webView.setDefaultSettings()
                webView.settings.javaScriptCanOpenWindowsAutomatically = !blockPopupsAndAds
                webView.settings.setSupportMultipleWindows(blockPopupsAndAds)

                // Debug mode (chrome://inspect/#devices)
                if (BuildConfig.DEBUG &&
                    0 != webView.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
                ) {
                    WebView.setWebContentsDebuggingEnabled(true)
                }

                headers["user-agent"]?.let {
                    webView.settings.userAgentString = it
                }
            },
            client = webClient,
            chromeClient = chromeClient,
        )
    }
}
