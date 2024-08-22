package com.zeusinstitute.upiapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import android.util.Xml
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Transaction(
    @Id var id: Long = 0,
    var amount: Double = 0.0,
    var type: String = "",
    var date: String = ""
)

class BillHistory : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var exportButton: Button
    private lateinit var clearButton: Button
    private lateinit var warningText: TextView
    private lateinit var transactionBox: Box<Transaction>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bill_history, container, false)

        recyclerView = view.findViewById(R.id.transactionRecyclerView)
        exportButton = view.findViewById(R.id.exportButton)
        clearButton = view.findViewById(R.id.clearButton)
        warningText = view.findViewById(R.id.warningText)

        transactionBox = ObjectBox.store.boxFor()

        setupRecyclerView()
        setupButtons()
        loadTransactions()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupButtons() {
        exportButton.setOnClickListener { exportToXml() }
        clearButton.setOnClickListener { clearTransactions() }
    }

    private fun loadTransactions() {
        val smsEnabled = requireActivity().getSharedPreferences("com.zeusinstitute.upiapp.preferences", Context.MODE_PRIVATE)
            .getBoolean("sms_enabled", false)

        if (smsEnabled) {
            warningText.visibility = View.GONE
            val transactions = transactionBox.query().orderDesc(Transaction_.date).build().find()
            recyclerView.adapter = TransactionAdapter(transactions)
        } else {
            warningText.visibility = View.VISIBLE
            warningText.text = "Bill History is disabled, enable SMS in login page"
            warningText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        }
    }

    private fun exportToXml() {
        val transactions = transactionBox.all
        context?.let { ctx ->
            val xmlFile = File(ctx.getExternalFilesDir(null), "transactions.xml")
            val serializer: XmlSerializer = Xml.newSerializer()
            val writer = FileOutputStream(xmlFile).writer()

            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.startTag("", "transactions")

            for (transaction in transactions) {
                serializer.startTag("", "transaction")
                serializer.attribute("", "type", transaction.type)
                serializer.startTag("", "amount")
                serializer.text(transaction.amount.toString())
                serializer.endTag("", "amount")
                serializer.startTag("", "date")
                serializer.text(transaction.date)
                serializer.endTag("", "date")
                serializer.endTag("", "transaction")
            }

            serializer.endTag("", "transactions")
            serializer.endDocument()
            writer.close()
        }
    }

    private fun clearTransactions() {
        transactionBox.removeAll()
        loadTransactions()
    }
}

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.amountTextView.text = "Rs. ${transaction.amount}"
        holder.typeTextView.text = transaction.type
        holder.dateTextView.text = transaction.date

        holder.amountTextView.setTextColor(
            ContextCompat.getColor(holder.itemView.context,
                if (transaction.type == "Credit") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            )
        )
    }

    override fun getItemCount() = transactions.size
}