package com.zpdl.shoplive.ui

import com.zpdl.shoplive.api.MarbleResponse

data class SearchUiState(
    val marbleResponse: MarbleResponse?,
    val favorite: MutableSet<Int>
)
