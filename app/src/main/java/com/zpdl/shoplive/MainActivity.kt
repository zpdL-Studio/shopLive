package com.zpdl.shoplive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.zpdl.shoplive.ui.FavoriteScreen
import com.zpdl.shoplive.ui.SearchScreen
import com.zpdl.shoplive.ui.theme.ShopLiveTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopLiveTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Home()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun Home() {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigation(currentTab = HomeTab.values()[pagerState.currentPage]) {
                coroutineScope.launch {
                    pagerState.scrollToPage(page = it.ordinal)
                }
            }
        }
    ) {
        HorizontalPager(
            modifier = Modifier.padding(it),
            count = HomeTab.values().size,
            state = pagerState
        ) { page ->
            when (HomeTab.values()[page]) {
                HomeTab.SEARCH -> SearchScreen()
                HomeTab.FAVORITE -> FavoriteScreen()
            }
        }
    }
}

@Composable
fun BottomNavigation(currentTab: HomeTab, onChanged: (tab: HomeTab) -> Unit) {
    NavigationBar {
        HomeTab.values().forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = {
                    onChanged(tab)
                },
                label = {
                    Text(
                        text = tab.label,
                    )
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = "${tab.label} Icon",
                    )
                }
            )
        }
    }
}

enum class HomeTab(val label: String, val icon: ImageVector) {
    SEARCH("검색", Icons.Default.Search),
    FAVORITE("즐겨찾기", Icons.Default.Favorite)
}
