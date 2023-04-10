package com.zpdl.shoplive.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zpdl.shoplive.api.MarbleData

@Composable
fun MarbleComposable(
    favorite: Boolean = false,
    data: MarbleData,
    onClick: () -> Unit,
) {
    Surface(
        color = if (favorite) Color.LightGray else MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(all = 8.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f),
                onClick = onClick
            ) {
                data.thumbnail?.let {
                    val url = "${data.thumbnail.path}.${data.thumbnail.extension}"
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                data.name,
                maxLines = 1,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = data.description,
                maxLines = 3,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}