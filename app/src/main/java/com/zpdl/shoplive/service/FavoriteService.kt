package com.zpdl.shoplive.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.zpdl.shoplive.api.MarbleData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class FavoriteService private constructor(private val context: Context) {
    companion object {
        private const val favoriteKey = "FavoriteKey"

        @SuppressLint("StaticFieldLeak")
        @Volatile //인스턴스가 메인 메모리를 바로 참조하여 인스턴스 중복 생성 방지
        private var instance: FavoriteService? = null

        //동시성 체크 - 각 스레드가 동시에 실행되지 못하도록
        fun getInstance(context: Context) =
            instance ?: synchronized(FavoriteService::class.java) {
                instance ?: FavoriteService(context).also {
                    instance = it
                }
            }
    }

    private val _favoritesSubject = BehaviorSubject.createDefault<MutableList<MarbleData>>(mutableListOf())
    val favoritesObservable: Observable<MutableList<MarbleData>> = _favoritesSubject

    init {
        val prefs = context.getSharedPreferences(favoriteKey, Context.MODE_PRIVATE)
        val gson = Gson()
        val obj = prefs.getString(favoriteKey, "")?.let {
            try {
                return@let gson.fromJson(it, FavoriteModel::class.java)
            } catch (e: Exception) {
                return@let null
            }
        }
        _favoritesSubject.onNext(obj?.favorites ?: mutableListOf())
    }

    fun toggleFavorite(marbleData: MarbleData) {
        val oldFavorites = _favoritesSubject.value?.toMutableList() ?: mutableListOf()
        for (favorite in oldFavorites) {
            if (favorite.id == marbleData.id) {
                oldFavorites.remove(favorite)
                updateFavorite(oldFavorites)
                return
            }
        }

        updateFavorite(mutableListOf<MarbleData>().apply {
            add(marbleData)
            for (i in 0 until minOf(4, oldFavorites.size)) {
                add(oldFavorites[i])
            }
        })
    }

    private fun updateFavorite(favorites: MutableList<MarbleData>) {
        _favoritesSubject.onNext(favorites)
        val prefs = context.getSharedPreferences(favoriteKey, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(FavoriteModel(favorites))
        editor.putString(favoriteKey, json)
        editor.apply()
    }
}

data class FavoriteModel(
    val favorites: MutableList<MarbleData>
)