package com.hoc.pagination_mvi.domain.entity

data class Photo(
  val albumId: Int,
  val id: Int,
  val thumbnailUrl: String,
  val title: String,
  val url: String
)