package uz.nurlibaydev.workmanagerguide.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface FileApi {

    @GET("/wp-content/uploads/2021/11/StudioCompose10.jpg")
    suspend fun downloadImage(): Response<ResponseBody>

    companion object {
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://molo17.com")
                .build()
                .create(FileApi::class.java)
        }
    }
}