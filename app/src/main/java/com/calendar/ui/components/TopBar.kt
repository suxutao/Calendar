package com.calendar.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.calendar.R


// 顶部导航栏组件（更新后）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    onScheduleClick: () -> Unit,
    onDateJumpClick: () -> Unit, // 日期跳转回调
    onImportExportClick: () -> Unit, // 导入导出回调
    onSettingsClick: () -> Unit, // 原设置回调
    modifier: Modifier = Modifier
) {
    // 控制弹窗显示/隐藏的状态
    var isMenuExpanded by remember { mutableStateOf(false) }
    // 记录弹窗锚点位置（用于对齐按钮）
    val menuAnchor = remember { mutableStateOf<Rect?>(null) }

    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            // 右侧第一个按钮：日程（不变）
            IconButton(onClick = onScheduleClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(id = R.string.schedule)
                )
            }

            // 右侧第二个按钮：更多（触发弹窗）
            IconButton(
                onClick = { isMenuExpanded = true },
                // 记录按钮位置，让弹窗对齐
                modifier = Modifier.onGloballyPositioned {
                    menuAnchor.value = it.boundsInWindow()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert, // 三个点的“更多”图标
                    contentDescription = stringResource(id = R.string.more_options)
                )
            }

            // 下拉弹窗（点击更多后显示）
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }, // 点击外部关闭
                offset = DpOffset(
                    x = 0.dp,
                    y = -(menuAnchor.value?.height?:48f).dp
                )
            ) {
                // 弹窗选项1：日期跳转
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.date_jump)) },
                    onClick = {
                        onDateJumpClick()
                        isMenuExpanded = false // 点击后关闭弹窗
                    }
                )

                // 弹窗选项2：日程导入与导出
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.import_export)) },
                    onClick = {
                        onImportExportClick()
                        isMenuExpanded = false
                    }
                )

                // 弹窗选项3：设置
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.settings)) },
                    onClick = {
                        onSettingsClick()
                        isMenuExpanded = false
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}