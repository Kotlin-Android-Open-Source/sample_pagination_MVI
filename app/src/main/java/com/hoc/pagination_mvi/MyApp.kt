package com.hoc.pagination_mvi

import com.hoc.pagination_mvi.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class MyApp : DaggerApplication() {
  private val applicationComponent by lazy {
    DaggerApplicationComponent.factory().create(this)
  }

  override fun applicationInjector(): AndroidInjector<MyApp> {
    return applicationComponent
  }
}