package com.calendar.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 单项高度
val PICKER_ITEM_HEIGHT = 48.dp
// 可见区域的item数量（上下各2项+中间选中项）
const val VISIBLE_ITEMS_COUNT = 5

/**
 * 基础滚动选择器组件
 * @param items 选项列表
 * @param scrollState 滚动状态
 * @param itemHeight 单项高度
 * @param modifier 修饰符
 */
@Composable
fun ScrollPicker(
    items: List<String>,
    scrollState: androidx.compose.foundation.ScrollState,
    itemHeight: androidx.compose.ui.unit.Dp = PICKER_ITEM_HEIGHT,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部空白填充（让初始项能滚动到中间）
        repeat((VISIBLE_ITEMS_COUNT - 1) / 2) {
            Box(modifier = Modifier.height(itemHeight))
        }

        // 选项列表
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // 底部空白填充
        repeat((VISIBLE_ITEMS_COUNT - 1) / 2) {
            Box(modifier = Modifier.height(itemHeight))
        }
    }
}