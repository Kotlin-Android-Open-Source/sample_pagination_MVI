package com.hoc.pagination_mvi.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hoc.pagination_mvi.di.AppViewModelFactory
import com.hoc.pagination_mvi.di.ViewModelKey
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProviderImpl
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.RxSchedulerProvider
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.RxSchedulerProviderImpl
import com.hoc.pagination_mvi.ui.main.MainContract
import com.hoc.pagination_mvi.ui.main.MainInteractorImpl
import com.hoc.pagination_mvi.ui.main.MainVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
  @Binds
  abstract fun provideMainInteractor(mainInteractorImpl: MainInteractorImpl): MainContract.Interactor

  @Binds
  abstract fun provideVMFactory(appViewModelFactory: AppViewModelFactory): ViewModelProvider.Factory

  @Binds
  @IntoMap
  @ViewModelKey(MainVM::class)
  abstract fun bindMainVM(mainVM: MainVM): ViewModel
}