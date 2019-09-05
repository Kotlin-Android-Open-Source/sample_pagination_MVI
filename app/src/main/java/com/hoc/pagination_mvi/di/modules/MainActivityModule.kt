package com.hoc.pagination_mvi.di.modules

import com.hoc.pagination_mvi.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface MainActivityModule {
  @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
  fun contributeMainActivity(): MainActivity
}