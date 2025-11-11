package com.calendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.calendar.core.constants.ViewMode
import com.calendar.R


@Composable
fun ViewModeToggleRow(
    currentMode: ViewMode,
    onModeChanged: (ViewMode) -> Unit,
    modifier: Modifier = Modifier // 添加modifier参数，默认值为Modifier
) {
    // 在Row中应用modifier
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // 年视图按钮
        Button(
            onClick = { onModeChanged(ViewMode.YEAR) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentMode == ViewMode.YEAR) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (currentMode == ViewMode.YEAR) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        ) {
            Text(text = stringResource(id = R.string.year_view))
        }

        Spacer(modifier = Modifier.weight(0.05f))

        // 月视图按钮（其余按钮代码不变）
        Button(
            onClick = { onModeChanged(ViewMode.MONTH) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentMode == ViewMode.MONTH) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (currentMode == ViewMode.MONTH) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        ) {
            Text(text = stringResource(id = R.string.month_view))
        }

        Spacer(modifier = Modifier.weight(0.05f))

        // 周视图按钮
        Button(
            onClick = { onModeChanged(ViewMode.WEEK) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentMode == ViewMode.WEEK) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (currentMode == ViewMode.WEEK) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        ) {
            Text(text = stringResource(id = R.string.week_view))
        }

        Spacer(modifier = Modifier.weight(0.05f))

        // 日视图按钮
        Button(
            onClick = { onModeChanged(ViewMode.DAY) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentMode == ViewMode.DAY) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (currentMode == ViewMode.DAY) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        ) {
            Text(text = stringResource(id = R.string.day_view))
        }
    }
}