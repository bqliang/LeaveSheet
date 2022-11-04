package com.bqliang.leavesheet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.data.database.entity.Annex
import com.bqliang.leavesheet.databinding.AnnexItemBinding
import com.bqliang.leavesheet.main.LeaveSheetViewModel
import kotlinx.coroutines.launch

class AnnexesAdapter(val leaveSheetViewModel: LeaveSheetViewModel) :
    ListAdapter<Annex, AnnexesAdapter.AnnexViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Annex>() {
        override fun areItemsTheSame(oldItem: Annex, newItem: Annex) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Annex, newItem: Annex) =
            oldItem.fileName == newItem.fileName
    }


    inner class AnnexViewHolder(private val binding: AnnexItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(annex: Annex, position: Int) {
            binding.annexName.text = annex.fileName
            binding.downloadBtn.setOnLongClickListener {
                leaveSheetViewModel.viewModelScope.launch {
                    leaveSheetViewModel.deleteAnnex(annex)
                }
                true
            }
            /**
             * This is important, because it forces the data binding to execute immediately,
             * which allows the RecyclerView to make the correct view size measurements
             */
            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AnnexViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.annex_item,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: AnnexViewHolder, position: Int) {
        val annex = getItem(position)
        holder.bind(annex, position)
    }
}