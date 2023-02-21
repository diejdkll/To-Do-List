package com.example.to_dolist

import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class User(
    val id: Int, val username: String, val token: String
)

interface RetrofitService {
    // Login
    @POST("user/login/")
    @FormUrlEncoded
    fun login(
        @FieldMap params: HashMap<String, Any>
    ): Call<User>
}