package com.calendar.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calendar.model.Schedule
import com.calendar.util.LunarCalendarUtil
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekdaysHeader() {
    val weekdays = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEach { dayOfWeek ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateCell(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    scheduleCount: Int,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val date = calendarDay.date
    val lunarDate = LunarCalendarUtil.formatLunarDate(date)
    
    // 确定日期文本颜色
    val textColor = when {
        !calendarDay.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        calendarDay.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    // 确定选中状态的背景色
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        calendarDay.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    // 确定选中状态的文本颜色
    val selectedTextColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        textColor
    }
    
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .clickable {
                onDateClick(date)
            }
            .background(backgroundColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = if (calendarDay.isToday) FontWeight.Bold else FontWeight.Normal,
                color = selectedTextColor,
                textAlign = TextAlign.Center
            )
            
            // 农历日期
            Text(
                text = lunarDate,
                fontSize = 8.sp,
                color = if (isSelected) selectedTextColor.copy(alpha = 0.8f) else textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            // 日程指示器
            if (scheduleCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}