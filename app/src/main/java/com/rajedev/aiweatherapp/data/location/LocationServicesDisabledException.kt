package com.rajedev.aiweatherapp.data.location

import com.rajedev.aiweatherapp.domain.repository.LocationServicesUnavailable

class LocationServicesDisabledException :
    Exception("Location services are turned off on this device"), LocationServicesUnavailable
