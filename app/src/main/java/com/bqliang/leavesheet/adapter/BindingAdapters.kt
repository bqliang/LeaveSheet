package com.bqliang.leavesheet.adapter

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.leavesheet.data.database.entity.Annex


@BindingAdapter("annexes")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Annex>?) {
    val adapter = recyclerView.adapter as AnnexesAdapter
    adapter.submitList(data)
}

@BindingAdapter("icon")
fun bindAppCompatImageView(imageView: AppCompatImageView, drawable: Drawable) {
    imageView.setImageDrawable(drawable)
}