package com.example.afinal

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketMaster {
    @GET("events.json")
    fun getEvents(
        @Query("apikey") apiKey:String,
        @Query("keyword") keyword: String,
        @Query("city") city: String,
        @Query("sort") sort: String,
        @Query("size") size: Int
    ): Call<EventData>
}