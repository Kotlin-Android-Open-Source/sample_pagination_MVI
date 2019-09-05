package com.hoc.pagination_mvi.data

import com.hoc.pagination_mvi.data.remote.ApiService
import com.hoc.pagination_mvi.di.ApplicationScope
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.entity.Photo
import com.hoc.pagination_mvi.domain.repository.PhotoRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ApplicationScope
class PhotoRepositoryImpl @Inject constructor(
  private val apiService: ApiService,
  private val dispatchersProvider: CoroutinesDispatchersProvider
) : PhotoRepository {
  override suspend fun getPhotos(start: Int, limit: Int): List<Photo> {
    return withContext(dispatchersProvider.io) {
      apiService.getPhotos(start = start, limit = limit).map {
        Photo(
          id = it.id,
          title = it.title,
          albumId = it.albumId,
          thumbnailUrl = it.thumbnailUrl,
          url = it.url
        )
      }
    }
  }
}