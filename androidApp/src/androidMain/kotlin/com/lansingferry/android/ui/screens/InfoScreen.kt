package com.lansingferry.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lansingferry.shared.model.FerryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(ferryInfo: FerryInfo) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Ferry Info") })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Schedule Section
            SectionHeader("Schedule")

            InfoRow(
                label = "Wisconsin Departure",
                value = formatRange(
                    ferryInfo.schedule.regularHours.wisconsinDeparture.start,
                    ferryInfo.schedule.regularHours.wisconsinDeparture.end,
                ),
            )
            InfoRow(
                label = "Iowa Departure",
                value = formatRange(
                    ferryInfo.schedule.regularHours.iowaDeparture.start,
                    ferryInfo.schedule.regularHours.iowaDeparture.end,
                ),
            )
            InfoRow(
                label = "Holiday Hours",
                value = formatRange(
                    ferryInfo.schedule.holidayHours.start,
                    ferryInfo.schedule.holidayHours.end,
                ),
            )

            if (ferryInfo.schedule.commuterPriorityWindows.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Commuter Priority",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(8.dp))
                        ferryInfo.schedule.commuterPriorityWindows.forEach { window ->
                            Text(
                                text = formatRange(window.start, window.end),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Vehicles Section
            SectionHeader("Vehicles")

            ExpandableList(
                title = "Allowed",
                items = ferryInfo.vehicleRestrictions.allowed,
                icon = Icons.Default.Check,
                iconTint = Color(0xFF4CAF50),
            )

            ExpandableList(
                title = "Prohibited",
                items = ferryInfo.vehicleRestrictions.prohibited,
                icon = Icons.Default.Close,
                iconTint = Color(0xFFF44336),
            )

            Spacer(Modifier.height(8.dp))

            // Size Limits Section
            SectionHeader("Size Limits")

            val sizeLimits = ferryInfo.vehicleRestrictions.sizeLimits
            InfoRow(label = "Height", value = "${sizeLimits.heightFeet} ft")
            InfoRow(label = "Length", value = "${sizeLimits.lengthFeet} ft")
            InfoRow(label = "Width", value = sizeLimits.widthFeetInches)
            InfoRow(label = "Weight", value = "${sizeLimits.weightTons} tons")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExpandableList(
    title: String,
    items: List<String>,
    icon: ImageVector,
    iconTint: Color,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "$title (${items.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 52.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1].toIntOrNull() ?: return time
    val period = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return if (minute == 0) "$displayHour $period" else "$displayHour:${"%02d".format(minute)} $period"
}

private fun formatRange(start: String, end: String): String {
    return "${formatTime(start)} – ${formatTime(end)}"
}
