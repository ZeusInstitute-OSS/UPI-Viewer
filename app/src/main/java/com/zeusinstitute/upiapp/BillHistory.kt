package com.zeusinstitute.upiapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import android.util.Xml

data class Transaction(
    val amount: Double,
    val from: String,
    val isCredit: Boolean,
    val date: String
)

class BillHistory : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var exportButton: Button
    private lateinit var clearButton: Button // New clear button
    private val transactions = mutableListOf<Transaction>()
    private lateinit var sharedPref: SharedPreferences // For accessing SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bill_history, container, false)

        recyclerView = view.findViewById(R.id.transactionRecyclerView)
        exportButton = view.findViewById(R.id.exportButton)
        clearButton = view.findViewById(R.id.clearButton) // Initialize clear button

        sharedPref = requireActivity().getSharedPreferences("com.zeusinstitute.upiapp.preferences", Context.MODE_PRIVATE)

        setupRecyclerView()
        setupExportButton()
        setupClearButton()
        loadTransactions()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TransactionAdapter(transactions)
    }

    private fun setupExportButton() {
        exportButton.setOnClickListener {
            exportToXml()
        }
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            clearTransactions()
        }
    }

    private fun loadTransactions() {
        val smsEnabled = sharedPref.getBoolean("sms_enabled", false)
        if (smsEnabled) {
            // Get transactions from SMSService (you'll need a mechanism to do this)
            val smsTransactions = getTransactionsFromSMSService()
            transactions.addAll(smsTransactions)
            recyclerView.adapter?.notifyDataSetChanged()
        } else {
            Toast.makeText(context, "Cannot take new elements, SMS scanning is disabled", Toast.LENGTH_SHORT).show()
            // Load from persistent storage if SMS is disabled
            loadTransactionsFromStorage()
        }
    }

    private fun getTransactionsFromSMSService(): List<Transaction> {
        // TODO: Implement logic to retrieve transactions from SMSService
        // This might involve using a shared data store, broadcast receiver, etc.
        return emptyList() // Replace with actual transaction retrieval
    }

    private fun exportToXml() {
        context?.let { ctx ->
            val xmlFile = File(ctx.getExternalFilesDir(null), "transactions.xml")
            val serializer: XmlSerializer = Xml.newSerializer()
            val writer = FileOutputStream(xmlFile).writer()

            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.startTag("", "transactions")

            for (transaction in transactions) {
                serializer.startTag("", "transaction")
                serializer.attribute("", "type", if (transaction.isCredit) "credit" else "debit")
                serializer.startTag("", "amount")
                serializer.text(transaction.amount.toString())
                serializer.endTag("", "amount")
                serializer.startTag("", "from")
                serializer.text(transaction.from)
                serializer.endTag("", "from")
                serializer.startTag("", "date")
                serializer.text(transaction.date)
                serializer.endTag("", "date")
                serializer.endTag("", "transaction")
            }

            serializer.endTag("", "transactions")
            serializer.endDocument()
            writer.close()

            // TODO: Show a success message to the user
        }
    }
    private fun clearTransactions() {
        transactions.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        clearTransactionsInStorage() // Clear from persistent storage as well
        Toast.makeText(context, "Transactions cleared", Toast.LENGTH_SHORT).show()
    }

    // --- Persistent Storage Functions (You need to implement these) ---

    private fun loadTransactionsFromStorage() {
        // TODO: Load transactions from your chosen storage (e.g., Room database)
    }

    private fun clearTransactionsInStorage() {
        // TODO: Clear transactions from your chosen storage
    }
}

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val fromTextView: TextView = view.findViewById(R.id.fromTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.amountTextView.text = "Rs. ${transaction.amount}"
        holder.fromTextView.text = transaction.from
        holder.dateTextView.text = transaction.date

        if (transaction.isCredit) {
            holder.iconImageView.setImageResource(R.drawable.ic_credit) // You need to create this drawable
            holder.amountTextView.setTextColor(Color.GREEN)
        } else {
            holder.iconImageView.setImageResource(R.drawable.ic_debit) // You need to create this drawable
            holder.amountTextView.setTextColor(Color.RED)
        }
    }

    override fun getItemCount() = transactions.size
}