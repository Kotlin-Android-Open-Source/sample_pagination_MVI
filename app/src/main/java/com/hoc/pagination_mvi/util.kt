package com.hoc.pagination_mvi

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.subjects.Subject

@CheckResult
inline fun <T : Any, R : Any> Observable<T>.exhaustMap(crossinline transform: (T) -> Observable<out R>): Observable<R> {
  return this
    .toFlowable(BackpressureStrategy.DROP)
    .flatMap({ transform(it).toFlowable(BackpressureStrategy.MISSING) }, 1)
    .toObservable()
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Subject<T>.asObservable(): Observable<T> = this

val Context.isOrientationPortrait get() = this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun Fragment.toast(text: CharSequence) = requireContext().toast(text)