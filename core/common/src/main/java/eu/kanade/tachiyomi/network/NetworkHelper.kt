package eu.kanade.tachiyomi.network

import android.content.Context
import eu.kanade.tachiyomi.network.interceptor.CloudflareInterceptor
import eu.kanade.tachiyomi.network.interceptor.ConnectionResetRetryInterceptor
import eu.kanade.tachiyomi.network.interceptor.UncaughtExceptionInterceptor
import eu.kanade.tachiyomi.network.interceptor.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkHelper(
    private val context: Context,
    private val preferences: NetworkPreferences,
) {

    val cookieJar = AndroidCookieJar()

    private val clientBuilder: OkHttpClient.Builder = run {
        val builder = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(2, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .fastFallback(true)
            .cache(
                Cache(
                    directory = File(context.cacheDir, "network_cache"),
                    maxSize = 5L * 1024 * 1024, // 5 MiB
                ),
            )
            .addInterceptor(UncaughtExceptionInterceptor())
            .addInterceptor(ConnectionResetRetryInterceptor())
            .addInterceptor(UserAgentInterceptor(::defaultUserAgentProvider))

        if (preferences.verboseLogging().get()) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addNetworkInterceptor(httpLoggingInterceptor)
        }

        val googleDns = builder.buildDoh(
            "https://dns.google/dns-query",
            listOf("8.8.4.4", "8.8.8.8"),
        )
        val cloudflareDns = builder.buildDoh(
            "https://cloudflare-dns.com/dns-query",
            listOf(
                "162.159.36.1",
                "162.159.46.1",
                "1.1.1.1",
                "1.0.0.1",
                "162.159.132.53",
            ),
        )

        val selectedDns: Dns = when (preferences.dohProvider().get()) {
            PREF_DOH_CLOUDFLARE -> ResilientDns(cloudflareDns, googleDns)
            PREF_DOH_GOOGLE -> ResilientDns(googleDns, cloudflareDns)
            PREF_DOH_ADGUARD -> ResilientDns(
                builder.buildDoh(
                    "https://dns-unfiltered.adguard.com/dns-query",
                    listOf("94.140.14.140", "94.140.14.141"),
                ),
                googleDns,
            )
            PREF_DOH_QUAD9 -> ResilientDns(
                builder.buildDoh(
                    "https://dns.quad9.net/dns-query",
                    listOf("9.9.9.9", "149.112.112.112"),
                ),
                googleDns,
            )
            PREF_DOH_ALIDNS -> ResilientDns(
                builder.buildDoh(
                    "https://dns.alidns.com/dns-query",
                    listOf("223.5.5.5", "223.6.6.6"),
                ),
                googleDns,
            )
            PREF_DOH_DNSPOD -> ResilientDns(
                builder.buildDoh(
                    "https://doh.pub/dns-query",
                    listOf("1.12.12.12", "120.53.53.53"),
                ),
                googleDns,
            )
            PREF_DOH_360 -> ResilientDns(
                builder.buildDoh(
                    "https://doh.360.cn/dns-query",
                    listOf("101.226.4.6", "218.30.118.6"),
                ),
                googleDns,
            )
            PREF_DOH_QUAD101 -> ResilientDns(
                builder.buildDoh(
                    "https://dns.twnic.tw/dns-query",
                    listOf("101.101.101.101", "101.102.103.104"),
                ),
                googleDns,
            )
            PREF_DOH_MULLVAD -> ResilientDns(
                builder.buildDoh(
                    "https://dns.mullvad.net/dns-query",
                    listOf("194.242.2.2"),
                ),
                googleDns,
            )
            PREF_DOH_CONTROLD -> ResilientDns(
                builder.buildDoh(
                    "https://freedns.controld.com/p0",
                    listOf("76.76.2.0", "76.76.10.0"),
                ),
                googleDns,
            )
            PREF_DOH_NJALLA -> ResilientDns(
                builder.buildDoh(
                    "https://dns.njalla.no/dns-query",
                    listOf("95.215.19.19"),
                ),
                googleDns,
            )
            PREF_DOH_SHECAN -> ResilientDns(
                builder.buildDoh(
                    "https://free.shecan.ir/dns-query",
                    listOf("178.22.122.100", "185.51.200.2"),
                ),
                googleDns,
            )
            PREF_DOH_LIBREDNS -> ResilientDns(
                builder.buildDoh(
                    "https://doh.libredns.gr/dns-query",
                    listOf("116.202.176.26"),
                ),
                googleDns,
            )
            else -> ResilientDns(IPv4PreferredDns(okhttp3.Dns.SYSTEM), googleDns)
        }
        builder.dns(selectedDns)
    }

    val client = clientBuilder
        .addInterceptor(
            CloudflareInterceptor(context, cookieJar, ::defaultUserAgentProvider),
        )
        .build()

    /**
     * @deprecated Since extension-lib 1.5
     */
    @Deprecated("The regular client handles Cloudflare by default")
    @Suppress("UNUSED")
    val cloudflareClient: OkHttpClient = client

    fun defaultUserAgentProvider() = preferences.defaultUserAgent().get().trim()
}
