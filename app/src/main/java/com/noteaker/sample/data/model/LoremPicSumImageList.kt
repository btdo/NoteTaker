package com.noteaker.sample.data.model

class LoremPicSumImageList : ArrayList<LoremPicSumImageList.LoremPicSumImageItem>(){
    data class LoremPicSumImageItem(
        val author: String? = null,
        val download_url: String? = null,
        val height: Int? = null,
        val id: String? = null,
        val url: String? = null,
        val width: Int? = null
    )
}