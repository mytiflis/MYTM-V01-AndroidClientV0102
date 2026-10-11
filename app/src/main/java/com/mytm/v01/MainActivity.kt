package com.mytm.v01

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * MYTM V01 Android tablet client.
 *
 * Client-only: no server, no Node.js, no SQLite, no local license logic.
 * It connects to the existing MYTM V01 Windows server over LAN/Wi-Fi and
 * renders the existing web UI (login.html -> ana.html) inside a WebView.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var setupView: View
    private lateinit var offlineOverlay: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var hostEdit: EditText
    private lateinit var portEdit: EditText

    private val prefs by lazy { getSharedPreferences("mytm_v01", Context.MODE_PRIVATE) }
    private val ui = Handler(Looper.getMainLooper())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var currentUrl: String? = null
    private var retryScheduled = false

    private lateinit var locationManager: LocationManager
    private var lastKnownLocation: Location? = null
    private var locationCallback: ((Location?) -> Unit)? = null
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastKnownLocation = location
            locationCallback?.let { it(location) }
            locationCallback = null
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: android.os.Bundle?) {}
    }

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val cb = fileCallback
            fileCallback = null
            cb?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            )
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            if (!allGranted) {
                toast(getString(R.string.permission_location_denied))
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineGranted = result[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = result[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineGranted || coarseGranted) {
                startLocationUpdates()
            } else {
                locationCallback?.let { it(null) }
                locationCallback = null
                toast(getString(R.string.permission_location_denied))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.webView)
        setupView = findViewById(R.id.setupView)
        offlineOverlay = findViewById(R.id.offlineOverlay)
        statusText = findViewById(R.id.statusText)
        hostEdit = findViewById(R.id.hostEdit)
        portEdit = findViewById(R.id.portEdit)

        hostEdit.setText(prefs.getString("host", ""))
        portEdit.setText(prefs.getInt("port", 3303).toString())

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupWeb()

        findViewById<Button>(R.id.findBtn).setOnClickListener { findServer() }
        findViewById<Button>(R.id.testBtn).setOnClickListener { testFromUi() }
        findViewById<Button>(R.id.connectBtn).setOnClickListener { connect() }
        findViewById<Button>(R.id.retryBtn).setOnClickListener {
            hideOffline()
            currentUrl?.let { web.reload() } ?: connect()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.visibility == View.VISIBLE && web.canGoBack()) {
                    web.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // ---------------------------------------------------------------- WebView

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWeb() {
        val s = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.mediaPlaybackRequiresUserGesture = false
        s.allowFileAccess = true
        s.allowContentAccess = true
        s.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        web.addJavascriptInterface(MyTMJavascriptInterface(this), "MyTMNative")

        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                hideOffline()
                view?.evaluateJavascript("if (window.MyTMNativeReady) MyTMNativeReady();", null)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) showOffline()
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback
                return try {
                    fileChooserLauncher.launch(fileChooserParams!!.createIntent())
                    true
                } catch (_: Exception) {
                    fileCallback = null
                    filePathCallback?.onReceiveValue(null)
                    false
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null) return
                val perms = ArrayList<String>()
                for (r in request.resources) {
                    when (r) {
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                            if (Build.VERSION.SDK_INT >= 23) perms.add(android.Manifest.permission.CAMERA)
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                            if (Build.VERSION.SDK_INT >= 23) perms.add(android.Manifest.permission.RECORD_AUDIO)
                    }
                }
                if (perms.isNotEmpty()) permissionLauncher.launch(perms.toTypedArray())
                request.grant(request.resources)
            }
        }

        web.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            startDownload(url, userAgent, contentDisposition, mimeType)
        }
    }

    inner class MyTMJavascriptInterface(private val context: Context) {

        @JavascriptInterface
        fun getCurrentLocation(): String {
            return try {
                val location = getLastKnownLocationSync()
                val json = JSONObject()
                if (location != null) {
                    json.put("latitude", location.latitude)
                    json.put("longitude", location.longitude)
                    json.put("accuracy", location.accuracy)
                    json.put("timestamp", location.time)
                    json.put("success", true)
                } else {
                    json.put("success", false)
                    json.put("error", "Location not available")
                }
                json.toString()
            } catch (e: Exception) {
                JSONObject().put("success", false).put("error", e.message).toString()
            }
        }

        @JavascriptInterface
        fun requestCurrentLocation(): String {
            return try {
                val json = JSONObject()
                requestLocationUpdate { loc ->
                }
                json.put("success", true)
                json.put("message", "Location request started. Call getCurrentLocation() again in a few seconds.")
                json.toString()
            } catch (e: Exception) {
                JSONObject().put("success", false).put("error", e.message).toString()
            }
        }

        @JavascriptInterface
        fun getDeviceIP(): String {
            return try {
                val json = JSONObject()
                val ip = Discovery.getLocalIP()
                if (ip != null) {
                    json.put("ip", ip)
                    json.put("success", true)
                } else {
                    json.put("success", false)
                    json.put("error", "Could not determine local IP")
                }
                json.toString()
            } catch (e: Exception) {
                JSONObject().put("success", false).put("error", e.message).toString()
            }
        }

        @JavascriptInterface
        fun getDeviceInfo(): String {
            return try {
                val json = JSONObject()
                json.put("deviceId", android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID))
                json.put("model", Build.MODEL)
                json.put("manufacturer", Build.MANUFACTURER)
                json.put("androidVersion", Build.VERSION.RELEASE)
                json.put("sdkInt", Build.VERSION.SDK_INT)
                json.put("appVersion", "1.0")
                json.put("success", true)
                json.toString()
            } catch (e: Exception) {
                JSONObject().put("success", false).put("error", e.message).toString()
            }
        }

        @JavascriptInterface
        fun sendLocationToServer(latitude: Double, longitude: Double, accuracy: Float): String {
            return try {
                val json = JSONObject()
                json.put("success", true)
                json.put("message", "Location data ready for server")
                json.toString()
            } catch (e: Exception) {
                JSONObject().put("success", false).put("error", e.message).toString()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val perms = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.permission_location_title)
                .setMessage(R.string.permission_location_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    locationPermissionLauncher.launch(perms)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    toast(getString(R.string.permission_location_denied))
                }
                .show()
        } else {
            locationPermissionLauncher.launch(perms)
        }
    }

    private fun startLocationUpdates() {
        try {
            val criteria = android.location.Criteria().apply {
                accuracy = android.location.Criteria.ACCURACY_FINE
                powerRequirement = android.location.Criteria.POWER_LOW
            }
            val provider = locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider, 0, 0f, locationListener, Looper.getMainLooper())
                lastKnownLocation = locationManager.getLastKnownLocation(provider)
            }
        } catch (e: Exception) {
            Log.w("MYTM", "Location updates failed", e)
        }
    }

    private fun requestLocationUpdate(callback: (Location?) -> Unit) {
        locationCallback = callback
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }
        startLocationUpdates()
        if (lastKnownLocation != null && (System.currentTimeMillis() - lastKnownLocation!!.time < 30000)) {
            callback(lastKnownLocation)
            locationCallback = null
        }
    }

    private fun getLastKnownLocationSync(): Location? {
        if (!hasLocationPermission()) return null
        try {
            val criteria = android.location.Criteria().apply {
                accuracy = android.location.Criteria.ACCURACY_FINE
            }
            val provider = locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
            return locationManager.getLastKnownLocation(provider)
        } catch (_: Exception) {
            return null
        }
    }

    // ------------------------------------------------------------ Connection

    private fun connect() {
        val host = hostEdit.text.toString().trim()
        val port = portEdit.text.toString().trim().toIntOrNull() ?: 3303
        if (host.isEmpty()) {
            toast(getString(R.string.status_need_host))
            return
        }
        prefs.edit().putString("host", host).putInt("port", port).apply()
        currentUrl = "http://$host:$port"
        offlineOverlay.visibility = View.GONE
        setupView.visibility = View.GONE
        web.visibility = View.VISIBLE
        web.loadUrl(currentUrl!!)
    }

    private fun testFromUi() {
        val host = hostEdit.text.toString().trim()
        val port = portEdit.text.toString().trim().toIntOrNull() ?: 3303
        if (host.isEmpty()) {
            toast(getString(R.string.status_need_host))
            return
        }
        statusText.setText(R.string.status_testing)
        background {
            val ok = testConnection(host, port)
            ui.post {
                statusText.text =
                    if (ok) getString(R.string.status_ok, "http://$host:$port")
                    else getString(R.string.status_fail, "http://$host:$port")
            }
        }
    }

    private fun findServer() {
        statusText.setText(R.string.status_searching)
        background {
            val found = Discovery.find(this, 6000)
            ui.post {
                if (found != null) {
                    val parts = found.removePrefix("http://").split(":")
                    hostEdit.setText(parts.getOrNull(0) ?: "")
                    portEdit.setText(parts.getOrNull(1) ?: "3303")
                    statusText.text = getString(R.string.status_ok, found)
                } else {
                    statusText.text =
                        getString(R.string.status_fail, "UDP 33031 / 239.255.42.42")
                }
            }
        }
    }

    private fun testConnection(host: String, port: Int): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL("http://$host:$port/demo").openConnection() as HttpURLConnection)
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            code in 100..499
        } catch (_: Exception) {
            false
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    // --------------------------------------------------------------- Offline

    private fun showOffline() {
        offlineOverlay.visibility = View.VISIBLE
        if (!retryScheduled) {
            retryScheduled = true
            ui.postDelayed(retryRunnable, 5000)
        }
    }

    private fun hideOffline() {
        offlineOverlay.visibility = View.GONE
        retryScheduled = false
        ui.removeCallbacks(retryRunnable)
    }

    private val retryRunnable = object : Runnable {
        override fun run() {
            val url = currentUrl ?: return
            val host = hostEdit.text.toString().trim()
            val port = portEdit.text.toString().trim().toIntOrNull() ?: 3303
            background {
                val ok = testConnection(host, port)
                ui.post {
                    if (ok) {
                        web.loadUrl(url)
                        hideOffline()
                    } else {
                        ui.postDelayed(this, 5000)
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------- Utils

    private fun background(block: () -> Unit) {
        executor.execute(block)
    }

    private fun toast(msg: String) {
        ui.post { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroy() {
        ui.removeCallbacks(retryRunnable)
        try {
            locationManager.removeUpdates(locationListener)
        } catch (_: Exception) {}
        executor.shutdown()
        super.onDestroy()
    }
}
