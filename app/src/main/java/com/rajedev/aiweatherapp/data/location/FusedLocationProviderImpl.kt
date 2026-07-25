package com.rajedev.aiweatherapp.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rajedev.aiweatherapp.domain.model.LatLon
import com.rajedev.aiweatherapp.domain.repository.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FusedLocationProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationProvider {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    // One-shot fetch, not a location stream - caller (an explicit "use current location" action)
    // must have already confirmed the permission before invoking this.
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LatLon> {
        if (!isLocationServicesEnabled()) {
            return Result.failure(LocationServicesDisabledException())
        }
        return fetchCurrentLocation()
    }

    private fun isLocationServicesEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private suspend fun fetchCurrentLocation(): Result<LatLon> = suspendCancellableCoroutine { continuation ->
        val cancellationSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(Result.success(LatLon(location.latitude, location.longitude)))
                } else {
                    continuation.resume(Result.failure(LocationUnavailableException()))
                }
            }
            .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        continuation.invokeOnCancellation { cancellationSource.cancel() }
    }
}
