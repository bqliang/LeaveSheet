package com.bqliang.leavesheet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.leavesheet.data.database.entity.Annex
import com.bqliang.leavesheet.databinding.AnnexItemBinding

class AnnexAdapter :
    ListAdapter<Annex, AnnexAdapter.AnnexViewHolder>(DiffCallback) {

    private companion object DiffCallback : DiffUtil.ItemCallback<Annex>() {
        override fun areItemsTheSame(oldItem: Annex, newItem: Annex) =
            oldItem.fileName == newItem.fileName

        override fun areContentsTheSame(oldItem: Annex, newItem: Annex) =
            oldItem.fileName == newItem.fileName
    }


    inner class AnnexViewHolder(private val annexItemView: AnnexItemBinding) :
        RecyclerView.ViewHolder(annexItemView.root) {
        fun bind(annex: Annex) {
            annexItemView.headline.text = annex.fileName
            annexItemView.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnexViewHolder {
        val annexItemView =
            AnnexItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnexViewHolder(annexItemView)
    }


    override fun onBindViewHolder(holder: AnnexViewHolder, position: Int) =
        holder.bind(getItem(position))
}