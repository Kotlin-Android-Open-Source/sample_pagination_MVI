package com.hoc.pagination_mvi.di.modules

import com.hoc.pagination_mvi.ui.main.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {
  @ContributesAndroidInjector
  abstract fun contributeMainFragment(): MainFragment
}