package com.zeusinstitute.upiapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import android.util.Xml
import android.widget.ImageView
import androidx.core.content.ContextCompat

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String,
    val date: String
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction ORDER BY date DESC")
    fun getAll(): List<Transaction>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("DELETE FROM transaction")
    suspend fun deleteAll()
}

@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transaction_database"
                ).build().also { instance = it }
            }
        }
    }
}

class BillHistory : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var exportButton: Button
    private lateinit var clearButton: Button
    private lateinit var db: AppDatabase
    private lateinit var warningText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bill_history, container, false)

        recyclerView = view.findViewById(R.id.transactionRecyclerView)
        exportButton = view.findViewById(R.id.exportButton)
        clearButton = view.findViewById(R.id.clearButton)
        warningText = view.findViewById(R.id.warningText)

        db = AppDatabase.getInstance(requireContext())

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
            lifecycleScope.launch {
                val transactions = db.transactionDao().getAll()
                recyclerView.adapter = TransactionAdapter(transactions)
            }
        } else {
            warningText.visibility = View.VISIBLE
            warningText.text = "Bill History is disabled, enable SMS in login page"
            warningText.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
    }

    private fun exportToXml() {
        lifecycleScope.launch {
            val transactions = db.transactionDao().getAll()
            val xmlFile = File(requireContext().getExternalFilesDir(null), "transactions.xml")
            val serializer: XmlSerializer = Xml.newSerializer()
            FileOutputStream(xmlFile).writer().use { writer ->
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
            }
        }
    }

    private fun clearTransactions() {
        lifecycleScope.launch {
            db.transactionDao().deleteAll()
            loadTransactions()
        }
    }
}

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
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

        holder.iconImageView.setImageResource(
            if (transaction.type == "Credit") R.drawable.ic_credit else R.drawable.ic_debit
        )

        holder.amountTextView.setTextColor(
            ContextCompat.getColor(holder.itemView.context,
                if (transaction.type == "Credit") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            )
        )
    }

    override fun getItemCount() = transactions.size
}