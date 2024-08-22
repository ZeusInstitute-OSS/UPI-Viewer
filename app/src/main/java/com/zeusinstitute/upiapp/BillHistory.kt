package com.zeusinstitute.upiapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import com.zeusinstitute.upiapp.PayTransaction

class BillHistory : Fragment() {
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var warningTextView: TextView

    private lateinit var clearButton: Button
    private lateinit var exportButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bill_history, container, false)

        clearButton = view.findViewById(R.id.clearButton) // Initialize clearButton
        exportButton = view.findViewById(R.id.exportButton) // Initialize exportButton

        transactionRecyclerView = view.findViewById(R.id.transactionRecyclerView)
        warningTextView = view.findViewById(R.id.warningTextView)

        adapter = TransactionAdapter()
        transactionRecyclerView.adapter = adapter
        transactionRecyclerView.layoutManager = LinearLayoutManager(context)

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "transactions"
        ).build()

        val transactionDao = db.transactionDao()

        clearButton.setOnClickListener {
            lifecycleScope.launch {
                transactionDao.deleteAll() // Clear database
                transactionRecyclerView.removeAllViews() // Clear UI
            }
        }

        exportButton.setOnClickListener {
            lifecycleScope.launch {
                exportTransactionsToXML(transactionDao.getAllTransactions())
            }
        }

        lifecycleScope.launch {
            transactionDao.getAll().collect { transactions ->
                transactionRecyclerView.removeAllViews()
                transactions.forEach { transaction ->
                    val transactionItemView = inflater.inflate(R.layout.transaction_item, transactionRecyclerView, false)
                    val transactionIcon = transactionItemView.findViewById<ImageView>(R.id.transactionIcon)
                    val transactionNameTextView = transactionItemView.findViewById<TextView>(R.id.transactionNameTextView)
                    val transactionAmountTextView = transactionItemView.findViewById<TextView>(R.id.transactionAmountTextView)
                    val transactionDateTextView = transactionItemView.findViewById<TextView>(R.id.transactionDateTextView)

                    // Set icon based on transaction type (replace with your actual drawables)
                    transactionIcon.setImageResource(if (transaction.type == "Debit") R.drawable.ic_debit else R.drawable.ic_credit)

                    // Set transaction details (extract name from SMS data if possible)
                    transactionNameTextView.text = extractNameFromTransaction(transaction) // Implement name extraction
                    transactionAmountTextView.text = "₹${transaction.amount}" // Format amount with currency symbol
                    transactionDateTextView.text = transaction.date

                    transactionRecyclerView.addView(transactionItemView)
                }
            }
        }

        return view
    }

    private fun extractNameFromTransaction(transaction: PayTransaction): String {
        // TODO: Implement logic to extract the name from the transaction data (if available in SMS)
        // For now, return a placeholder
        return "Unknown"
    }

    private fun exportTransactionsToXML(transactions: List<PayTransaction>) {
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("transactions")
            doc.appendChild(rootElement)

            transactions.forEach { transaction ->
                val transactionElement = doc.createElement("transaction")
                rootElement.appendChild(transactionElement)

                val typeElement = doc.createElement("type")
                typeElement.appendChild(doc.createTextNode(transaction.type))
                transactionElement.appendChild(typeElement)

                val amountElement = doc.createElement("amount")
                amountElement.appendChild(doc.createTextNode(transaction.amount.toString()))
                transactionElement.appendChild(amountElement)

                val dateElement = doc.createElement("date")
                dateElement.appendChild(doc.createTextNode(transaction.date))
                transactionElement.appendChild(dateElement)
            }

            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

            val writer = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(writer))
            val xmlString = writer.toString()

            // TODO: Save the xmlString to a file or share it as needed
            println(xmlString) // Print XML to console for now
        } catch (e: Exception) {
            // Handle exceptions during XML creation
            e.printStackTrace()
        }
    }
}