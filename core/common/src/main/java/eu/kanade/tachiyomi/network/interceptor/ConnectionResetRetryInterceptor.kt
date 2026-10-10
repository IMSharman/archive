package eu.kanade.tachiyomi.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketException

class ConnectionResetRetryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: Exception) {
            val isReset = e is SocketException ||
                (e is IOException && e.message?.contains("reset", ignoreCase = true) == true)
            if (isReset) {
                try {
                    Thread.sleep(150)
                } catch (_: InterruptedException) {
                }
                chain.proceed(request)
            } else {
                throw e
            }
        }
    }
}
