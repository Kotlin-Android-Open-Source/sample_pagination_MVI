package com.hoc.pagination_mvi.domain.dispatchers_schedulers

import com.hoc.pagination_mvi.di.ApplicationScope
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@ApplicationScope
class RxSchedulerProviderImpl @Inject constructor() : RxSchedulerProvider {
  override val io = Schedulers.io()
  override val ui: Scheduler = AndroidSchedulers.mainThread()
}