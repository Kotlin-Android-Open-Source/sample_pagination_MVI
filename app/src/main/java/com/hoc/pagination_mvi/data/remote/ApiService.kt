package com.hoc.pagination_mvi.data.remote

import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "http://jsonplaceholder.typicode.com/"

interface ApiService {
  @GET("photos")
  suspend fun getPhotos(
    @Query("_start") start: Int,
    @Query("_limit") limit: Int
  ): List<PhotoResponse>

  companion object {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<ApiService>()
  }
}