package com.hoc.pagination_mvi.domain.dispatchers_schedulers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.asCoroutineDispatcher

class CoroutinesDispatchersProviderImpl(
  rxSchedulerProvider: RxSchedulerProvider,
  override val io: CoroutineDispatcher = rxSchedulerProvider.io.asCoroutineDispatcher(),
  override val ui: CoroutineDispatcher = rxSchedulerProvider.ui.asCoroutineDispatcher()
) : CoroutinesDispatchersProvider