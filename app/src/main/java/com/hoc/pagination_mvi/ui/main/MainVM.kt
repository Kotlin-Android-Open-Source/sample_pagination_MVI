package com.hoc.pagination_mvi.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hoc.pagination_mvi.domain.usecase.GetPhotosUseCase
import com.hoc.pagination_mvi.ui.main.MainContract.*
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainVM @Inject constructor(
  private val getPhotosUseCase: GetPhotosUseCase
) : ViewModel() {
  private val initial = ViewState.initial()
  private val _stateD = MutableLiveData<ViewState>().apply { value = initial }
  private val intentS = PublishSubject.create<ViewIntent>()
  private val compositeDisposable = CompositeDisposable()

  val stateD: LiveData<ViewState> get() = _stateD

  fun processIntents(intents: Observable<ViewIntent>) = intents.subscribe(intentS::onNext)!!

  private val toPartialStateChange = ObservableTransformer<ViewIntent, PartialStateChange> {
    TODO()
  }

  init {
    intentS
      .compose(intentFilter)
      .compose(toPartialStateChange)
      .scan(initial) { vs, change -> change.reduce(vs) }
      .subscribeBy { }
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
  }
}