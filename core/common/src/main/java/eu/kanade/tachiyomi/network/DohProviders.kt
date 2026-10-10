package eu.kanade.tachiyomi.network

import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Based on https://github.com/square/okhttp/blob/ef5d0c83f7bbd3a0c0534e7ca23cbc4ee7550f3b/okhttp-dnsoverhttps/src/test/java/okhttp3/dnsoverhttps/DohProviders.java
 */

const val PREF_DOH_CLOUDFLARE = 1
const val PREF_DOH_GOOGLE = 2
const val PREF_DOH_ADGUARD = 3
const val PREF_DOH_QUAD9 = 4
const val PREF_DOH_ALIDNS = 5
const val PREF_DOH_DNSPOD = 6
const val PREF_DOH_360 = 7
const val PREF_DOH_QUAD101 = 8
const val PREF_DOH_MULLVAD = 9
const val PREF_DOH_CONTROLD = 10
const val PREF_DOH_NJALLA = 11
const val PREF_DOH_SHECAN = 12
const val PREF_DOH_LIBREDNS = 13

class IPv4PreferredDns(private val delegate: Dns) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = delegate.lookup(hostname)
        val (ipv4, _) = addresses.partition { it is Inet4Address }
        return if (ipv4.isNotEmpty()) ipv4 else addresses
    }
}

private fun OkHttpClient.Builder.buildDoh(
    url: String,
    bootstrapIps: List<String>,
): Dns {
    return IPv4PreferredDns(
        DnsOverHttps.Builder().client(build())
            .url(url.toHttpUrl())
            .bootstrapDnsHosts(bootstrapIps.map { InetAddress.getByName(it) })
            .includeIPv6(false)
            .build(),
    )
}

fun OkHttpClient.Builder.dohCloudflare() = dns(
    buildDoh(
        "https://cloudflare-dns.com/dns-query",
        listOf(
            "162.159.36.1",
            "162.159.46.1",
            "1.1.1.1",
            "1.0.0.1",
            "162.159.132.53",
        ),
    ),
)

fun OkHttpClient.Builder.dohGoogle() = dns(
    buildDoh(
        "https://dns.google/dns-query",
        listOf(
            "8.8.4.4",
            "8.8.8.8",
        ),
    ),
)

// AdGuard "Default" DNS works too but for the sake of making sure no site is blacklisted,
// we use "Unfiltered"
fun OkHttpClient.Builder.dohAdGuard() = dns(
    buildDoh(
        "https://dns-unfiltered.adguard.com/dns-query",
        listOf(
            "94.140.14.140",
            "94.140.14.141",
        ),
    ),
)

fun OkHttpClient.Builder.dohQuad9() = dns(
    buildDoh(
        "https://dns.quad9.net/dns-query",
        listOf(
            "9.9.9.9",
            "149.112.112.112",
        ),
    ),
)

fun OkHttpClient.Builder.dohAliDNS() = dns(
    buildDoh(
        "https://dns.alidns.com/dns-query",
        listOf(
            "223.5.5.5",
            "223.6.6.6",
        ),
    ),
)

fun OkHttpClient.Builder.dohDNSPod() = dns(
    buildDoh(
        "https://doh.pub/dns-query",
        listOf(
            "1.12.12.12",
            "120.53.53.53",
        ),
    ),
)

fun OkHttpClient.Builder.doh360() = dns(
    buildDoh(
        "https://doh.360.cn/dns-query",
        listOf(
            "101.226.4.6",
            "218.30.118.6",
            "123.125.81.6",
            "140.207.198.6",
            "180.163.249.75",
            "101.199.113.208",
            "36.99.170.86",
        ),
    ),
)

fun OkHttpClient.Builder.dohQuad101() = dns(
    buildDoh(
        "https://dns.twnic.tw/dns-query",
        listOf(
            "101.101.101.101",
        ),
    ),
)

/*
 * Mullvad DoH
 * without ad blocking option
 * Source: https://mullvad.net/en/help/dns-over-https-and-dns-over-tls
 */
fun OkHttpClient.Builder.dohMullvad() = dns(
    buildDoh(
        "https://dns.mullvad.net/dns-query",
        listOf(
            "194.242.2.2",
        ),
    ),
)

/*
 * Control D
 * unfiltered option
 * Source: https://controld.com/free-dns/?
 */
fun OkHttpClient.Builder.dohControlD() = dns(
    buildDoh(
        "https://freedns.controld.com/p0",
        listOf(
            "76.76.2.0",
            "76.76.10.0",
        ),
    ),
)

/*
 * Njalla
 * Non logging and uncensored
 */
fun OkHttpClient.Builder.dohNajalla() = dns(
    buildDoh(
        "https://dns.njal.la/dns-query",
        listOf(
            "95.215.19.53",
        ),
    ),
)

/**
 * Source: https://shecan.ir/
 */
fun OkHttpClient.Builder.dohShecan() = dns(
    buildDoh(
        "https://free.shecan.ir/dns-query",
        listOf(
            "178.22.122.100",
            "185.51.200.2",
        ),
    ),
)

/**
 * Source: https://libredns.gr/
 */
fun OkHttpClient.Builder.dohLibreDNS() = dns(
    buildDoh(
        "https://doh.libredns.gr/dns-query",
        listOf(
            "116.202.176.26",
        ),
    ),
)
