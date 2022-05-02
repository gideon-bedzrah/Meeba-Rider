package com.berthstudios.meeba.Api

data class DirectionsModel(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)