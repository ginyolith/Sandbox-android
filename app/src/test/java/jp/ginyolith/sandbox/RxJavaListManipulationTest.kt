package jp.ginyolith.sandbox

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RxJavaListManipulationTest {
    @Test
    fun testVariousMethods() {
        val list = listOf("hoge", "fuga", "aa", "bb", "cc")
        val observable = Observable.fromIterable(list)

        val result = observable.filter {
                                it == "aa"
                            }.map {
                                it.capitalize()
                            }.toList().blockingGet().single()

        assertEquals("Aa", result)
    }

    @Test
    fun testAsync() {
        val service = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://www.json-generator.com/api/json/get/")
                .build().create(TestService::class.java)

        val list = service.get()

        list.subscribe({
                println(it)
                assertNotNull(it)
                assertFalse(it.isEmpty())
            }, {
                it.fillInStackTrace()
            }
        )
    }

    @Test
    fun testAsync2() {
        val service = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://www.json-generator.com/api/json/get/")
                .build().create(TestService::class.java)

        val observable = service.get()
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .doOnSuccess {
                    println("subscribe")
                    println(it)
                }
                .subscribe()

        observable
                .subscribeOn(Schedulers.io())
                .doOnSuccess {
                    println("blockingGet")
                    println(it)
                }
                .blockingGet()
    }
}