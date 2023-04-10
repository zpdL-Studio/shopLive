package com.zpdl.shoplive.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zpdl.shoplive.api.MarbleData
import com.zpdl.shoplive.service.FavoriteService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavoriteViewModel(private val favoriteService: FavoriteService) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                FavoriteViewModel(
                    favoriteService = FavoriteService.getInstance(application),
                )
            }
        }
    }

    private val _uiState =
        MutableStateFlow(FavoriteUiState(favorite = null))
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    private var compositeDisposable = CompositeDisposable()

    init {
        favoriteService.favoritesObservable.subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe { favorites ->
            _uiState.update {
                it.copy(favorite = favorites)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun toggleFavorite(marbleData: MarbleData) {
        favoriteService.toggleFavorite(marbleData)
    }
}
