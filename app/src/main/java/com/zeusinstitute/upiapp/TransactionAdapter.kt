package com.zeusinstitute.upiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionIcon: ImageView = itemView.findViewById(R.id.transactionIcon)
        val transactionNameTextView: TextView = itemView.findViewById(R.id.transactionNameTextView)
        val transactionAmountTextView: TextView = itemView.findViewById(R.id.transactionAmountTextView)
        val transactionDateTextView: TextView = itemView.findViewById(R.id.transactionDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.transactionIcon.setImageResource(
            if (transaction.type == "Debit") R.drawable.ic_debit
            else R.drawable.ic_credit
        )
        holder.transactionNameTextView.text = "Unknown" // Replace with name extraction logic
        holder.transactionAmountTextView.text = "₹${transaction.amount}"
        holder.transactionDateTextView.text = transaction.date
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}