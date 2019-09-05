package com.hoc.pagination_mvi.domain.usecase

import com.hoc.pagination_mvi.domain.entity.Photo
import com.hoc.pagination_mvi.domain.repository.PhotoRepository

class GetPhotosUseCase(
  private val photoRepository: PhotoRepository
) {
  suspend operator fun invoke(start: Int, limit: Int): List<Photo> {
    return photoRepository.getPhotos(start = start, limit = limit)
  }
}