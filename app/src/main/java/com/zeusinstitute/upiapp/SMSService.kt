package com.zeusinstitute.upiapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.speech.tts.TextToSpeech
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlinx.coroutines.*

class SMSService : Service(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val messageQueue = LinkedBlockingQueue<String>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val notificationId = 1
    private val notificationChannelId = "sms_service_channel"

    companion object {
        const val STOP_SERVICE = "STOP_SERVICE"
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Telephony.Sms.Intents.getMessagesFromIntent(intent)
                } else {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val pdus = bundle["pdus"] as Array<*>?
                        pdus?.map { pdu ->
                            SmsMessage.createFromPdu(pdu as ByteArray)
                        }?.toTypedArray()
                    } else {
                        null
                    }
                }

                messages?.forEach { smsMessage ->
                    val messageBody = smsMessage.messageBody
                    processMessage(messageBody)
                }
            }
        }
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == STOP_SERVICE) {
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        startMessageProcessing()
        val filter = IntentFilter(STOP_SERVICE)
        registerReceiver(stopReceiver, filter)

        // Start the service in the foreground (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(notificationId, createNotification())
        }
    }

    private fun processMessage(message: String) {
        val sharedPref = getSharedPreferences("com.zeusinstitute.upiapp.preferences", Context.MODE_PRIVATE)
        val smsEnabled = sharedPref.getBoolean("sms_enabled", true)

        Log.d("SMSService", "Processing message: $message")
        Log.d("SMSService", "SMS Enabled: $smsEnabled")

        if (!smsEnabled) {
            Log.d("SMSService", "SMS notifications are disabled. Skipping processing.")
            return
        }

        if (message.contains("credited") && !message.contains("debited") && !message.contains("credited to")) {
            val regex = "Rs\\.?\\s*(\\d+(\\.\\d{2})?)".toRegex() // Match Rs or Rs.
            val matchResult = regex.find(message)
            matchResult?.let {
                val amount = it.groupValues[1]
                val announcementMessage = "Received Rupees $amount"
                Log.d("SMSService", "Queueing message: $announcementMessage")
                messageQueue.offer(announcementMessage)
            } ?: Log.d("SMSService", "No amount found in the message")
        } else if (message.contains("debited")) {
            val regex = "Rs\\.?\\s*(\\d+(\\.\\d{2})?)".toRegex() // Match Rs or Rs.
            val matchResult = regex.find(message)
            matchResult?.let {
                val amount = it.groupValues[1]
                val announcementMessage = "Sent Rupees $amount"
                Log.d("SMSService", "Queueing message: $announcementMessage")
                messageQueue.offer(announcementMessage)
            } ?: Log.d("SMSService", "No amount found in the message")
        } else if (message.contains("credited") && message.contains("credited to")) {
            val regex = "Rs\\.?\\s*(\\d+(\\.\\d{2})?)".toRegex() // Match Rs or Rs.
            val matchResult = regex.find(message)
            matchResult?.let {
                val amount = it.groupValues[1]
                val announcementMessage = "Received Rupees $amount"
                Log.d("SMSService", "Queueing message: $announcementMessage")
                messageQueue.offer(announcementMessage)
            } ?: Log.d("SMSService", "No amount found in the message")
        } else {
            Log.d("SMSService", "Message does not match criteria for announcement")
        }
    }

    private fun startMessageProcessing() {
        scope.launch {
            while (isActive) {
                val message = messageQueue.poll()
                if (message != null) {
                    announceMessage(message)
                    showNotification(message) // Show notification for each message
                }
                delay(1000) // Check every second
            }
        }
    }

    private fun announceMessage(message: String) {
        Log.d("SMSService", "Announcing message: $message")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "UPI_CREDIT")
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null)
        }
    }

    private fun showNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "SMS Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }


        val intent = Intent(this, MainActivity::class.java) // Replace with your main activity
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, notificationChannelId)
        } else {
            NotificationCompat.Builder(this) // Use the default constructor for older versions
        }

        val notification = notificationBuilder
            .setContentTitle("UPI Credit")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java) // Replace with your main activity
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, notificationChannelId)
        } else {
            NotificationCompat.Builder(this)
        }

        return notificationBuilder
            .setContentTitle("UPI Speaker Mode")
            .setContentText("Service is running")
            .setSmallIcon(R.mipmap.ic_launcher)  // Use App Icon For notifications
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
        unregisterReceiver(stopReceiver)
        job.cancel()
        tts?.shutdown()
    }
}