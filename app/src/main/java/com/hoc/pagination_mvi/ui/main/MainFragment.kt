package com.hoc.pagination_mvi.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hoc.pagination_mvi.R
import com.hoc.pagination_mvi.ui.main.MainContract.ViewIntent
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class MainFragment : Fragment() {
  @Inject
  lateinit var factory: ViewModelProvider.Factory
  private val mainVM by viewModels<MainVM> { factory }
  private val compositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidSupportInjection.inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.fragment_main, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bindVM()
  }

  private fun bindVM() {
    mainVM.stateD.observe(viewLifecycleOwner, Observer {

    })
    mainVM.processIntents(
      Observable.mergeArray(
        Observable.just(ViewIntent.Initial)
      )
    ).addTo(compositeDisposable)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    compositeDisposable.clear()
  }
}