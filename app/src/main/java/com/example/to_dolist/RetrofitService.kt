package com.example.to_dolist

import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

class User(
    val id: Int,
    val username: String,
    val token: String
)

class ToDo(
    val id: Int,
    val content: String,
    val is_complete: Boolean,
    val created: String
)

interface RetrofitService {
    // Login
    @POST("user/login/")
    @FormUrlEncoded
    fun login(
        @FieldMap params: HashMap<String, Any>
    ): Call<User>

    // Join
    @POST("user/signup/")
    @FormUrlEncoded
    fun join(
        @FieldMap params: HashMap<String, Any>
    ): Call<User>

    // Write
    @POST("to-do/")
    @FormUrlEncoded
    fun write(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: HashMap<String, Any>
    ): Call<Any>
}