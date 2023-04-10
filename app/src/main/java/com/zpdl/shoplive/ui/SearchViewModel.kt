package com.zpdl.shoplive.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zpdl.shoplive.api.MarbleApi
import com.zpdl.shoplive.api.MarbleData
import com.zpdl.shoplive.api.MarbleResponse
import com.zpdl.shoplive.service.FavoriteService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class SearchViewModel(private val favoriteService: FavoriteService) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                SearchViewModel(
                    favoriteService = FavoriteService.getInstance(application),
                )
            }
        }
    }

    private val _uiState =
        MutableStateFlow(SearchUiState(marbleResponse = null, favorite = mutableSetOf()))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _keywordState = MutableStateFlow("")
    val keywordState: StateFlow<String> = _keywordState.asStateFlow()

    private val _loadingState = MutableStateFlow(0)
    val loadingState: StateFlow<Int> = _loadingState.asStateFlow()

    private val _loadingMoreState = MutableStateFlow(false)
    val loadingMoreState: StateFlow<Boolean> = _loadingMoreState.asStateFlow()

    private var compositeDisposable = CompositeDisposable()
    private val keywordSubject = BehaviorSubject.createDefault("")
    private var keywordIdentify: String = System.currentTimeMillis().toString()

    init {
        viewModelScope.launch {
            keywordState.collect {
                Log.i("KKH", "viewModelScope.launch $it")
                keywordSubject.onNext(it)
            }
        }

        compositeDisposable.add(keywordSubject.map {
            keywordIdentify = System.currentTimeMillis().toString()
            Pair(keywordIdentify, it)
        }.debounce(300, TimeUnit.MILLISECONDS).doOnNext {
            _loadingState.value = _loadingState.value + 1
        }.flatMap {
            val nameStartsWith = it.second.trim()
            if (nameStartsWith.length >= 2) {
                return@flatMap MarbleApi.getCharacters(
                    ts = it.first, nameStartsWith = nameStartsWith
                )
            }
            return@flatMap Observable.just(
                MarbleResponse(
                    ts = it.first, keyword = nameStartsWith, data = null
                )
            )
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doAfterNext {
            _loadingState.value = _loadingState.value - 1
        }.subscribe { res ->
            if (keywordIdentify == res.ts && res.data != null) {
                _uiState.update {
                    it.copy(marbleResponse = res)
                }
            }
        })

        compositeDisposable.add(MarbleApi.getCharacters(keywordIdentify).doOnSubscribe {
            _loadingState.value = _loadingState.value + 1
        }.doOnComplete {
            _loadingState.value = _loadingState.value - 1
        }.subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe { res ->
            _uiState.update {
                it.copy(marbleResponse = res)
            }
        })

        favoriteService.favoritesObservable.subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe { favorites ->
            _uiState.update {
                it.copy(favorite = favorites.map { favorite ->
                    favorite.id
                }.toMutableSet())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        keywordSubject.onComplete()
    }

    fun onKeywordChange(value: String) {
        _keywordState.value = value
    }

    fun onScrollBottom() {
        if (_loadingMoreState.value) {
            return
        }

        val marbleResponse = _uiState.value.marbleResponse ?: return
        val marbleDataSet = marbleResponse.data ?: return
        if (marbleDataSet.count < marbleDataSet.limit) {
            return
        }
        Log.i("KKH", "onScrollBottom")

        keywordIdentify = System.currentTimeMillis().toString()
        compositeDisposable.add(MarbleApi.getCharacters(
            keywordIdentify,
            nameStartsWith = marbleResponse.keyword,
            offset = marbleDataSet.offset + marbleDataSet.count
        ).doOnSubscribe {
            _loadingMoreState.value = true
        }.doOnComplete {
            _loadingMoreState.value = false
        }.subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe { res ->
            if (keywordIdentify == res.ts && res.data != null) {
                _uiState.update {
                    it.copy(marbleResponse = res.copy(data = res.data.copy(results = mutableListOf<MarbleData>().apply {
                        addAll(marbleDataSet.results)
                        addAll(res.data.results)
                    })))
                }
            }
        })
    }

    fun toggleFavorite(marbleData: MarbleData) {
        favoriteService.toggleFavorite(marbleData)
    }
}
