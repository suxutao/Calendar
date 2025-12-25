package com.calendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calendar.R
import com.calendar.constants.ViewMode

@Composable
fun CalendarActionBar(
    currentMode: ViewMode,
    onModeChanged: (ViewMode) -> Unit,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            text = stringResource(id = R.string.month_view),
            isSelected = currentMode == ViewMode.MONTH,
            onClick = { onModeChanged(ViewMode.MONTH) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        ActionButton(
            text = stringResource(id = R.string.week_view),
            isSelected = currentMode == ViewMode.WEEK,
            onClick = { onModeChanged(ViewMode.WEEK) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        ActionButton(
            text = stringResource(id = R.string.day_view),
            isSelected = currentMode == ViewMode.DAY,
            onClick = { onModeChanged(ViewMode.DAY) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        ScheduleButton(
            onClick = onScheduleClick,
            isSelected = currentMode == ViewMode.SCHEDULE,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ScheduleButton(
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = "日程",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
