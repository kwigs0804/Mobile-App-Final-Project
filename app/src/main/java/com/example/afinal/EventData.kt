package com.example.afinal

import com.google.gson.annotations.SerializedName

data class EventData(
    val _embedded: _Embedded
)

data class _Embedded(
    val events: List<Events>
)

data class Events(
    val id: String,
    val name: String,
    val url: String,
    val images: List<Images>,
    val dates: Dates,
    val priceRanges: List<Price>?,
    val _embedded: EmbeddedVenue
)

data class Images(
    val url: String,
    val width: Int,
    val height: Int
)

data class Price(
    val min: Double?,
    val max: Double?
)

data class Dates(
    val start: Start
)

data class Start(
    val localDate: String,
    val localTime: String?
)

data class EmbeddedVenue(
    val venues: List<Venue>
)

data class Venue(
    val name: String,
    val city: City,
    val state: State,
    val address: Address,
    val location: Location
)

data class Address(
    val line1: String
)

data class Location(
    @SerializedName("longitude") val lon:String?,
    @SerializedName("latitude") val lat:String?
)

data class State(
    val name: String
)

data class City(
    val name: String
)
