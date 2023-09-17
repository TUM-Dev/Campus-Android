package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class NavigaTUMActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val location = intent.getStringExtra("location")
        val encoded_location = java.net.URLEncoder.encode(location, "utf-8")
        val url = "https://nav.tum.de/search?q=$encoded_location"
        val webview = WebView(this)
        setContentView(webview)
        webview.settings.javaScriptEnabled = true
        webview.loadUrl(url)
    }
}
