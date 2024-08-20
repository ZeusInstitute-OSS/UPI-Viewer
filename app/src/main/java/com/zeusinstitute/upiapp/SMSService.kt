import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlinx.coroutines.*

class SMSService : Service(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val messageQueue = LinkedBlockingQueue<String>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

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
                    val timestamp = smsMessage.timestampMillis
                    processMessage(messageBody, timestamp)
                }
            }
        }
    }
    // Add a BroadcastReceiver to receive the stop signal
    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == STOP_SERVICE) {
                stopSelf() // Stop the service
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        startMessageProcessing()
        // Register the stop receiver
        val filter = IntentFilter(STOP_SERVICE)
        registerReceiver(stopReceiver, filter)
    }

    private fun processMessage(message: String, timestamp: Long) {
        val sharedPref = getSharedPreferences("com.zeusinstitute.upiapp.preferences", Context.MODE_PRIVATE)
        val smsEnabled = sharedPref.getBoolean("sms_enabled", true)

        if (!smsEnabled) {
            return  // Don't process messages if SMS notifications are disabled
        }

        if (message.contains("credited") && !message.contains("debited")) {
            val regex = "Rs\\s*(\\d+(\\.\\d{2})?)".toRegex()
            val matchResult = regex.find(message)
            matchResult?.let {
                val amount = it.groupValues[1]
                val timeString = Date(timestamp).toString()
                messageQueue.offer("Received Rupees $amount at $timeString")
            }
        }
    }

    private fun startMessageProcessing() {
        scope.launch {
            while (isActive) {
                val message = messageQueue.poll()
                if (message != null) {
                    announceMessage(message)
                }
                delay(1000) // Check every second
            }
        }
    }

    private fun announceMessage(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "UPI_CREDIT")
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null)
        }
        Log.d("SMSService", "Announcing: $message") // Log the spoken message
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