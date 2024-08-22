package com.zeusinstitute.upiapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BillHistory : Fragment() {
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var warningTextView: TextView

    private lateinit var clearButton: Button
    private lateinit var exportButton: Button
    private lateinit var refreshButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bill_history, container, false)

        transactionRecyclerView = view.findViewById(R.id.transactionRecyclerView)
        warningTextView = view.findViewById(R.id.warningTextView)
        clearButton = view.findViewById(R.id.clearButton)
        exportButton = view.findViewById(R.id.exportButton)
        refreshButton = view.findViewById(R.id.refreshButton)

        adapter = TransactionAdapter()
        transactionRecyclerView.adapter = adapter
        transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = (requireContext().applicationContext as UPIAPP).database


        val transactionDao = db.transactionDao()

        refreshButton.setOnClickListener {
            refreshData()
         }

        clearButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) { // Perform database operation on IO dispatcher
                    transactionDao.deleteAll()
                }
                adapter.submitList(emptyList()) // Update adapter after clearing, on the main thread
            }
        }

        /*exportButton.setOnClickListener {
            lifecycleScope.launch {
                exportTransactionsToXML(transactionDao.getAllTransactions())
            }
        }*/

        exportButton.setOnClickListener {
            lifecycleScope.launch {
                val transactions = withContext(Dispatchers.IO) { // Fetch transactions on IO thread
                    transactionDao.getAllTransactions()
                }
                exportTransactionsToXML(transactions) // Call export function on main thread}
                Log.d("BillHistory", "Export button pressed for exporting into database") // Log insertion
            }
        }

        lifecycleScope.launch {
            Log.d("BillHistory", "Attempting to read transactions from database")
            transactionDao.getAll()
                .flowOn(Dispatchers.IO) // Switch Flow collection to IO thread
                .collect { transactions ->
                    adapter.submitList(transactions) // Update adapter on main thread
                    Log.d("BillHistory", "Fetched ${transactions.size} transactions")
                }
        }
        return view
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

                val nameElement = doc.createElement("name")
                nameElement.appendChild(doc.createTextNode(transaction.name))
                transactionElement.appendChild(nameElement)

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
    private fun refreshData() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val smsEnabled = sharedPref.getBoolean("sms_enabled", false)

        warningTextView.visibility = if (smsEnabled) View.GONE else View.VISIBLE
        if (!smsEnabled) {
            warningTextView.text = "History Disabled, enable from Login."
            return // Don't reload data if SMS is disabled
        }

        lifecycleScope.launch {
            val db = (requireContext().applicationContext as UPIAPP).database // Get centralized database
            val transactionDao = db.transactionDao()
            val transactions = withContext(Dispatchers.IO) { transactionDao.getAllTransactionsOrderedByDate().first() }
            adapter.submitList(transactions)
            Log.d("BillHistory", "Fetched ${transactions.size} transactions")
            if (transactions.isEmpty()) {
                warningTextView.text = "No Data Available"
                warningTextView.visibility = View.VISIBLE
            } else {
                warningTextView.text = ""
                warningTextView.visibility = View.GONE
            }
        }
    }
}

