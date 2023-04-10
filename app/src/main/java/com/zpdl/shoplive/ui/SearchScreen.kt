package com.zpdl.shoplive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zpdl.shoplive.api.MarbleDataSet
import com.zpdl.shoplive.ui.composable.MarbleComposable


@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory
    )
) {
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center
    ) {
        KeywordField(viewModel = viewModel)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            val uiState by viewModel.uiState.collectAsState()
            uiState.marbleResponse?.data?.let {
                SearchList(viewModel = viewModel, marbleDataSet = it, favorite = uiState.favorite)
            }
            val loadingState by viewModel.loadingState.collectAsState()
            if (loadingState > 0) {
                Box(
                    modifier = Modifier.background(color = Color.White.copy(alpha = 0.5f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(all = 16.dp)
                    )
                }
            }
            val loadingMoreState by viewModel.loadingMoreState.collectAsState()
            if (loadingMoreState) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun KeywordField(viewModel: SearchViewModel) {
    val keywordState by viewModel.keywordState.collectAsState()
    val focusManager = LocalFocusManager.current
    TextField(
        value = keywordState,
        onValueChange = {
            viewModel.onKeywordChange(it)
        },
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
            .background(color = Color.White),
        label = {
            Text("Search")
        },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colorScheme.surface
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }),
    )
}

@Composable
private fun SearchList(
    viewModel: SearchViewModel,
    marbleDataSet: MarbleDataSet,
    favorite: MutableSet<Int>
) {
    val list = marbleDataSet.results
    if (list.isNotEmpty()) {
        val lazyListState = rememberLazyGridState()
        val bottomState = lazyListState.bottomState()
        LaunchedEffect(bottomState) {
            if (bottomState) {
                viewModel.onScrollBottom()
            }
        }

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            columns = GridCells.Fixed(count = 2)
        ) {
            items(count = list.size, key = {
                list[it].id
            }, contentType = { 0 }, itemContent = {
                val item = list[it]
                MarbleComposable(favorite = favorite.contains(item.id), data = item, onClick = {
                    viewModel.toggleFavorite(item)
                })
            })
        }
    } else {
        Text("No results found")
    }
}

@Composable
private fun LazyGridState.bottomState(): Boolean {
    return remember(this) {
        derivedStateOf {
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

                (lastVisibleItem.index + 1 == layoutInfo.totalItemsCount && lastVisibleItem.offset.y + lastVisibleItem.size.height <= viewportHeight)
            }
        }
    }.value
}