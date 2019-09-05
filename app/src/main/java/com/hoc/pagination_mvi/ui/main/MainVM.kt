package com.hoc.pagination_mvi.ui.main

import androidx.lifecycle.ViewModel
import com.hoc.pagination_mvi.di.ApplicationScope
import com.hoc.pagination_mvi.domain.usecase.GetPhotosUseCase
import javax.inject.Inject

class MainVM @Inject constructor(
  private val getPhotosUseCase: GetPhotosUseCase
) : ViewModel() {

}