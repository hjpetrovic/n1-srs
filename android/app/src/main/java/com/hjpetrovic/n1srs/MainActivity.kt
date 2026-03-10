package com.hjpetrovic.n1srs

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // The live GitHub Pages URL — always up to date, Google login + Firebase sync works here.
    // After the first online load the service worker caches the app, so subsequent launches
    // work offline automatically within the same HTTPS origin.
    private val appUrl = "https://hjpetrovic.github.io/n1-srs/"

    // Mimic Chrome Mobile so Google OAuth does not block the WebView user agent.
    private val chromeUA =
        "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true       // localStorage / sessionStorage
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT  // respects service worker cache
                userAgentString = chromeUA
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(false)
                // Allow popups so Firebase signInWithPopup can open its auth window
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
            }

            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    // Only show the offline page when the top-level page fails to load.
                    // Sub-resource errors (e.g. a single image) are ignored so the SW
                    // cache can still serve the rest of the app.
                    if (request.isForMainFrame) {
                        view.loadDataWithBaseURL(
                            null,
                            offlinePage(),
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                }
            }
        }

        setContentView(webView)
        webView.loadUrl(appUrl)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    private fun offlinePage(): String = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              background: #0f172a; color: #94a3b8; font-family: sans-serif;
              display: flex; flex-direction: column; align-items: center;
              justify-content: center; min-height: 100vh; padding: 32px; text-align: center;
            }
            h1 { color: #e2e8f0; font-size: 1.8rem; margin-bottom: 16px; }
            p  { line-height: 1.7; margin-bottom: 8px; }
            .hint { font-size: 0.82rem; color: #475569; margin-top: 24px; }
          </style>
        </head>
        <body>
          <h1>N1 SRS</h1>
          <p>No internet connection.</p>
          <p>Connect to Wi-Fi or mobile data to load the app.</p>
          <p class="hint">
            Once loaded online for the first time,<br>
            the app will work offline automatically.
          </p>
        </body>
        </html>
    """.trimIndent()
}
