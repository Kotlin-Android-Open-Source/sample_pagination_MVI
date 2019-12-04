package com.hoc.pagination_mvi.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.hoc.pagination_mvi.asObservable
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.usecase.GetPhotosUseCase
import com.hoc.pagination_mvi.exhaustMap
import com.hoc.pagination_mvi.ui.main.MainContract.*
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.rxObservable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainVM @Inject constructor(
  private val getPhotosUseCase: GetPhotosUseCase,
  private val dispatchers: CoroutinesDispatchersProvider
) : ViewModel() {
  private val initial = ViewState.initial()
  private val _stateD = MutableLiveData<ViewState>().apply { value = initial }
  private val stateS = BehaviorSubject.createDefault(initial)

  private val stateObservable get() = stateS.asObservable()
  private val intentS = PublishSubject.create<ViewIntent>()
  private val compositeDisposable = CompositeDisposable()

  val stateD get() = _stateD.distinctUntilChanged()

  fun processIntents(intents: Observable<ViewIntent>) = intents.subscribe(intentS::onNext)!!

  private val initialProcessor =
    ObservableTransformer<ViewIntent.Initial, PartialStateChange> { intents ->
      intents
        .withLatestFrom(stateS)
        .filter { (_, vs) -> vs.photoItems.isEmpty() }
        .flatMap {
          rxObservable(dispatchers.main) {
            send(PartialStateChange.PhotoFirstPage.Loading)
            try {
              getPhotosUseCase(start = 0, limit = PAGE_SIZE)
                .map(::PhotoVS)
                .let { PartialStateChange.PhotoFirstPage.Data(it) }
                .let { send(it) }
            } catch (e: Exception) {
              send(PartialStateChange.PhotoFirstPage.Error(e))
            }
          }
        }
    }

  private val nextPageProcessor =
    ObservableTransformer<ViewIntent.LoadNextPage, PartialStateChange> { intents ->
      intents
        .throttleFirst(400, TimeUnit.MILLISECONDS)
        .withLatestFrom(stateObservable)
        .filter { (_, vs) -> canLoadNextPage(vs) }
        .map { (_, vs) -> vs.photoItems.size }
        .exhaustMap { start ->
          rxObservable(dispatchers.main) {
            send(PartialStateChange.PhotoNextPage.Loading)
            try {
              getPhotosUseCase(start = start, limit = PAGE_SIZE)
                .map(::PhotoVS)
                .let { PartialStateChange.PhotoNextPage.Data(it) }
                .let { send(it) }
            } catch (e: Exception) {
              send(PartialStateChange.PhotoNextPage.Error(e))
            }
          }
        }
    }

  private fun canLoadNextPage(vs: ViewState): Boolean {
    return !vs.isLoading &&
        vs.error == null &&
        vs.photoItems.isNotEmpty() &&
        (vs.items.findLast { it is Item.Placeholder } as? Item.Placeholder)?.state == PlaceholderState.Idle
  }

  private val toPartialStateChange =
    ObservableTransformer<ViewIntent, PartialStateChange> { intents ->
      intents.publish { shared ->
        Observable.mergeArray(
          shared.ofType<ViewIntent.Initial>().compose(initialProcessor),
          shared.ofType<ViewIntent.LoadNextPage>().compose(nextPageProcessor)
        )
      }
    }

  init {
    stateS
      .subscribeBy { _stateD.value = it }
      .addTo(compositeDisposable)

    intentS
      .compose(intentFilter)
      .compose(toPartialStateChange)
      .observeOn(AndroidSchedulers.mainThread())
      .scan(initial) { vs, change -> change.reduce(vs) }
      .subscribe(stateS::onNext)
      .addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.dispose()
  }

  private companion object {
    val intentFilter = ObservableTransformer<ViewIntent, ViewIntent> { intents ->
      intents.publish { shared ->
        Observable.mergeArray(
          shared.ofType<ViewIntent.Initial>().take(1),
          shared.filter { it !is ViewIntent.Initial }
        )
      }
    }
    const val PAGE_SIZE = 20
  }
}

