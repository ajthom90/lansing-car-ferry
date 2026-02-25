package dev.ajthom.lansingferry.ui.screens

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
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ajthom.lansingferry.R
import dev.ajthom.lansingferry.shared.model.FerryInfo
import dev.ajthom.lansingferry.shared.model.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ferryInfo: FerryInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.home_title)) })

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

                // Facebook notice
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0),
                    ),
                    modifier = Modifier.fillMaxWidth().clickable {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.facebook)))
                        } catch (_: android.content.ActivityNotFoundException) {
                        }
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = stringResource(R.string.home_facebook_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                // Quick info cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    InfoCard(
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.home_crossing),
                        value = stringResource(R.string.home_crossing_value, ferryInfo.schedule.crossingDurationMinutes),
                        modifier = Modifier.weight(1f),
                    )
                    InfoCard(
                        icon = Icons.Default.DirectionsCar,
                        title = stringResource(R.string.home_capacity),
                        value = stringResource(R.string.home_capacity_value, ferryInfo.schedule.approximateCapacity),
                        modifier = Modifier.weight(1f),
                    )
                    InfoCard(
                        icon = Icons.Default.MoneyOff,
                        title = stringResource(R.string.home_cost),
                        value = stringResource(R.string.home_cost_value),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Locations
                Text(stringResource(R.string.home_locations_section), style = MaterialTheme.typography.titleMedium)

                LocationRow(label = stringResource(R.string.home_location_iowa), location = ferryInfo.locations.iowa) {
                    val uri = Uri.parse("geo:${ferryInfo.locations.iowa.latitude},${ferryInfo.locations.iowa.longitude}?q=${ferryInfo.locations.iowa.latitude},${ferryInfo.locations.iowa.longitude}(${Uri.encode(ferryInfo.locations.iowa.name)})")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
                LocationRow(label = stringResource(R.string.home_location_wisconsin), location = ferryInfo.locations.wisconsin) {
                    val uri = Uri.parse("geo:${ferryInfo.locations.wisconsin.latitude},${ferryInfo.locations.wisconsin.longitude}?q=${ferryInfo.locations.wisconsin.latitude},${ferryInfo.locations.wisconsin.longitude}(${Uri.encode(ferryInfo.locations.wisconsin.name)})")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }

                // Links
                Text(stringResource(R.string.home_resources_section), style = MaterialTheme.typography.titleMedium)

                LinkRow(icon = Icons.Default.Link, label = stringResource(R.string.home_link_facebook)) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.facebook)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
                LinkRow(icon = Icons.Default.Traffic, label = stringResource(R.string.home_link_traffic)) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.traffic)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }
                LinkRow(icon = Icons.Default.Language, label = stringResource(R.string.home_link_iowadot)) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.iowadot)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        // No app available to handle the intent
                    }
                }

                // Disclaimer
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cdn.jsdelivr.net/gh/ajthom90/lansing-car-ferry@main/data/privacy-policy.html")))
                            } catch (_: android.content.ActivityNotFoundException) {}
                        },
                    )
                    Text(
                        text = stringResource(R.string.terms_of_use),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cdn.jsdelivr.net/gh/ajthom90/lansing-car-ferry@main/data/terms-of-use.html")))
                            } catch (_: android.content.ActivityNotFoundException) {}
                        },
                    )
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
                Icon(Icons.Default.Map, contentDescription = stringResource(R.string.home_open_in_maps), tint = MaterialTheme.colorScheme.primary)
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
