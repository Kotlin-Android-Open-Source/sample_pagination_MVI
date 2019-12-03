package com.hoc.pagination_mvi.ui.main

interface MainContract {
  data class ViewState(
    val items: List<Item>,
    val isLoading: Boolean,
    val error: Throwable?
  ) {
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
        isLoading = true,
        error = null
      )
    }
  }

  sealed class Item {
    data class HorizontalList(
      val items: List<HorizontalItem>,
      val isLoading: Boolean,
      val error: Throwable?
    ) : Item() {
      sealed class HorizontalItem {
        data class Item(val s: String) : HorizontalItem()
        data class Placeholder(val state: PlaceholderState) : HorizontalItem()
      }
    }

    data class Photo(
      val albumId: Int,
      val id: Int,
      val thumbnailUrl: String,
      val title: String,
      val url: String
    ) : Item()

    data class Placeholder(val state: PlaceholderState) : Item()
  }

  sealed class PlaceholderState {
    object Loading : PlaceholderState()
    object Idle : PlaceholderState()
    data class Error(val error: Throwable) : PlaceholderState()
  }

  sealed class ViewIntent {
    object Initial : ViewIntent()
  }

  sealed class PartialStateChange {
    fun reduce(vs: ViewState): ViewState {
      TODO()
    }
  }

  sealed class SingleEvent {

  }
}