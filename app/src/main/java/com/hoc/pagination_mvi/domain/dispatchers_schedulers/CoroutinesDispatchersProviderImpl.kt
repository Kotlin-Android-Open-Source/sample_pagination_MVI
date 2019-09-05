package com.hoc.pagination_mvi.domain.dispatchers_schedulers

import com.hoc.pagination_mvi.di.ApplicationScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import javax.inject.Inject

@ApplicationScope
class CoroutinesDispatchersProviderImpl @Inject constructor(
  rxSchedulerProvider: RxSchedulerProvider
) : CoroutinesDispatchersProvider {
  override val io: CoroutineDispatcher = rxSchedulerProvider.io.asCoroutineDispatcher()
  override val ui: CoroutineDispatcher = rxSchedulerProvider.ui.asCoroutineDispatcher()
}