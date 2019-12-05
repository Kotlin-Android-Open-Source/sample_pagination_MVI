package com.hoc.pagination_mvi.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.hoc.pagination_mvi.asObservable
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.RxSchedulerProvider
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainVM @Inject constructor(
  private val interactor: Interactor,
  private val rxSchedulerProvider: RxSchedulerProvider
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
        .withLatestFrom(stateObservable)
        .filter { (_, vs) -> vs.photoItems.isEmpty() }
        .flatMap { interactor.photoFirstPageChanges(limit = PAGE_SIZE) }
    }

  private val nextPageProcessor =
    ObservableTransformer<ViewIntent.LoadNextPage, PartialStateChange> { intents ->
      intents
        .withLatestFrom(stateObservable)
        .filter { (_, vs) -> vs.canLoadNextPage() }
        .map { (_, vs) -> vs.photoItems.size }
        .exhaustMap { interactor.photoNextPageChanges(start = it, limit = PAGE_SIZE) }
    }

  private val retryLoadPageProcessor =
    ObservableTransformer<ViewIntent.RetryLoadPage, PartialStateChange> { intents ->
      intents
        .withLatestFrom(stateObservable)
        .filter { (_, vs) -> vs.shouldRetry() }
        .map { (_, vs) -> vs.photoItems.size }
        .exhaustMap { interactor.photoNextPageChanges(start = it, limit = PAGE_SIZE) }
    }

  private val toPartialStateChange =
    ObservableTransformer<ViewIntent, PartialStateChange> { intents ->
      intents.publish { shared ->
        Observable.mergeArray(
          shared.ofType<ViewIntent.Initial>().compose(initialProcessor),
          shared.ofType<ViewIntent.LoadNextPage>().compose(nextPageProcessor),
          shared.ofType<ViewIntent.RetryLoadPage>().compose(retryLoadPageProcessor)
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
      .observeOn(rxSchedulerProvider.main)
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

