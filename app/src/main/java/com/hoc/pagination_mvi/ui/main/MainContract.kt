package com.hoc.pagination_mvi.ui.main

import androidx.annotation.LayoutRes
import com.hoc.pagination_mvi.R
import com.hoc.pagination_mvi.ui.main.MainContract.Item.HorizontalList.HorizontalItem
import io.reactivex.Observable
import com.hoc.pagination_mvi.domain.entity.Photo as PhotoDomain
import com.hoc.pagination_mvi.domain.entity.Post as PostDomain

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
            error = null,
            postItems = emptyList()
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
      val error: Throwable?,
      val postItems: List<HorizontalItem.Post>
    ) : Item(R.layout.recycler_item_horizontal_list) {

      sealed class HorizontalItem(@LayoutRes val viewType: Int) {
        data class Post(val post: PostVS) : HorizontalItem(R.layout.recycler_item_horizontal_post)

        data class Placeholder(val state: PlaceholderState) :
          HorizontalItem(R.layout.recycler_item_horizontal_placeholder)
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

  data class PostVS(
    val body: String,
    val id: Int,
    val title: String,
    val userId: Int
  ) {
    constructor(domain: PostDomain) : this(
      body = domain.body,
      userId = domain.userId,
      id = domain.id,
      title = domain.title
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

    sealed class PostFirstPage : PartialStateChange() {
      data class Data(val posts: List<PostVS>) : PostFirstPage()
      data class Error(val error: Throwable) : PostFirstPage()
      object Loading : PostFirstPage()

      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          is Data -> {
            vs.copy(
              items = vs.items.map {
                if (it is Item.HorizontalList) {
                  val postItems = this.posts.map { HorizontalItem.Post(it) }
                  it.copy(
                    items = postItems + HorizontalItem.Placeholder(PlaceholderState.Idle),
                    isLoading = false,
                    error = null,
                    postItems = postItems
                  )
                } else {
                  it
                }
              }
            )
          }
          is Error -> {
            vs.copy(
              items = vs.items.map {
                if (it is Item.HorizontalList) {
                  it.copy(
                    items = emptyList(),
                    isLoading = false,
                    error = error,
                    postItems = emptyList()
                  )
                } else {
                  it
                }
              }
            )
          }
          Loading -> {
            vs.copy(
              items = vs.items.map {
                if (it is Item.HorizontalList) {
                  it.copy(
                    items = emptyList(),
                    isLoading = true,
                    error = null,
                    postItems = emptyList()
                  )
                } else {
                  it
                }
              }
            )
          }
        }
      }
    }
  }

  sealed class SingleEvent {

  }

  interface Interactor {
    fun photoNextPageChanges(start: Int, limit: Int): Observable<PartialStateChange.PhotoNextPage>
    fun photoFirstPageChanges(limit: Int): Observable<PartialStateChange.PhotoFirstPage>

    fun postFirstPageChanges(limit: Int): Observable<PartialStateChange.PostFirstPage>
  }
}