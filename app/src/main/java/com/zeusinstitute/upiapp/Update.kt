package com.zeusinstitute.upiapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
private val buildRunId: Long = 0

class UpdateFragment : Fragment() {
    private val TAG = "UpdateFragment"
    private val releasesUrl = "https://api.github.com/repos/ZeusInstitute-OSS/UPI-Viewer/releases"

    private lateinit var checkUpdateButton: Button
    private lateinit var noUpdateText: TextView
    private lateinit var downloadProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update, container, false) // Use your fragment layout

        checkUpdateButton = view.findViewById(R.id.checkUpdateButton)
        noUpdateText = view.findViewById(R.id.noUpdateText)
        downloadProgressBar = view.findViewById(R.id.downloadProgressBar)


        checkUpdateButton.setOnClickListener {
                if (buildRunId == 0L) {
                    Toast.makeText(context, "Updating is disabled", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        checkForUpdate()
                    }
                }
            }

        return view
    }

    private suspend fun checkForUpdate() {
        withContext(Dispatchers.Main) {
            checkUpdateButton.isEnabled = false
            noUpdateText.visibility = View.GONE
            downloadProgressBar.visibility = View.GONE
        }

        val latestRelease = getLatestRelease()

        if (latestRelease != null) {
            if (latestRelease.runId > buildRunId) {
                downloadApk(latestRelease.apkUrl)
            } else if (latestRelease.runId == buildRunId) {
                withContext(Dispatchers.Main) {
                    noUpdateText.text = "Already on Latest Build"
                    noUpdateText.visibility = View.VISIBLE
                    checkUpdateButton.isEnabled = true
                }
            } else {
                // Handle the case where the latest release has a lower runId
                // (this might indicate an issue with your release numbering)
                withContext(Dispatchers.Main) {
                    noUpdateText.text = "Unexpected version found"
                    noUpdateText.visibility = View.VISIBLE
                    checkUpdateButton.isEnabled = true
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                noUpdateText.text = "No updates available"
                noUpdateText.visibility = View.VISIBLE
                checkUpdateButton.isEnabled = true
            }
        }
    }

    private suspend fun getLatestRelease(): Release? {
        val latestReleaseUrl = "https://api.github.com/repos/ZeusInstitute-OSS/UPI-Viewer/releases/latest"

        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(latestReleaseUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github+json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response) // Parse the JSON response

                    val runId = jsonObject.getLong("id")
                    val tagName = jsonObject.getString("tag_name")
                    val apkUrl = "https://github.com/ZeusInstitute-OSS/UPI-Viewer/releases/download/$tagName/app-debug-signed.apk"
                    Release(runId, apkUrl)
                } else {
                    Log.e(TAG, "Error getting latest release: HTTP $responseCode")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting latest release", e)
                null
            }
        }
    }

    private suspend fun downloadApk(url: String) {
        withContext(Dispatchers.Main) {
            downloadProgressBar.visibility = View.VISIBLE
            downloadProgressBar.progress = 0
        }

        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val contentLength = connection.contentLength
                    var bytesRead = 0
                    val buffer = ByteArray(8192)
                    var bytes: Int

                    connection.inputStream.use { input ->
                        while (input.read(buffer).also { bytes = it } >= 0) {
                            bytesRead += bytes
                            val progress = (bytesRead.toFloat() / contentLength * 100).toInt()
                            withContext(Dispatchers.Main) {
                                downloadProgressBar.progress = progress
                            }
                        }
                    }

                    // Here you would save the APK and initiate the installation
                    android.util.Log.i(TAG, "APK downloaded successfully")
                } else {
                    android.util.Log.e(TAG, "HTTP error: ${connection.responseCode}")
                    withContext(Dispatchers.Main) {
                        noUpdateText.text = "Update failed: HTTP ${connection.responseCode}"
                        noUpdateText.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error downloading APK", e)
                withContext(Dispatchers.Main) {
                    noUpdateText.text = "Update failed: ${e.message}"
                    noUpdateText.visibility = View.VISIBLE
                }
            }
        }

        withContext(Dispatchers.Main) {
            downloadProgressBar.visibility = View.GONE
            checkUpdateButton.isEnabled = true
        }
    }

    data class Release(val runId: Long, val apkUrl: String)
}