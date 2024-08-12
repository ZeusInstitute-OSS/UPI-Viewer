import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.zeusinstitute.upiapp.R

// Data class to hold payee information
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
    private lateinit var upiIdTextView: TextView
    private lateinit var dynamicPayeesContainer: LinearLayout

    private val qrCodeDataList: MutableList<PayeeData> = mutableListOf()
    private lateinit var qrCodeAdapter: QRCodeAdapter

    private var numPayeesCreated = 3 // Start with 3 persistent payees
    private lateinit var savedData: String // To store the saved UPI ID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        upiIdTextView = view.findViewById(R.id.upiIdTextView)
        dynamicPayeesContainer = view.findViewById(R.id.dynamicPayeesContainer)

        // Get saved UPI ID from SharedPreferences
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        savedData = sharedPref.getString("saved_data", "") ?: ""
        upiIdTextView.text = "UPI ID: $savedData"

        // Initialize RecyclerView
        qrCodeAdapter = QRCodeAdapter(qrCodeDataList)
        qrCodeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        qrCodeRecyclerView.adapter = qrCodeAdapter

        // Add Payee Button
        addPayeeButton.setOnClickListener {
            numPayeesCreated++
            addNewPayeeEditText()
        }

        // Clear Button
        clearButton.setOnClickListener {
            totalAmountEditText.text.clear()
            remainingAmountTextView.text = "Remaining amount: 0"
            payee1EditText.text.clear()
            payee2EditText.text.clear()
            payee3EditText.text.clear()
            dynamicPayeesContainer.removeAllViews() // Remove dynamically added EditTexts
            qrCodeDataList.clear()
            qrCodeAdapter.notifyDataSetChanged()
            numPayeesCreated = 3 // Reset number of payees created
        }

        // Generate QR Button
        generateQrButton.setOnClickListener {
            qrCodeDataList.clear() // Clear previous QR codes
            val totalAmount = totalAmountEditText.text.toString().toIntOrNull() ?: 0
            val payee1Amount = payee1EditText.text.toString().toIntOrNull() ?: 0
            val payee2Amount = payee2EditText.text.toString().toIntOrNull() ?: 0
            val payee3Amount = payee3EditText.text.toString().toIntOrNull() ?: 0

            // Generate QR codes (using the retrieved savedData)
            generateQRCode(savedData, totalAmount, 0)?.let { qrCodeDataList.add(it) }
            generateQRCode(savedData, payee1Amount, 1)?.let { qrCodeDataList.add(it) }
            generateQRCode(savedData, payee2Amount, 2)?.let { qrCodeDataList.add(it) }
            generateQRCode(savedData, payee3Amount, 3)?.let { qrCodeDataList.add(it) }

            // Generate QR codes for dynamically added payees
            for (i in 0 until dynamicPayeesContainer.childCount) {
                val editText = dynamicPayeesContainer.getChildAt(i) as? EditText
                val amount = editText?.text.toString().toIntOrNull() ?: 0
                generateQRCode(savedData, amount, i + 4)?.let { qrCodeDataList.add(it) }
            }

            qrCodeAdapter.notifyDataSetChanged()
            updateRemainingAmount()
        }

        // Listen for changes in total amount and payee amounts to update remaining amount
        totalAmountEditText.addTextChangedListener { updateRemainingAmount() }
        payee1EditText.addTextChangedListener { updateRemainingAmount() }
        payee2EditText.addTextChangedListener { updateRemainingAmount() }
        payee3EditText.addTextChangedListener { updateRemainingAmount() }

        return view
    }

    private fun addNewPayeeEditText() {
        val editText = EditText(requireContext())
        editText.hint = "Enter payee $numPayeesCreated amount"
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dynamicPayeesContainer.addView(editText)

        // Add a text changed listener to the new EditText
        editText.addTextChangedListener { updateRemainingAmount() }
    }

    private fun updateRemainingAmount() {
        val totalAmount = totalAmountEditText.text.toString().toIntOrNull() ?: 0
        var paidAmount = payee1EditText.text.toString().toIntOrNull() ?: 0
        paidAmount += payee2EditText.text.toString().toIntOrNull() ?: 0
        paidAmount += payee3EditText.text.toString().toIntOrNull() ?: 0

        // Add amounts from dynamically created EditTexts
        for (i in 0 until dynamicPayeesContainer.childCount) {
            val editText = dynamicPayeesContainer.getChildAt(i) as? EditText
            paidAmount += editText?.text.toString().toIntOrNull() ?: 0
        }

        val remainingAmount = totalAmount - paidAmount
        remainingAmountTextView.text = "Remaining amount: $remainingAmount"
    }

    private fun generateQRCode(upiId: String, amount: Int, payeeNumber: Int): PayeeData? {
        if (amount <= 0) return null // Don't generate QR code for zero or negative amounts
        val barcodeEncoder = BarcodeEncoder()
        val upiString = "upi://pay?pa=$upiId&tn=undefined&am=$amount" // Replace with your actual UPI string format
        return try {
            val qrCode = barcodeEncoder.encodeBitmap(upiString, BarcodeFormat.QR_CODE, 200, 200)
            PayeeData(amount, qrCode, payeeNumber)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    // RecyclerView Adapter for QR Codes
    inner class QRCodeAdapter(private val qrCodeDataList: List<PayeeData>) :
        RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder>() {

        inner class QRCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val qrCodeImageView: ImageView = itemView.findViewById(R.id.qrCodeImageView)
            val payeeTextView: TextView = itemView.findViewById(R.id.payeeTextView) // Add this to qr_code_item.xml
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRCodeViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.qr_code_item, parent, false)
            return QRCodeViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: QRCodeViewHolder, position: Int) {
            val payeeData = qrCodeDataList[position]
            holder.qrCodeImageView.setImageBitmap(payeeData.qrCode)
            holder.payeeTextView.text = "Payee ${payeeData.payeeNumber + 1}: ₹${payeeData.amount}"
        }

        override fun getItemCount(): Int = qrCodeDataList.size
    }
}