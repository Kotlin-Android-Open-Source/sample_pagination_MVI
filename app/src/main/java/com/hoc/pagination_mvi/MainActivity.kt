package com.hoc.pagination_mvi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.hoc.pagination_mvi.data.ApiService
import com.hoc.pagination_mvi.data.BASE_URL
import com.hoc.pagination_mvi.data.PhotoRepositoryImpl
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.CoroutinesDispatchersProviderImpl
import com.hoc.pagination_mvi.domain.dispatchers_schedulers.RxSchedulerProviderImpl
import com.hoc.pagination_mvi.domain.usecase.GetPhotosUseCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private fun getRetrofit(client: OkHttpClient): Retrofit {
  return Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(
      MoshiConverterFactory.create(
        Moshi
          .Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      )
    )
    .build()
}

private fun getOkHttpClient(): OkHttpClient {
  return OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .apply {
      if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor()
          .setLevel(HttpLoggingInterceptor.Level.BODY)
          .let(::addInterceptor)
      }
    }
    .build()
}

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val dispatchersProvider = CoroutinesDispatchersProviderImpl(RxSchedulerProviderImpl())

    CoroutineScope(dispatchersProvider.ui).launch {
      val photos = GetPhotosUseCase(
        PhotoRepositoryImpl(
          ApiService(
            getRetrofit(
              getOkHttpClient()
            )
          ),
          dispatchersProvider
        )
      )(start = 0, limit = 5)

      Toast.makeText(this@MainActivity, "Photos: $photos", LENGTH_SHORT).show()
    }
  }
}
