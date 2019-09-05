package com.hoc.pagination_mvi.domain.dispatchers_schedulers

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers

class RxSchedulerProviderImpl(
  override val io: Scheduler = Schedulers.io(),
  override val ui: Scheduler = AndroidSchedulers.mainThread()
) : RxSchedulerProvider {
}