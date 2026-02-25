package com.lansingferry.android.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lansingferry.shared.model.FerryInfo
import com.lansingferry.shared.model.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ferryInfo: FerryInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Lansing Car Ferry") })

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Status banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = ferryInfo.schedule.serviceNote,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    )
                }

                // Quick info cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    InfoCard(
                        icon = Icons.Default.AccessTime,
                        title = "Crossing",
                        value = "${ferryInfo.schedule.crossingDurationMinutes} min",
                        modifier = Modifier.weight(1f),
                    )
                    InfoCard(
                        icon = Icons.Default.DirectionsCar,
                        title = "Capacity",
                        value = "~${ferryInfo.schedule.approximateCapacity} vehicles",
                        modifier = Modifier.weight(1f),
                    )
                    InfoCard(
                        icon = Icons.Default.MoneyOff,
                        title = "Cost",
                        value = "FREE",
                        modifier = Modifier.weight(1f),
                    )
                }

                // Locations
                Text("Ferry Locations", style = MaterialTheme.typography.titleMedium)

                LocationRow(label = "Iowa", location = ferryInfo.locations.iowa) {
                    val uri = Uri.parse("geo:${ferryInfo.locations.iowa.latitude},${ferryInfo.locations.iowa.longitude}?q=${ferryInfo.locations.iowa.latitude},${ferryInfo.locations.iowa.longitude}(${Uri.encode(ferryInfo.locations.iowa.name)})")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
                LocationRow(label = "Wisconsin", location = ferryInfo.locations.wisconsin) {
                    val uri = Uri.parse("geo:${ferryInfo.locations.wisconsin.latitude},${ferryInfo.locations.wisconsin.longitude}?q=${ferryInfo.locations.wisconsin.latitude},${ferryInfo.locations.wisconsin.longitude}(${Uri.encode(ferryInfo.locations.wisconsin.name)})")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }

                // Links
                Text("Resources", style = MaterialTheme.typography.titleMedium)

                LinkRow(icon = Icons.Default.Link, label = "Facebook Updates") {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.facebook)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
                LinkRow(icon = Icons.Default.Traffic, label = "511 Iowa Traffic") {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.traffic)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
                LinkRow(icon = Icons.Default.Language, label = "Iowa DOT Info") {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.iowadot)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun LocationRow(
    label: String,
    location: Location,
    onMapClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(location.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    location.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onMapClick) {
                Icon(Icons.Default.Map, contentDescription = "Open in Maps", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
        }
    }
}
