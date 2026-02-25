package com.noteaker.sample.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noteaker.sample.R
import com.noteaker.sample.navigation.TopBarItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    topBarItems: List<TopBarItem>,
    isShowBackButton: Boolean,
    onBackClicked: () -> Unit,
    onItemClicked: (action: TopBarItem) -> Unit,
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        navigationIcon = {
            if (isShowBackButton) {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, "Back")
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = "NoteTaker",
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        },
        actions = {
            NavBarItems(items = topBarItems) { }
        })
}


@Composable
fun NavBarItems(
    items: List<TopBarItem>,
    onItemClicked: (item: TopBarItem) -> Unit
) {
    items.forEach { action ->
        val shouldDisplay by action.shouldDisplay.collectAsStateLifeCycle()
        if (shouldDisplay) {
            IconButton(onClick = {
                onItemClicked(action)
            }) {
                AppBarIcon(action = action)
            }
        }
    }
}


@Composable
fun AppBarIcon(action: TopBarItem) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            action.icon,
            contentDescription = action.contentDescription,
            tint = Color.Unspecified,
        )
        action.text?.let {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = it)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

