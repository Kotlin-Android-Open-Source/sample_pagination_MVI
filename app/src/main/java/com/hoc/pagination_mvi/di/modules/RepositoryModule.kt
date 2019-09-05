package com.hoc.pagination_mvi.di.modules

import com.hoc.pagination_mvi.data.PhotoRepositoryImpl
import com.hoc.pagination_mvi.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {
  @Binds
  fun providePhotoRepository(photoRepositoryImpl: PhotoRepositoryImpl): PhotoRepository
}