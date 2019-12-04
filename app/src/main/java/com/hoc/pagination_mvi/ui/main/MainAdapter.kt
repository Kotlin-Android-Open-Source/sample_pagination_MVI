package com.hoc.pagination_mvi.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoc.pagination_mvi.R
import com.hoc.pagination_mvi.ui.main.MainContract.*


class MainAdapter :
  ListAdapter<Item, MainAdapter.VH>(object : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
      return when (oldItem) {
        is Item.HorizontalList -> newItem is Item.HorizontalList
        is Item.Photo -> {
          if (newItem is Item.Photo) {
            newItem.photo.id == oldItem.photo.id
          } else {
            false
          }
        }
        is Item.Placeholder -> newItem is Item.Placeholder
      } || oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem

    override fun getChangePayload(oldItem: Item, newItem: Item): Any? {
      return when {
        oldItem is Item.Placeholder && newItem is Item.Placeholder -> newItem.state
        oldItem is Item.HorizontalList && newItem is Item.HorizontalList -> newItem
        oldItem is Item.Photo && newItem is Item.Photo -> newItem.photo
        else -> null
      }
    }
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): VH {
    val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return when (viewType) {
      R.layout.recycler_item_photo -> PhotoVH(itemView)
      R.layout.recycler_item_placeholder -> PlaceHolderVH(itemView)
      R.layout.recycler_item_horizontal_list -> HorizontalListVH(itemView)
      else -> error("Unknown viewType=$viewType")
    }
  }

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
    val payload = payloads.firstOrNull() ?: return holder.bind(getItem(position))
    if (payload is PlaceholderState && holder is PlaceHolderVH) {
      return holder.update(payload)
    }
    if (payload is Item.HorizontalList && holder is HorizontalListVH) {
      return holder.update(payload)
    }
    if (payload is Item.Photo && holder is PhotoVH) {
      return holder.update(payload)
    }
  }

  @LayoutRes
  override fun getItemViewType(position: Int) = getItem(position).viewType

  abstract class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: Item)
  }

  private class PhotoVH(itemView: View) : VH(itemView) {
    override fun bind(item: Item) {
      if (item !is Item.Photo) return
      update(item)
    }

    fun update(payload: Item.Photo) {
      //TODO
    }
  }

  private class PlaceHolderVH(itemView: View) : VH(itemView) {
    override fun bind(item: Item) {
      if (item !is Item.Placeholder) return
      update(item.state)
    }

    fun update(state: PlaceholderState) {
//TODO
    }
  }

  private class HorizontalListVH(itemView: View) : VH(itemView) {
    override fun bind(item: Item) {
      if (item !is Item.HorizontalList) return
      update(item)
    }

    fun update(item: Item.HorizontalList) {
//TODO
    }
  }
}