package com.bqliang.leavesheet.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.leavesheet.data.database.entity.Annex


@BindingAdapter("annexes")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Annex>?) {
    val adapter = recyclerView.adapter as AnnexAdapter
    adapter.submitList(data)
}