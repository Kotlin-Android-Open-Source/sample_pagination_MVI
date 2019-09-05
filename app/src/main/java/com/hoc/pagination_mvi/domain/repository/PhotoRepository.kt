package com.hoc.pagination_mvi.domain.repository

import com.hoc.pagination_mvi.domain.entity.Photo

interface PhotoRepository {
  suspend fun getPhotos(
    start: Int,
    limit: Int
  ): List<Photo>
}