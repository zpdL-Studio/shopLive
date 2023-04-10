package com.zpdl.shoplive.api

import com.google.gson.GsonBuilder
import com.zpdl.shoplive.BuildConfig
import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigInteger
import java.security.MessageDigest


class MarbleApi {

    companion object {
        private const val BASE_URL = "https://gateway.marvel.com:443/"

        private fun getApiService(): MarbleApiInterface {
            return getInstance().create(MarbleApiInterface::class.java)
        }

        private fun getInstance(): Retrofit {
            val gson = GsonBuilder().setLenient().create()
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()).build()
        }

        private fun getMarbleKey(ts: String): Observable<MarbleApiKey> {
            return Observable.fromCallable {
                val md = MessageDigest.getInstance("MD5")
                val message = "$ts${BuildConfig.MARBLE_API_PRIVATE_KEY}${BuildConfig.MARBLE_API_PUBLIC_KEY}"
                MarbleApiKey(
                    ts = ts,
                    apikey = BuildConfig.MARBLE_API_PUBLIC_KEY,
                    hash = BigInteger(1, md.digest(message.toByteArray())).toString(16)
                        .padStart(32, '0'),
                )
            }
        }

        fun getCharacters(
            ts: String,
            nameStartsWith: String? = null,
            offset: Int? = null
        ): Observable<MarbleResponse> {
            return getMarbleKey(ts).flatMap {
                getApiService().getCharacters(
                    ts = it.ts,
                    apikey = it.apikey,
                    hash = it.hash,
                    nameStartsWith = nameStartsWith,
                    offset = offset
                )
            }.map {
                MarbleResponse(ts = ts, keyword = nameStartsWith, data = it.data)
            }
        }
    }
}

interface MarbleApiInterface {
    @GET("v1/public/characters")
    fun getCharacters(
        @Query("ts") ts: String,
        @Query("apikey") apikey: String,
        @Query("hash") hash: String,
        @Query("nameStartsWith") nameStartsWith: String?,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int? = null
    ): Observable<MarbleBody>
}

data class MarbleApiKey(
    val ts: String,
    val apikey: String,
    val hash: String,
)

data class MarbleResponse(
    val ts: String, val keyword: String?, val data: MarbleDataSet?
)

data class MarbleBody(
    val data: MarbleDataSet
)

data class MarbleDataSet(
    val offset: Int, val limit: Int, val total: Int, val count: Int, val results: List<MarbleData>
)

data class MarbleData(
    val id: Int, val name: String, val description: String, val thumbnail: MarbleThumbnail?
)

data class MarbleThumbnail(
    val path: String,
    val extension: String,
)