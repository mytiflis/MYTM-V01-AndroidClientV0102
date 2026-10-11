package com.mytm.v01

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.Inet4Address
import java.util.Enumeration

/**
 * LAN discovery for the MYTM V01 server.
 *
 * Mirrors the existing desktop client approach:
 *   - UDP port 33031
 *   - Multicast group 239.255.42.42
 *   - Discovery message "MYTM_DISCOVER|1"
 *   - Server replies with a pipe separated beacon:
 *     MYTM_DISCOVERY|1|<serverId>|<hostname>|<port>|<host>|<ts>|api,web
 */
object Discovery {

    private const val PORT = 33031
    private const val GROUP = "239.255.42.42"
    private const val MESSAGE = "MYTM_DISCOVER|1"

    /**
     * Broadcasts a discovery probe and waits for a valid beacon.
     * @return "http://host:port" of a reachable server, or null.
     */
    @SuppressLint("MissingPermission")
    fun find(context: Context, timeoutMs: Long = 6000): String? {
        var lock: WifiManager.MulticastLock? = null
        var socket: MulticastSocket? = null
        try {
            // Multicast receive needs an explicit lock on Android Wi-Fi.
            try {
                val wm = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as? WifiManager
                lock = wm?.createMulticastLock("mytm_discovery")?.apply {
                    setReferenceCounted(false)
                    acquire()
                }
            } catch (_: Exception) {
            }

            socket = MulticastSocket(PORT)
            socket.reuseAddress = true
            socket.soTimeout = 1000

            val group = InetAddress.getByName(GROUP)
            joinGroup(socket, group)

            val payload = MESSAGE.toByteArray(Charsets.UTF_8)
            val targets = ArrayList<InetAddress>()
            targets.add(group)
            try {
                targets.add(InetAddress.getByName("127.0.0.1"))
            } catch (_: Exception) {
            }
            broadcastAddress()?.let { targets.add(it) }

            for (t in targets) {
                try {
                    socket.send(DatagramPacket(payload, payload.size, t, PORT))
                } catch (_: Exception) {
                }
            }

            val deadline = System.currentTimeMillis() + timeoutMs
            val buf = ByteArray(2048)
            while (System.currentTimeMillis() < deadline) {
                val packet = DatagramPacket(buf, buf.size)
                try {
                    socket.receive(packet)
                } catch (_: SocketTimeoutException) {
                    continue
                } catch (_: Exception) {
                    continue
                }
                val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                parse(text)?.let { return it }
            }
        } catch (_: Exception) {
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
            try {
                lock?.release()
            } catch (_: Exception) {
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun joinGroup(socket: MulticastSocket, group: InetAddress) {
        try {
            socket.joinGroup(group)
            return
        } catch (_: Exception) {
        }
        try {
            val nif = java.util.Collections
                .list(NetworkInterface.getNetworkInterfaces())
                .firstOrNull { it.isUp && !it.isLoopback }
            if (nif != null) {
                socket.joinGroup(InetSocketAddress(group, PORT), nif)
            }
        } catch (_: Exception) {
        }
    }

    private fun broadcastAddress(): InetAddress? {
        return try {
            val nif = java.util.Collections
                .list(NetworkInterface.getNetworkInterfaces())
                .firstOrNull { it.isUp && !it.isLoopback }
            val addr = nif?.interfaceAddresses?.firstOrNull { it.broadcast != null }
            addr?.broadcast
        } catch (_: Exception) {
            null
        }
    }

    /** Parses a MYTM beacon line and returns "http://host:port" if valid. */
    fun parse(line: String): String? {
        val p = line.trim().split("|")
        if (p.size < 8) return null
        if (p[0] != "MYTM_DISCOVERY") return null
        if (p[1] != "1") return null
        val port = p[4].toIntOrNull() ?: return null
        if (port !in 1..65535) return null
        val host = p[5].trim()
        if (host.isEmpty()) return null
        return "http://$host:$port"
    }

    /**
     * Returns the device's local LAN IP address (e.g., 192.168.x.x).
     * Iterates network interfaces and returns the first non-loopback IPv4 address.
     */
    fun getLocalIP(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (!networkInterface.isUp || networkInterface.isLoopback) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        return address.hostAddress
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }
}
