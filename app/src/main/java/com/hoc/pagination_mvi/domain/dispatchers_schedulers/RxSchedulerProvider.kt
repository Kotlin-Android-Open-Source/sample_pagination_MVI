package com.hoc.pagination_mvi.domain.dispatchers_schedulers

import io.reactivex.Scheduler

interface RxSchedulerProvider {
  val io: Scheduler
  val ui: Scheduler
}