package com.hoc.pagination_mvi.domain.dispatchers_schedulers

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutinesDispatchersProvider {
  val io: CoroutineDispatcher
  val ui: CoroutineDispatcher
}