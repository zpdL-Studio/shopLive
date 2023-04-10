package com.zpdl.shoplive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zpdl.shoplive.api.MarbleData
import com.zpdl.shoplive.ui.composable.MarbleComposable


@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = viewModel(
        factory = FavoriteViewModel.Factory
    )
) {
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center
    ) {
        Text("Favorite", modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxWidth())
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            val uiState by viewModel.uiState.collectAsState()
            val favorite = uiState.favorite
            if(favorite == null) {
                Box(
                    modifier = Modifier.background(color = Color.White.copy(alpha = 0.5f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(all = 16.dp)
                    )
                }
            } else {
                FavoriteList(
                    viewModel = viewModel,
                    list = favorite
                )
            }
        }
    }
}

@Composable
private fun FavoriteList(
    viewModel: FavoriteViewModel,
    list: MutableList<MarbleData>,
) {
    if (list.isNotEmpty()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            columns = GridCells.Fixed(count = 2)
        ) {
            items(count = list.size, key = {
                list[it].id
            }, contentType = { 0 }, itemContent = {
                val item = list[it]
                MarbleComposable(favorite = false, data = item, onClick = {
                    viewModel.toggleFavorite(item)
                })
            })
        }
    } else {
        Text("No results found")
    }
}