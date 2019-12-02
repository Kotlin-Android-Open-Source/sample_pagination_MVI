package com.hoc.pagination_mvi.di

import com.hoc.pagination_mvi.MyApp
import com.hoc.pagination_mvi.di.modules.MainActivityModule
import com.hoc.pagination_mvi.di.modules.DataModule
import com.hoc.pagination_mvi.di.modules.DomainModule
import com.hoc.pagination_mvi.di.modules.ViewModelModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@ApplicationScope
@Component(
  modules = [
    AndroidSupportInjectionModule::class,
    MainActivityModule::class,
    DataModule::class,
    DomainModule::class,
    ViewModelModule::class
  ]
)
interface ApplicationComponent : AndroidInjector<MyApp> {
  @Component.Factory
  interface Factory : AndroidInjector.Factory<MyApp>
}