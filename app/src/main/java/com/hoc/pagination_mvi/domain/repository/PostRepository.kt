package com.hoc.pagination_mvi.domain.repository

import com.hoc.pagination_mvi.domain.entity.Post

interface PostRepository {
  suspend fun getPosts(
    start: Int,
    limit: Int
  ): List<Post>
}