package com.example.zivaministries.network

import com.example.zivaministries.models.MagazineResponse
import com.example.zivaministries.models.VideoResponse
import retrofit2.http.GET

interface ApiService {
    // Magazines
    @GET("Elkesh1/7459f92fad9d36ebecff5879de26892f/raw/7136093b9943b50599eaa449af4bd246e1b05367/gistfile1.txt")
    suspend fun getMagazines() : MagazineResponse

    // Audio
    @GET("bulltammyd/fbb29206787548dbe85e3423c9862cd4/raw/a2ca75f97610b152873299be42d5e51a2812ddd1/audio.json")
    suspend fun getVideos(): VideoResponse
}