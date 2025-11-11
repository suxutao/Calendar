// components/DatePickerDialog.kt
package com.calendar.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * 日期选择器弹窗（修复AlertDialog参数不匹配问题）
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    visible: Boolean,
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    minYear: Int = 1970,
    maxYear: Int = 2100
) {
    var tempSelectedDate by remember { mutableStateOf(initialDate) }

    if (visible) {
        // 使用 Material3 AlertDialog 标准参数结构：title + text + confirmButton
        AlertDialog(
            onDismissRequest = onDismiss, // 必需参数：点击外部或返回键的回调
            modifier = Modifier.width(320.dp),
            // 标题（可选）
            title = {
                Text(
                    text = "选择日期",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            // 主要内容区域（替代原来的content参数）
            text = {
                DatePicker(
                    initialDate = initialDate,
                    onDateSelected = { tempSelectedDate = it },
                    minYear = minYear,
                    maxYear = maxYear
                )
            },
            // 确定按钮（必需参数）
            confirmButton = {
                Button(
                    onClick = {
                        onDateSelected(tempSelectedDate)
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            },
            // 取消按钮（可选）
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "取消",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            // 弹窗样式（可选，使用主题默认）
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}