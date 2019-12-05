package com.hoc.pagination_mvi.domain.usecase

import com.hoc.pagination_mvi.di.ApplicationScope
import com.hoc.pagination_mvi.domain.entity.Photo
import com.hoc.pagination_mvi.domain.entity.Post
import com.hoc.pagination_mvi.domain.repository.PhotoRepository
import com.hoc.pagination_mvi.domain.repository.PostRepository
import javax.inject.Inject

@ApplicationScope
class GetPostsUseCase @Inject constructor(
  private val postRepository: PostRepository
) {
  suspend operator fun invoke(start: Int, limit: Int): List<Post> {
    return postRepository.getPosts(start = start, limit = limit)
  }
}