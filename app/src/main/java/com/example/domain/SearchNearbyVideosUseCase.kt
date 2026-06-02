package com.example.domain

import com.example.model.VideoPost
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SearchNearbyVideosUseCase {

    // Coordinate mapping for Sumedang subdistricts to calculate simulated distance in KM
    private val lemburCoordinates = mapOf(
        "Situraja" to Pair(-6.883, 107.95),
        "Cimalaka" to Pair(-6.816, 107.933),
        "Ganeas" to Pair(-6.866, 107.933),
        "Sumedang Utara" to Pair(-6.833, 107.916),
        "Sumedang Selatan" to Pair(-6.866, 107.916),
        "Tanjungsari" to Pair(-6.900, 107.800),
        "Jatinangor" to Pair(-6.930, 107.760)
    )

    /**
     * Search and sort video posts based on proximity to a target subdistrict (Lembur).
     * Returns a list of video posts sorted by distance, paired with the distance in km (Double).
     */
    fun findNearestVideos(
        posts: List<VideoPost>,
        targetLembur: String
    ): List<Pair<VideoPost, Double>> {
        // Find reference coordinates for user's subdistrict. Default to Cimalaka if not found.
        val userCoords = lemburCoordinates[targetLembur] ?: Pair(-6.816, 107.933)

        return posts.map { post ->
            val postCoords = lemburCoordinates[post.lembur] ?: Pair(-6.816, 107.933)
            val distance = calculateDistanceInKm(
                userCoords.first, userCoords.second,
                postCoords.first, postCoords.second
            )
            Pair(post, distance)
        }.sortedBy { it.second }
    }

    private fun calculateDistanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
