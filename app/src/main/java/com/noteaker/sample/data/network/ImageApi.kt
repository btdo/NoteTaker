package com.noteaker.sample.data.network

import com.noteaker.sample.data.model.LoremPicSumImageList
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApi {
    @GET("/v2/list")
    suspend fun images(@Query("page") page: Int, @Query("limit") limit: Int): LoremPicSumImageList
}