package com.rajedev.aiweatherapp.presentation.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rajedev.aiweatherapp.R

@Composable
fun StaleDataBanner(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = Icons.Filled.CloudOff, contentDescription = null)
            Text(
                text = stringResource(R.string.stale_data_banner_message),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
