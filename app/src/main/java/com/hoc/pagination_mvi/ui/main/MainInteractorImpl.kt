package com.hoc.pagination_mvi.ui.main

import com.hoc.pagination_mvi.di.ApplicationScope
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.usecase.GetPhotosUseCase
import com.hoc.pagination_mvi.domain.usecase.GetPostsUseCase
import com.hoc.pagination_mvi.ui.main.MainContract.PartialStateChange.*
import com.hoc.pagination_mvi.ui.main.MainContract.PhotoVS
import com.hoc.pagination_mvi.ui.main.MainContract.PostVS
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject

@ApplicationScope
@ExperimentalCoroutinesApi
class MainInteractorImpl @Inject constructor(
  private val getPhotosUseCase: GetPhotosUseCase,
  private val getPostsUseCase: GetPostsUseCase,
  private val dispatchers: CoroutinesDispatchersProvider
) : MainContract.Interactor {
  override fun photoNextPageChanges(
    start: Int,
    limit: Int
  ): Observable<PhotoNextPage> {
    return rxObservable(dispatchers.main) {
      send(PhotoNextPage.Loading)
      try {
        getPhotosUseCase(start = start, limit = limit)
          .map(MainContract::PhotoVS)
          .let { PhotoNextPage.Data(it) }
          .let { send(it) }
      } catch (e: Exception) {
        delay(500)
        send(PhotoNextPage.Error(e))
      }
    }
  }

  override fun photoFirstPageChanges(limit: Int): Observable<PhotoFirstPage> {
    return rxObservable(dispatchers.main) {
      send(PhotoFirstPage.Loading)
      try {
        getPhotosUseCase(start = 0, limit = limit)
          .map(::PhotoVS)
          .let { PhotoFirstPage.Data(it) }
          .let { send(it) }
      } catch (e: Exception) {
        delay(500)
        send(PhotoFirstPage.Error(e))
      }
    }
  }

  override fun postFirstPageChanges(limit: Int): Observable<PostFirstPage> {
    return rxObservable(dispatchers.main) {
      send(PostFirstPage.Loading)
      try {
        getPostsUseCase(start = 0, limit = limit)
          .map(::PostVS)
          .let { PostFirstPage.Data(it) }
          .let { send(it) }
      } catch (e: Exception) {
        delay(500)
        send(PostFirstPage.Error(e))
      }
    }
  }

  override fun postNextPageChanges(
    start: Int,
    limit: Int
  ): Observable<PostNextPage> {
    return rxObservable(dispatchers.main) {
      send(PostNextPage.Loading)
      try {
        getPostsUseCase(start = start, limit = limit)
          .map(::PostVS)
          .let { PostNextPage.Data(it) }
          .let { send(it) }
      } catch (e: Exception) {
        delay(500)
        send(PostNextPage.Error(e))
      }
    }
  }

  override fun refreshAll(
    limitPost: Int,
    limitPhoto: Int
  ): Observable<Refresh> {
    return rxObservable(dispatchers.main) {
      send(Refresh.Refreshing)

      coroutineScope {
        val async1 = async { getPostsUseCase(limit = limitPost, start = 0) }
        val async2 = async { getPhotosUseCase(limit = limitPhoto, start = 0) }

        try {
          send(
            Refresh.Success(
              posts = async1.await().map(::PostVS),
              photos = async2.await().map(::PhotoVS)
            )
          )
        } catch (e: Exception) {
          delay(500)
          send(Refresh.Error(e))
        }
      }
    }
  }
}