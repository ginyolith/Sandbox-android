package jp.ginyolith.sandbox

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

class RxJavaResourceManageTest {
    @Test
    fun testBackpressure() {
        val flowable = Flowable.interval(10L, TimeUnit.MICROSECONDS)
                .doOnNext { it -> println("emit:$it") }
                .observeOn(Schedulers.computation())
                .subscribe(object : Subscriber<Long> {
                    override fun onComplete() {
                        println("完了")
                    }

                    override fun onSubscribe(s: Subscription?) {
                        s?.request(Long.MAX_VALUE)
                    }

                    override fun onNext(t: Long?) {
                        // 1000millisec待ってから処理を行う
                        try {
                            println("wating.....")
                            Thread.sleep(3000L)
                        } catch (e : Exception) {
                            e.printStackTrace()
                        }

                        println("received: $t")
                    }

                    override fun onError(t: Throwable?) {
                        println("完了")
                    }

                })

        Thread.sleep(5000L)

    }
}