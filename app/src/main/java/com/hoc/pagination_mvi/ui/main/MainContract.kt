package com.hoc.pagination_mvi.ui.main

import androidx.annotation.LayoutRes
import com.hoc.pagination_mvi.R
import com.hoc.pagination_mvi.domain.entity.Photo as PhotoDomain

interface MainContract {
  data class ViewState(
    val items: List<Item>,
    val photoItems: List<Item.Photo>
  ) {

    fun canLoadNextPage(): Boolean {
      return photoItems.isNotEmpty() &&
          (items.singleOrNull { it is Item.Placeholder } as? Item.Placeholder)
            ?.state == PlaceholderState.Idle
    }

    fun shouldRetry(): Boolean {
      return (items.singleOrNull { it is Item.Placeholder } as? Item.Placeholder)
        ?.state is PlaceholderState.Error
    }

    companion object Factory {
      @JvmStatic
      fun initial() = ViewState(
        items = listOf(
          Item.HorizontalList(
            items = emptyList(),
            isLoading = true,
            error = null
          )
        ),
        photoItems = emptyList()
      )
    }
  }

  sealed class Item(@LayoutRes val viewType: Int) {

    data class HorizontalList(
      val items: List<HorizontalItem>,
      val isLoading: Boolean,
      val error: Throwable?
    ) : Item(R.layout.recycler_item_horizontal_list) {
      sealed class HorizontalItem {
        data class Item(val s: String) : HorizontalItem()
        data class Placeholder(val state: PlaceholderState) : HorizontalItem()
      }
    }

    data class Photo(val photo: PhotoVS) : Item(R.layout.recycler_item_photo)

    data class Placeholder(val state: PlaceholderState) : Item(R.layout.recycler_item_placeholder)
  }

  data class PhotoVS(
    val albumId: Int,
    val id: Int,
    val thumbnailUrl: String,
    val title: String,
    val url: String
  ) {
    constructor(domain: PhotoDomain) : this(
      id = domain.id,
      albumId = domain.albumId,
      thumbnailUrl = domain.thumbnailUrl,
      title = domain.title,
      url = domain.url
    )
  }

  sealed class PlaceholderState {
    object Loading : PlaceholderState()
    object Idle : PlaceholderState()
    data class Error(val error: Throwable) : PlaceholderState()

    override fun toString() = when (this) {
      Loading -> "PlaceholderState::Loading"
      Idle -> "PlaceholderState::Idle"
      is Error -> "PlaceholderState::Error($error)"
    }
  }

  sealed class ViewIntent {
    object Initial : ViewIntent()
    object LoadNextPage : ViewIntent()
    object RetryLoadPage : ViewIntent()
  }

  sealed class PartialStateChange {
    abstract fun reduce(vs: ViewState): ViewState

    sealed class PhotoFirstPage : PartialStateChange() {
      data class Data(val photos: List<PhotoVS>) : PhotoFirstPage()
      data class Error(val error: Throwable) : PhotoFirstPage()
      object Loading : PhotoFirstPage()

      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          is Data -> {
            val photoItems = this.photos.map { Item.Photo(it) }
            vs.copy(
              items = vs.items.filter { it !is Item.Photo && it !is Item.Placeholder }
                  + photoItems
                  + Item.Placeholder(PlaceholderState.Idle),
              photoItems = photoItems
            )
          }
          is Error -> vs.copy(
            items = vs.items.filter { it !is Item.Photo && it !is Item.Placeholder }
                + Item.Placeholder(PlaceholderState.Error(this.error)),
            photoItems = emptyList()
          )
          Loading -> vs.copy(
            items = vs.items.filter { it !is Item.Photo && it !is Item.Placeholder }
                + Item.Placeholder(PlaceholderState.Loading)
          )
        }
      }
    }

    sealed class PhotoNextPage : PartialStateChange() {
      data class Data(val photos: List<PhotoVS>) : PhotoNextPage()
      data class Error(val error: Throwable) : PhotoNextPage()
      object Loading : PhotoNextPage()

      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          is Data -> {
            val photoItems =
              vs.items.filterIsInstance<Item.Photo>() + this.photos.map { Item.Photo(it) }

            vs.copy(
              items = vs.items.filter { it !is Item.Photo && it !is Item.Placeholder }
                  + photoItems
                  + Item.Placeholder(PlaceholderState.Idle),
              photoItems = photoItems
            )
          }
          is Error -> vs.copy(
            items = vs.items.filter { it !is Item.Placeholder } +
                Item.Placeholder(
                  PlaceholderState.Error(
                    this.error
                  )
                )
          )
          Loading -> vs.copy(
            items = vs.items.filter { it !is Item.Placeholder } +
                Item.Placeholder(PlaceholderState.Loading)
          )
        }
      }
    }
  }

  sealed class SingleEvent {

  }
}