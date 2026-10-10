package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat

/**
 * Ensures all network traffic from the app process is routed through an active VPN tunnel.
 *
 * On modern Android versions (Android 14+ / Pixel), dual-stack Wi-Fi connections can leak
 * sockets or DNS queries over the physical Wi-Fi interface if the process is not explicitly
 * bound to the VPN network interface.
 */
object VpnNetworkBinder {

    @Volatile
    private var boundNetwork: Network? = null

    fun initialize(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return

        // 1. Initial binding check for already connected VPN
        updateBinding(connectivityManager)

        // 2. Listen for VPN network availability
        try {
            val vpnRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .build()

            connectivityManager.registerNetworkCallback(
                vpnRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        logcat(LogPriority.INFO) { "VPN network became available: $network" }
                        updateBinding(connectivityManager)
                    }

                    override fun onLost(network: Network) {
                        logcat(LogPriority.INFO) { "VPN network lost: $network" }
                        updateBinding(connectivityManager)
                    }

                    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                        updateBinding(connectivityManager)
                    }
                },
            )
        } catch (e: Exception) {
            logcat(LogPriority.WARN, e) { "Failed to register VPN network callback" }
        }

        // 3. Listen to default network changes (e.g. WiFi <-> Cellular transitions)
        try {
            connectivityManager.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        updateBinding(connectivityManager)
                    }

                    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                        updateBinding(connectivityManager)
                    }

                    override fun onLost(network: Network) {
                        updateBinding(connectivityManager)
                    }
                },
            )
        } catch (e: Exception) {
            logcat(LogPriority.WARN, e) { "Failed to register default network callback" }
        }
    }

    private fun updateBinding(connectivityManager: ConnectivityManager) {
        try {
            // Find active VPN network
            val activeVpn = connectivityManager.allNetworks.firstOrNull { network ->
                val caps = connectivityManager.getNetworkCapabilities(network)
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } ?: connectivityManager.allNetworks.firstOrNull { network ->
                connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            }

            if (activeVpn != null) {
                if (boundNetwork != activeVpn) {
                    logcat(LogPriority.INFO) { "Binding app process to VPN network: $activeVpn" }
                    val success = connectivityManager.bindProcessToNetwork(activeVpn)
                    if (success) {
                        boundNetwork = activeVpn
                    } else {
                        logcat(LogPriority.WARN) { "bindProcessToNetwork returned false for $activeVpn" }
                    }
                }
            } else {
                if (boundNetwork != null) {
                    logcat(LogPriority.INFO) { "No active VPN network found, clearing process binding" }
                    connectivityManager.bindProcessToNetwork(null)
                    boundNetwork = null
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Error updating process network binding" }
        }
    }
}
