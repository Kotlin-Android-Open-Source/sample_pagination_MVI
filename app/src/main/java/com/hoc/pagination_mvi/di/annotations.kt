package com.hoc.pagination_mvi.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import javax.inject.Scope
import kotlin.reflect.KClass

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@MustBeDocumented
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)