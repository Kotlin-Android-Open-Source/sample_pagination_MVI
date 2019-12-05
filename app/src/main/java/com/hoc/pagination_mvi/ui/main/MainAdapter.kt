package com.hoc.pagination_mvi.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.hoc.pagination_mvi.R
import com.hoc.pagination_mvi.asObservable
import com.hoc.pagination_mvi.ui.main.MainContract.*
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recycler_item_photo.view.*
import kotlinx.android.synthetic.main.recycler_item_placeholder.view.*


class MainAdapter(private val compositeDisposable: CompositeDisposable) :
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

  private val retryS = PublishSubject.create<Unit>()
  val retryObservable get() = retryS.asObservable()

  override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): VH {
    val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return when (viewType) {
      R.layout.recycler_item_photo -> PhotoVH(itemView)
      R.layout.recycler_item_placeholder -> PlaceHolderVH(itemView, parent)
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
    private val image = itemView.image!!

    override fun bind(item: Item) {
      if (item !is Item.Photo) return
      update(item)
    }

    fun update(item: Item.Photo) {
      image.load(item.photo.thumbnailUrl) {
        crossfade(true)
        placeholder(R.drawable.placeholder)
        error(R.drawable.placeholder)
      }
    }
  }

  private inner class PlaceHolderVH(itemView: View, parent: ViewGroup) : VH(itemView) {
    private val progressBar = itemView.progress_bar!!
    private val textError = itemView.text_error!!
    private val buttonRetry = itemView.button_retry!!

    init {
      buttonRetry
        .clicks()
        .takeUntil(parent.detaches())
        .filter {
          val position = adapterPosition
          if (position == RecyclerView.NO_POSITION) {
            false
          } else {
            (getItem(position) as? Item.Placeholder)?.state is PlaceholderState.Error
          }
        }
        .subscribeBy { retryS.onNext(Unit) }
        .addTo(compositeDisposable)
    }

    override fun bind(item: Item) {
      if (item !is Item.Placeholder) return
      update(item.state)
    }

    fun update(state: PlaceholderState) {
      Log.d("###", state.toString())
      when (state) {
        PlaceholderState.Loading -> {
          progressBar.isInvisible = false
          textError.isInvisible = true
          buttonRetry.isInvisible = true
        }
        PlaceholderState.Idle -> {
          progressBar.isInvisible = true
          textError.isInvisible = true
          buttonRetry.isInvisible = true
        }
        is PlaceholderState.Error -> {
          progressBar.isInvisible = true
          textError.isInvisible = false
          buttonRetry.isInvisible = false
          textError.text = state.error.message
        }
      }
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