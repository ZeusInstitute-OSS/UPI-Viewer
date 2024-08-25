package com.zeusinstitute.upiapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.io.File

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
            exportButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Correct casing for Build
                    lifecycleScope.launch {
                        val transactions = withContext(Dispatchers.IO) {
                            transactionDao.getAllTransactions()
                        }
                        exportTransactionsToXML(transactions)
                        Log.d(
                            "BillHistory",
                            "Export button pressed, exporting to XML"
                        )// Clarify log message
                    }
                } else {
                    // Handle the case for devices below KitKat
                    Toast.makeText(
                        requireContext(),
                        "Export is not supported on devices older than Android 4.4",
                        Toast.LENGTH_SHORT
                    ).show() // Use Toast properly
                }
            }

            lifecycleScope.launch {
                Log.d("BillHistory", "Attempting to read transactions from database")
                transactionDao.getAll()
                    .flowOn(Dispatchers.IO)
                    .collect { transactions ->
                        adapter.submitList(transactions)
                        Log.d("BillHistory", "Fetched ${transactions.size} transactions")
                    }
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
            saveXMLToFile(xmlString)
            println(xmlString) // Print XML to console for now
        } catch (e: Exception) {
            // Handle exceptions during XML creation
            e.printStackTrace()
        }
    }

    private fun saveXMLToFile(xmlString: String) {
        // Store the XML string in a temporary variable for later use
        tempXmlString = xmlString

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // For KitKat and above, use ACTION_CREATE_DOCUMENT
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
                putExtra(Intent.EXTRA_TITLE, "transactions.xml")
            }
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
        }
    }

    // Request code for creating a file (used for KitKat and above)
    private val CREATE_FILE_REQUEST_CODE = 1

    // Temporary variable to store the XML string
    private var tempXmlString: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                context?.contentResolver?.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(tempXmlString?.toByteArray())
                    Toast.makeText(context, "Transactions exported to XML", Toast.LENGTH_SHORT).show()
                }
            }
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

