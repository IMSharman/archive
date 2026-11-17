package eu.kanade.presentation.webview

import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.core.net.toUri
import java.io.ByteArrayInputStream

internal object WebViewPopupBlocker {

    private val SAFE_ALLOWLIST_DOMAINS = setOf(
        "cloudflare.com",
        "challenges.cloudflare.com",
        "cloudflareinsights.com",
        "hcaptcha.com",
        "recaptcha.net",
        "gstatic.com",
        "googleapis.com",
    )

    private val BLOCKED_AD_DOMAINS = setOf(
        // PopAds / PopCash / PopUnder networks
        "popads.net",
        "popcash.net",
        "popmyads.com",
        "popunder.net",
        "popundertotal.com",
        "popup.net",
        // Adsterra & associated popunder/directlink domains
        "adsterra.com",
        "adsterratech.com",
        "highperformanceformat.com",
        "highrevenuenetwork.com",
        "profitablecpmrate.com",
        "profitableratecpm.com",
        "effectivegatecpm.com",
        "topcreativeformat.com",
        "schemefigent.com",
        "preferencenail.com",
        // PropellerAds / Monetag / OnClick
        "propellerads.com",
        "propellerclick.com",
        "monetag.com",
        "onclickads.net",
        "onclickalgo.com",
        "onclckmn.com",
        "onclckstr.com",
        "wpadmngr.com",
        "deloplen.com",
        "dolohen.com",
        "glizauvo.net",
        "upgulpinon.com",
        "alwingulla.com",
        "vaynessa.com",
        "pushwhy.com",
        "notix.io",
        // Adcash
        "adcash.com",
        "acscdn.com",
        "ufpcdn.com",
        "onclasrv.com",
        "steepto.com",
        // ExoClick / TrafficStars / JuicyAds / TrafficJunky
        "exoclick.com",
        "exosrv.com",
        "tsyndicate.com",
        "realsrv.com",
        "trafficstars.com",
        "juicyads.com",
        "jads.co",
        "trafficjunky.net",
        "trafficjunky.com",
        "eroadvertising.com",
        "twinrdsyn.com",
        "twinrdengine.com",
        // ClickAdu / HilltopAds / Galaksion / Mondiad / AdMaven / RichAds / Evadav
        "clickadu.com",
        "clickaine.com",
        "hilltopads.net",
        "hilltopads.com",
        "galaksion.com",
        "mondiad.com",
        "ad-maven.com",
        "admaven.com",
        "richads.com",
        "evadav.com",
        "daoad.com",
        "kadam.net",
        "adoperator.com",
        "rollerads.com",
        "adspyglass.com",
        "bidgear.com",
        "pubfuture.com",
        "adinplay.com",
        "vidoomy.com",
        "venatusmedia.com",
        "playwire.com",
        "nitropay.com",
        "snigelweb.com",
        "freestar.com",
        // General Ad / Tracker / Redirect Networks
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "googletagservices.com",
        "moatads.com",
        "adnxs.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "mgid.com",
        "revcontent.com",
        "zergnet.com",
        "bidvertiser.com",
        "a-ads.com",
        "anonymousads.com",
        "coinzilla.com",
        "cointraffic.io",
        "bitmedia.io",
        "adskeeper.com",
        "adskeeper.co.uk",
        "adnow.com",
        "adhitz.com",
        "adxpansion.com",
        "trafficfactory.biz",
        "plugrush.com",
        "zeroredirect.com",
        "voluum.com",
        "bemob.com",
        "redtrack.io",
        "peerclick.com",
    )

    private val THIRD_PARTY_POPUP_PATH_PATTERNS = listOf(
        "/popunder",
        "/pop.js",
        "/pop.min.js",
        "/adframe",
        "/pagead/",
        "/onclick",
        "/popads",
    )

    fun isAdOrPopupUrl(uri: Uri?, currentPageUrl: String? = null): Boolean {
        if (uri == null) return false
        val host = uri.host?.lowercase()?.trimEnd('.') ?: return false
        if (host.isEmpty()) return false

        // Never block Cloudflare challenges, captchas, or core Google static assets
        if (matchesDomainSet(host, SAFE_ALLOWLIST_DOMAINS)) {
            return false
        }

        // Check known ad/popup/redirect domains
        if (matchesDomainSet(host, BLOCKED_AD_DOMAINS)) {
            return true
        }

        // For third-party requests, also block common popunder script paths
        val pageHost = currentPageUrl?.runCatching { toUri().host }?.getOrNull()
        if (pageHost != null && !isSameBaseDomainHost(pageHost, host)) {
            val pathAndQuery = (uri.encodedPath.orEmpty() + "?" + uri.encodedQuery.orEmpty()).lowercase()
            if (THIRD_PARTY_POPUP_PATH_PATTERNS.any { it in pathAndQuery }) {
                return true
            }
        }

        return false
    }

    fun isSameBaseDomain(url1: String?, url2: String?): Boolean {
        if (url1.isNullOrBlank() || url2.isNullOrBlank()) return false
        val host1 = runCatching { url1.toUri().host }.getOrNull() ?: return false
        val host2 = runCatching { url2.toUri().host }.getOrNull() ?: return false
        return isSameBaseDomainHost(host1, host2)
    }

    private fun isSameBaseDomainHost(host1: String, host2: String): Boolean {
        val clean1 = host1.lowercase().trimEnd('.')
        val clean2 = host2.lowercase().trimEnd('.')
        if (clean1 == clean2) return true
        if (clean1.endsWith(".$clean2") || clean2.endsWith(".$clean1")) return true
        val base1 = extractBaseDomain(clean1)
        val base2 = extractBaseDomain(clean2)
        return base1.isNotEmpty() && base1 == base2
    }

    private fun extractBaseDomain(host: String): String {
        val parts = host.split('.')
        return if (parts.size >= 2) {
            "${parts[parts.size - 2]}.${parts[parts.size - 1]}"
        } else {
            host
        }
    }

    private fun matchesDomainSet(host: String, domainSet: Set<String>): Boolean {
        var current = host
        while (current.isNotEmpty()) {
            if (current in domainSet) return true
            val dotIndex = current.indexOf('.')
            if (dotIndex == -1) break
            current = current.substring(dotIndex + 1)
        }
        return false
    }

    fun emptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(ByteArray(0)),
        )
    }
}
