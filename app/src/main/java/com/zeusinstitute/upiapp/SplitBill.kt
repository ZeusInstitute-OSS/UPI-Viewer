import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.zeusinstitute.upiapp.R

data class PayeeData(val amount: Int, val qrCode: Bitmap?, val payeeNumber: Int)

class SplitBillFragment : Fragment() {
    private lateinit var totalAmountEditText: EditText
    private lateinit var remainingAmountTextView: TextView
    private lateinit var payee1EditText: EditText
    private lateinit var payee2EditText: EditText
    private lateinit var payee3EditText: EditText
    private lateinit var addPayeeButton: Button
    private lateinit var clearButton: Button
    private lateinit var generateQrButton: Button
    private lateinit var qrCodeRecyclerView: RecyclerView
    private lateinit var paymentIdTextView: TextView
    private lateinit var dynamicPayeesContainer: LinearLayout

    private val qrCodeDataList: MutableList<PayeeData> = mutableListOf()
    private lateinit var qrCodeAdapter: QRCodeAdapter

    private var numPayeesCreated = 3
    private lateinit var savedData: String
    private lateinit var paymentMethod: String
    private lateinit var currency: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_split_bill, container, false)

        // Initialize view references
        totalAmountEditText = view.findViewById(R.id.totalAmountEditText)
        remainingAmountTextView = view.findViewById(R.id.remainingAmountTextView)
        payee1EditText = view.findViewById(R.id.payee1EditText)
        payee2EditText = view.findViewById(R.id.payee2EditText)
        payee3EditText = view.findViewById(R.id.payee3EditText)
        addPayeeButton = view.findViewById(R.id.addPayeeButton)
        clearButton = view.findViewById(R.id.clearButton)
        generateQrButton = view.findViewById(R.id.generateQrButton)
        qrCodeRecyclerView = view.findViewById(R.id.qrCodeRecyclerView)
        paymentIdTextView = view.findViewById(R.id.upiIdTextView)
        dynamicPayeesContainer = view.findViewById(R.id.dynamicPayeesContainer)

        // Get saved data from SharedPreferences
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        savedData = sharedPref.getString("saved_data", "") ?: ""
        paymentMethod = sharedPref.getString("payment_method", "") ?: ""
        currency = sharedPref.getString("currency", "") ?: ""

        paymentIdTextView.text = "$paymentMethod ID: $savedData"

        // Initialize RecyclerView
        qrCodeAdapter = QRCodeAdapter(qrCodeDataList)
        qrCodeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        qrCodeRecyclerView.adapter = qrCodeAdapter

        // Add TextWatcher to update remaining amount
        val textWatcher = createTextWatcher()
        totalAmountEditText.addTextChangedListener(textWatcher)
        payee1EditText.addTextChangedListener(textWatcher)
        payee2EditText.addTextChangedListener(textWatcher)
        payee3EditText.addTextChangedListener(textWatcher)

        // Add Payee Button
        addPayeeButton.setOnClickListener {
            numPayeesCreated++
            addNewPayeeEditText()
        }

        // Clear Button
        clearButton.setOnClickListener {
            totalAmountEditText.text.clear()
            payee1EditText.text.clear()
            payee2EditText.text.clear()
            payee3EditText.text.clear()
            dynamicPayeesContainer.removeAllViews()
            qrCodeDataList.clear()
            qrCodeAdapter.notifyDataSetChanged()
            numPayeesCreated = 3
            updateRemainingAmount()
        }

        // Generate QR Button
        generateQrButton.setOnClickListener {
            qrCodeDataList.clear()
            generateQRCodeForPayee(payee1EditText, 1)
            generateQRCodeForPayee(payee2EditText, 2)
            generateQRCodeForPayee(payee3EditText, 3)

            for (i in 0 until dynamicPayeesContainer.childCount) {
                val editText = dynamicPayeesContainer.getChildAt(i) as? EditText
                generateQRCodeForPayee(editText, i + 4)
            }

            qrCodeAdapter.notifyDataSetChanged()
        }

        return view
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateRemainingAmount() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun addNewPayeeEditText() {
        val editText = EditText(requireContext()).apply {
            hint = "Enter payee $numPayeesCreated amount"
            inputType = InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addTextChangedListener(createTextWatcher())
        }
        dynamicPayeesContainer.addView(editText)
    }

    private fun updateRemainingAmount() {
        val totalAmount = totalAmountEditText.text.toString().toIntOrNull() ?: 0
        var paidAmount = listOf(payee1EditText, payee2EditText, payee3EditText)
            .sumOf { it.text.toString().toIntOrNull() ?: 0 }

        for (i in 0 until dynamicPayeesContainer.childCount) {
            val editText = dynamicPayeesContainer.getChildAt(i) as? EditText
            paidAmount += editText?.text.toString().toIntOrNull() ?: 0
        }

        val remainingAmount = totalAmount - paidAmount
        remainingAmountTextView.text = "Remaining amount: ${getCurrencySymbol()}$remainingAmount"
    }

    private fun generateQRCodeForPayee(editText: EditText?, payeeNumber: Int) {
        val amount = editText?.text.toString().toIntOrNull() ?: 0
        generateQRCode(savedData, amount, payeeNumber)?.let { qrCodeDataList.add(it) }
    }

    private fun generateQRCode(paymentId: String, amount: Int, payeeNumber: Int): PayeeData? {
        if (amount <= 0) return null
        val barcodeEncoder = BarcodeEncoder()
        val qrString = when (paymentMethod) {
            "SGQR" -> "sgqr://pay?merchantId=$paymentId&$amount"
            "UPI" -> "upi://pay?pa=$paymentId&tn=undefined&am=$amount"
            else -> return null
        }
        return try {
            val qrCode = barcodeEncoder.encodeBitmap(qrString, BarcodeFormat.QR_CODE, 200, 200)
            PayeeData(amount, qrCode, payeeNumber)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun getCurrencySymbol(): String {
        return when (currency) {
            "₹ (INR)" -> "₹"
            "S$ (SGD)" -> "S$"
            else -> ""
        }
    }

    inner class QRCodeAdapter(private val qrCodeDataList: List<PayeeData>) :
        RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder>() {

        inner class QRCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val qrCodeImageView: ImageView = itemView.findViewById(R.id.qrCodeImageView)
            val payeeTextView: TextView = itemView.findViewById(R.id.payeeTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRCodeViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.qr_code_item, parent, false)
            return QRCodeViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: QRCodeViewHolder, position: Int) {
            val payeeData = qrCodeDataList[position]
            holder.qrCodeImageView.setImageBitmap(payeeData.qrCode)
            holder.payeeTextView.text = "Payee ${payeeData.payeeNumber}: ${getCurrencySymbol()}${payeeData.amount}"
        }

        override fun getItemCount(): Int = qrCodeDataList.size
    }
}