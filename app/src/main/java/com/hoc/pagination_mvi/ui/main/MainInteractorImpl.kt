package com.hoc.pagination_mvi.ui.main

import com.hoc.pagination_mvi.di.ApplicationScope
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.usecase.GetPhotosUseCase
import com.hoc.pagination_mvi.domain.usecase.GetPostsUseCase
import com.hoc.pagination_mvi.ui.main.MainContract.PhotoVS
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
  ): Observable<MainContract.PartialStateChange.PhotoNextPage> {
    return rxObservable(dispatchers.main) {
      send(MainContract.PartialStateChange.PhotoNextPage.Loading)
      try {
        getPhotosUseCase(start = start, limit = limit)
          .map(MainContract::PhotoVS)
          .let { MainContract.PartialStateChange.PhotoNextPage.Data(it) }
          .let { send(it) }
      } catch (e: Exception) {
        delay(500)
        send(MainContract.PartialStateChange.PhotoNextPage.Error(e))
      }
    }
  }

  override fun photoFirstPageChanges(limit: Int): Observable<MainContract.PartialStateChange.PhotoFirstPage> {
    return rxObservable(dispatchers.main) {
      send(MainContract.PartialStateChange.PhotoFirstPage.Loading)
      try {
        getPhotosUseCase(start = 0, limit = limit)
          .map(::PhotoVS)
          .let { MainContract.PartialStateChange.PhotoFirstPage.Data(it) }
          .let { send(it) }
      } catch (e: Exception) {
        delay(500)
        send(MainContract.PartialStateChange.PhotoFirstPage.Error(e))
      }
    }
  }
}