package eu.kanade.presentation.webview

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class WebViewPopupBlockerTest {

    @Test
    fun `allows normal same-site and non-ad URLs`() {
        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://example.com/manga/chapter-1",
            currentPageUrl = "https://example.com/manga",
        ) shouldBe false

        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://cdn.example.com/assets/page-1.jpg",
            currentPageUrl = "https://example.com/manga",
        ) shouldBe false
    }

    @Test
    fun `blocks known ad and popunder domains including subdomains`() {
        WebViewPopupBlocker.isAdOrPopupUrl("https://popads.net/pop.js") shouldBe true
        WebViewPopupBlocker.isAdOrPopupUrl("https://cdn.highperformanceformat.com/script.js") shouldBe true
        WebViewPopupBlocker.isAdOrPopupUrl("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") shouldBe
            true
        WebViewPopupBlocker.isAdOrPopupUrl("https://sub.effectivegatecpm.com/direct") shouldBe true
    }

    @Test
    fun `never blocks allowlisted challenge and captcha domains`() {
        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://challenges.cloudflare.com/turnstile/v0/api.js?onload=popunder",
            currentPageUrl = "https://example.com/",
        ) shouldBe false
        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://newassets.hcaptcha.com/captcha/v1/pop.js",
            currentPageUrl = "https://example.com/",
        ) shouldBe false
        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://www.recaptcha.net/recaptcha/api.js",
            currentPageUrl = "https://example.com/",
        ) shouldBe false
        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://www.gstatic.com/recaptcha/releases/widget.js",
            currentPageUrl = "https://example.com/",
        ) shouldBe false
    }

    @Test
    fun `blocks third-party popunder script paths but allows same-site paths`() {
        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://tracker-other.net/static/popunder.js",
            currentPageUrl = "https://reader-site.com/chapter/1",
        ) shouldBe true

        WebViewPopupBlocker.isAdOrPopupUrl(
            url = "https://cdn.reader-site.co.uk/scripts/pop.js",
            currentPageUrl = "https://www.reader-site.co.uk/chapter/1",
        ) shouldBe false
    }

    @Test
    fun `handles multi-part TLDs accurately in isSameBaseDomain`() {
        WebViewPopupBlocker.isSameBaseDomain(
            "https://www.example.co.uk/read",
            "https://img.example.co.uk/1.png",
        ) shouldBe true

        WebViewPopupBlocker.isSameBaseDomain(
            "https://example.co.uk/read",
            "https://ads-example.co.uk/pop",
        ) shouldBe false

        WebViewPopupBlocker.isSameBaseDomain(
            "https://site.com.au/page",
            "https://cdn.site.com.au/asset",
        ) shouldBe true

        WebViewPopupBlocker.isSameBaseDomain(
            "https://foo.com.au/page",
            "https://bar.com.au/page",
        ) shouldBe false
    }

    @Test
    fun `returns false for null blank or invalid URLs`() {
        WebViewPopupBlocker.isAdOrPopupUrl(null as String?) shouldBe false
        WebViewPopupBlocker.isAdOrPopupUrl("") shouldBe false
        WebViewPopupBlocker.isAdOrPopupUrl("not a url") shouldBe false
        WebViewPopupBlocker.isSameBaseDomain(null, "https://example.com") shouldBe false
        WebViewPopupBlocker.isSameBaseDomain("https://example.com", "") shouldBe false
    }
}
