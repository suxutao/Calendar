package com.calendar.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.calendar.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    onScheduleClick:()-> Unit,
    onSettingsClick:()-> Unit
) {
    TopAppBar(
        title = { Text(text = "我的日历") }, // 左侧"日历"文字
        actions = {
            // 右侧第一个图标：日程
            IconButton(onClick = onScheduleClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(id = R.string.schedule)
                )
            }
            // 右侧第二个图标：设置
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}