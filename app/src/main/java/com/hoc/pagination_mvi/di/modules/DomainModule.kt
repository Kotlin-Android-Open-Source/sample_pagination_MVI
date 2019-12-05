package com.hoc.pagination_mvi.di.modules

import com.hoc.pagination_mvi.data.PhotoRepositoryImpl
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProviderImpl
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.RxSchedulerProvider
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.RxSchedulerProviderImpl
import com.hoc.pagination_mvi.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module

@Module
interface DomainModule {

  @Binds
  fun provideRxSchedulerProvider(rxSchedulerProviderImpl: RxSchedulerProviderImpl): RxSchedulerProvider

  @Binds
  fun provideCoroutinesDispatchersProvider(coroutinesDispatchersProviderImpl: CoroutinesDispatchersProviderImpl): CoroutinesDispatchersProvider

  @Binds
  fun providePhotoRepository(photoRepositoryImpl: PhotoRepositoryImpl): PhotoRepository
}