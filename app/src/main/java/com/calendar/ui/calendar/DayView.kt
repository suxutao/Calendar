package com.calendar.ui.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DayView(){
    Text(
        text = "DayView",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}