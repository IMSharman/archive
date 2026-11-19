package eu.kanade.tachiyomi.network

import eu.kanade.tachiyomi.network.interceptor.ConnectionResetRetryInterceptor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException

class ResilientDnsTest {

    private val ipv4Addr = InetAddress.getByAddress("example.com", byteArrayOf(93, 184.toByte(), 216.toByte(), 34))
    private val ipv6Addr = InetAddress.getByName("2606:2800:220:1:248:1893:25c8:1946")

    @Test
    fun `IPv4PreferredDns prefers IPv4 when both IPv4 and IPv6 are returned`() {
        val delegate = Dns { listOf(ipv6Addr, ipv4Addr) }
        val dns = IPv4PreferredDns(delegate)

        dns.lookup("example.com") shouldContainExactly listOf(ipv4Addr)
    }

    @Test
    fun `IPv4PreferredDns falls back to IPv6 when no IPv4 addresses exist`() {
        val delegate = Dns { listOf(ipv6Addr) }
        val dns = IPv4PreferredDns(delegate)

        dns.lookup("example.com") shouldContainExactly listOf(ipv6Addr)
    }

    @Test
    fun `ResilientDns returns primary addresses when primary succeeds`() {
        val fallbackAddr = InetAddress.getByAddress("example.com", byteArrayOf(1, 1, 1, 1))
        val dns = ResilientDns(
            primary = Dns { listOf(ipv4Addr) },
            fallback = Dns { listOf(fallbackAddr) },
            systemFallback = null,
        )

        dns.lookup("example.com") shouldContainExactly listOf(ipv4Addr)
    }

    @Test
    fun `ResilientDns falls back when primary throws or returns empty`() {
        val fallbackAddr = InetAddress.getByAddress("example.com", byteArrayOf(1, 1, 1, 1))
        val throwingPrimary = ResilientDns(
            primary = Dns { throw UnknownHostException("NXDOMAIN") },
            fallback = Dns { listOf(fallbackAddr) },
            systemFallback = null,
        )
        throwingPrimary.lookup("example.com") shouldContainExactly listOf(fallbackAddr)

        val emptyPrimary = ResilientDns(
            primary = Dns { emptyList() },
            fallback = Dns { listOf(fallbackAddr) },
            systemFallback = null,
        )
        emptyPrimary.lookup("example.com") shouldContainExactly listOf(fallbackAddr)
    }

    @Test
    fun `ResilientDns uses systemFallback when both primary and fallback fail`() {
        val systemAddr = InetAddress.getByAddress("example.com", byteArrayOf(8, 8, 8, 8))
        val dns = ResilientDns(
            primary = Dns { throw UnknownHostException("primary failed") },
            fallback = Dns { throw UnknownHostException("fallback failed") },
            systemFallback = Dns { listOf(systemAddr) },
        )

        dns.lookup("example.com") shouldContainExactly listOf(systemAddr)
    }

    @Test
    fun `ResilientDns rethrows when all DNS resolvers fail`() {
        val dns = ResilientDns(
            primary = Dns { throw UnknownHostException("primary failed") },
            fallback = Dns { throw UnknownHostException("fallback failed") },
            systemFallback = Dns { throw UnknownHostException("system failed") },
        )

        shouldThrow<UnknownHostException> {
            dns.lookup("example.com")
        }
    }

    @Test
    fun `ConnectionResetRetryInterceptor retries once on SocketException or reset IOException`() {
        val interceptor = ConnectionResetRetryInterceptor()
        val request = Request.Builder().url("https://example.com").build()
        val okResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("ok".toResponseBody())
            .build()

        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } throws SocketException("Connection reset") andThen okResponse

        val response = interceptor.intercept(chain)
        response.code shouldBe 200
        verify(exactly = 2) { chain.proceed(request) }
    }

    @Test
    fun `ConnectionResetRetryInterceptor does not retry non-reset IOException`() {
        val interceptor = ConnectionResetRetryInterceptor()
        val request = Request.Builder().url("https://example.com").build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } throws IOException("timed out")

        shouldThrow<IOException> {
            interceptor.intercept(chain)
        }
        verify(exactly = 1) { chain.proceed(request) }
    }
}
