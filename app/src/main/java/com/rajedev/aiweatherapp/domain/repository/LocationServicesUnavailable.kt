package com.rajedev.aiweatherapp.domain.repository

// Marker implemented by a Throwable indicating the failure is specifically because the device's
// location services (GPS/network location) are turned off - distinct from other location-fetch
// failures (no fix, timeout, permission issues). Domain-owned so Presentation can discriminate
// the failure type without importing a Data-layer exception type.
interface LocationServicesUnavailable
