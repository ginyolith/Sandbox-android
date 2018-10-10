package jp.ginyolith.sandbox

import io.reactivex.Single
import retrofit2.http.GET

interface TestService {
    @GET("bPwdUuXKGa?indent=2")
    fun get() : Single<List<TestData>>
}

data class TestData(
        val _id : String,
        val index : Integer,
        val guid : String,
        val isActive : Boolean,
        val balance : String)