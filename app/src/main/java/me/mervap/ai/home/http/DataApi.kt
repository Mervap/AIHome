package me.mervap.ai.home.http

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

fun SharedPreferences.baseUrl(): String {
  return getString("url", "") ?: ""
}

val Context.sharedPreferences: SharedPreferences
  get() {
    return PreferenceManager.getDefaultSharedPreferences(this) ?: error("No shared preferences")
  }

@OptIn(ExperimentalSerializationApi::class)
fun Context.client(): DataAPI {
  val baseUrl = sharedPreferences.baseUrl()
  val retrofit = Retrofit.Builder()
    .baseUrl("http://$baseUrl")
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
    .build()
  return retrofit.create(DataAPI::class.java)
}

@OptIn(ExperimentalSerializationApi::class)
fun client(baseUrl: String): DataAPI {
  val retrofit = Retrofit.Builder()
    .baseUrl("http://$baseUrl")
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
    .build()
  return retrofit.create(DataAPI::class.java)
}

interface DataAPI {

  @GET("/meteo2/get_data.php")
  fun getData(): Call<Result<WeatherInfo>>

  @GET("/meteo2/get_pressure_data.php")
  fun getPressureData(): Call<Result<List<PressureInfo>>>

  @GET("/meteo2/get_last_24_hour_data.php")
  fun getLastData(): Call<Result<List<FullInfo>>>

  @GET("/meteo2/get_custom_period_data.php")
  fun getCustomPeriodData(
    @Query("dateTimeFrom") dateTimeFrom: String,
    @Query("dateTimeTo") dateTimeTo: String,
  ): Call<Result<List<FullInfo>>>

  @GET("/meteo2/get_flap_data.php")
  fun getFlapData(): Call<Result<FlapInfo>>

  @GET("/meteo2/flap_modes.php")
  fun putFlapData(
    @Query("trigger_mode") trigger_mode: Int,
    @Query("main_vent_flap") main_vent_flap: Int,
    @Query("room_vent_flap") room_vent_flap: Int,
  ): Call<Unit>
}

@Serializable(with = ResultSerializer::class)
sealed class Result<T> {
  data class Success<T>(val data: T) : Result<T>()
  data class Failure<T>(val message: String) : Result<T>()
}

@Serializable
data class WeatherInfo(
  val tInside: Double,
  val tOutside: Double,
  val pressure: Double,
  val humidity: Double,
)

@Serializable
data class PressureInfo(val dateTime: String, val pressure: Double)

@Serializable
data class FullInfo(
  val dateTime: String,
  val tInside: Double,
  val tOutside: Double,
  val pressure: Double,
  val humidity: Double,
  val humidityOutside: Double,
  val mainVentFlap: Double,
  val roomVentFlap: Double,
)

@Serializable
data class FlapInfo(val mainVentFlap: Double, val roomVentFlap: Double, val triggerMode: Int)


private class ResultSerializer<T>(private val dataSerializer: KSerializer<T>) :
  KSerializer<Result<T>> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Result") {
    element<Int>("success")
    element<String?>("message", isOptional = true)
    element("data", dataSerializer.descriptor, isOptional = true)
  }

  override fun serialize(encoder: Encoder, value: Result<T>) {
    val structuredEncoder = encoder.beginStructure(descriptor)
    when (value) {
      is Result.Failure<T> -> {
        structuredEncoder.encodeIntElement(descriptor, 0, 0)
        structuredEncoder.encodeStringElement(descriptor, 1, value.message)
      }
      is Result.Success<T> -> {
        structuredEncoder.encodeIntElement(descriptor, 0, 1)
        structuredEncoder.encodeSerializableElement(descriptor, 2, dataSerializer, value.data)
      }
    }
    structuredEncoder.endStructure(descriptor)
  }

  override fun deserialize(decoder: Decoder): Result<T> {
    val composite = decoder.beginStructure(descriptor)

    var success: Int? = null
    var message: String? = null
    var data: T? = null

    while (true) {
      when (composite.decodeElementIndex(descriptor)) {
        CompositeDecoder.DECODE_DONE -> break
        0 -> success = composite.decodeIntElement(descriptor, 0)
        1 -> message = composite.decodeStringElement(descriptor, 1)
        2 -> data = composite.decodeSerializableElement(descriptor, 2, dataSerializer)
      }
    }
    composite.endStructure(descriptor)

    return when (success) {
      0 -> Result.Failure(message ?: "")
      1 -> Result.Success(data ?: error(""))
      else -> error("Success can be 0 or 1")
    }
  }
}