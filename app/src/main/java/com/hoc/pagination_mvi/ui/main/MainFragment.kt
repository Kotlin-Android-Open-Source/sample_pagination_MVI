package com.hoc.pagination_mvi.ui.main

import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoc.pagination_mvi.R
import com.hoc.pagination_mvi.isOrientationPortrait
import com.hoc.pagination_mvi.ui.main.MainContract.ViewIntent
import com.jakewharton.rxbinding3.recyclerview.scrollEvents
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainFragment : Fragment() {
  @Inject
  lateinit var factory: ViewModelProvider.Factory
  private val mainVM by viewModels<MainVM> { factory }
  private val compositeDisposable = CompositeDisposable()

  private val maxSpanCount get() = if (requireContext().isOrientationPortrait) 2 else 4
  private val visibleThreshold get() = maxSpanCount

  private val adapter = MainAdapter()

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
    setupView()
    bindVM()
  }

  private fun setupView() {
    recycler.run {
      setHasFixedSize(true)
      layoutManager = GridLayoutManager(context, maxSpanCount).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
          override fun getSpanSize(position: Int): Int {
            return if (this@MainFragment.adapter.getItemViewType(position) == R.layout.recycler_item_horizontal_list) {
              2
            } else {
              1
            }
          }
        }
      }
      adapter = this@MainFragment.adapter

      val space = 8
      addItemDecoration(object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
          outRect: Rect,
          view: View,
          parent: RecyclerView,
          state: RecyclerView.State
        ) {
          outRect.left = space
          outRect.bottom = space
          outRect.right = space
          // Add top margin only for the first item to avoid double space between items
          outRect.top = if (parent.getChildLayoutPosition(view) == 0) space else 0
        }
      })
    }
  }

  private fun bindVM() {
    mainVM.stateD.observe(viewLifecycleOwner, Observer {
      it ?: return@Observer
      adapter.submitList(it.items)
    })
    mainVM.processIntents(
      Observable.mergeArray(
        Observable.just(ViewIntent.Initial),
        loadNextPageIntent()
      )
    ).addTo(compositeDisposable)
  }

  private fun loadNextPageIntent(): ObservableSource<ViewIntent> {
    return recycler
      .scrollEvents()
      .throttleFirst(400, TimeUnit.MILLISECONDS)
      .filter { (_, _, dy) ->
        val layoutManager = recycler.layoutManager as GridLayoutManager
        dy > 0 && layoutManager.findLastVisibleItemPosition() + visibleThreshold >= layoutManager.itemCount
      }
      .map { ViewIntent.LoadNextPage }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    compositeDisposable.clear()
    recycler.adapter = null
  }
}