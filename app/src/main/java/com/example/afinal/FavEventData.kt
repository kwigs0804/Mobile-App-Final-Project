package com.example.afinal

data class FavEventData(
    val id:String="",
    val name:String="",
    val date: String="",
    val venue: String="",
    val address: String="",
    val city: String="",
    val imagesUrl: String="",
    val pricing: String="",
    val dateTime:String="",
    var favorite: Boolean=true
)