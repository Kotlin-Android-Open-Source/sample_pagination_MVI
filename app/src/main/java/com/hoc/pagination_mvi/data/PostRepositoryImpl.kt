package com.hoc.pagination_mvi.data

import com.hoc.pagination_mvi.data.remote.ApiService
import com.hoc.pagination_mvi.di.ApplicationScope
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProvider
import com.hoc.pagination_mvi.domain.entity.Post
import com.hoc.pagination_mvi.domain.repository.PostRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ApplicationScope
class PostRepositoryImpl @Inject constructor(
  private val apiService: ApiService,
  private val dispatchersProvider: CoroutinesDispatchersProvider
) : PostRepository {
  override suspend fun getPosts(start: Int, limit: Int): List<Post> {
    return withContext(dispatchersProvider.io) {
      apiService.getPosts(start = start, limit = limit).map {
        Post(
          body = it.body,
          title = it.title,
          id = it.id,
          userId = it.userId
        )
      }
    }
  }

}