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
import androidx.room.*
import java.text.SimpleDateFormat


class SMSService : Service(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val messageQueue = LinkedBlockingQueue<String>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val notificationId = 1
    private val notificationChannelId = "sms_service_channel"
    private lateinit var notificationManager: NotificationManager

    private lateinit var db: AppDatabase
    lateinit var transactionDao: TransactionDao

    companion object {
        const val STOP_SERVICE = "STOP_SERVICE"
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    // For KitKat and above, use the bundled SMS API
                    val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    smsMessages?.forEach { smsMessage ->
                        val messageBody = smsMessage.messageBody
                        processMessage(messageBody)
                    }
                } else {
                    // For older devices, parse SMS messages manually
                    val bundle = intent.extras
                    if (bundle != null) {
                        val pdus = bundle["pdus"] as Array<*>?
                        pdus?.forEach { pdu ->
                            val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                            val messageBody = smsMessage.messageBody
                            processMessage(messageBody)
                        }
                    }
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

        this.db = (applicationContext as UPIAPP).database
        transactionDao = db.transactionDao() // Initialize transactionDao

        val smsIntentFilter = IntentFilter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            smsIntentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        } else {
            smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        }
        registerReceiver(smsReceiver, smsIntentFilter)

        startMessageProcessing()

        val filter = IntentFilter(STOP_SERVICE)

        if (Build.VERSION.SDK_INT >= 33 ){
            registerReceiver(stopReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopReceiver, filter)
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Start the service in the foreground (for Android 8.0 and above)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel() // Create channel before starting foreground
            startForeground(notificationId, createNotification())
        } else {
            // For older Android versions, show a regular notification
            showNotification("UPI Speaker Mode is running")
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

        val regex = "Rs\\.?\\s*(\\d+(\\.\\d{2})?)".toRegex()
        val matchResult = regex.find(message)

        var extractedName: String? = null
        val nameRegex = "(?i)(?:from|From|FROM)\\s+(.*?)(?:\\.|thru|through)".toRegex()
        val nameMatchResult = nameRegex.find(message)
        extractedName = nameMatchResult?.groupValues?.getOrNull(1)?.trim()

        matchResult?.let { result ->
            val amount = result.groupValues[1].toDoubleOrNull()
            if (amount != null) {
                val type = when {
                    message.contains("credited") && !message.contains("debited") -> "Credit"
                    message.contains("debited") -> "Debit"
                    else -> {
                        Log.d("SMSService", "Message does not match criteria for announcement")
                        return
                    }
                }

                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val transaction = PayTransaction(amount = amount, type = type, date = date, name = extractedName ?: "") // Use empty string if name is null

                scope.launch {
                    transactionDao.insert(transaction)
                    Log.d("SMSService", "Inserted transaction into database: $transaction")
                }

                val announcementMessage = "${if (type == "Credit") "Received" else "Sent"} Rupees $amount"
                Log.d("SMSService", "Queueing message: $announcementMessage")
                messageQueue.offer(announcementMessage)
            } else {
                Log.d("SMSService", "Invalid amount format in the message")
            }
        } ?: Log.d("SMSService", "No amount found in the message")
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
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("UPI Credit")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify(notificationId, notificationBuilder.build())
        } else {
            @Suppress("DEPRECATION")
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("UPI Speaker Mode")
            .setContentText("Service is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Check SDK version
            val channel = NotificationChannel(
                notificationChannelId,
                "SMS Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
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