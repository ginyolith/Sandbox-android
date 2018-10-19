package jp.ginyolith.sandbox

import io.reactivex.*
import io.reactivex.subscribers.DisposableSubscriber
import org.junit.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class RxJavaErrorHandlingTest {

    @Test
    fun testRetry() {
        val flowable = Flowable.create(FlowableOnSubscribe<Int> {
            println("Flowableの処理開始")
            for (i in 1..3) {
                if (i == 2) {
                    throw Exception("例外発生")
                }
                it.onNext(i)
            }

            it.onComplete()

            println("Flowableの処理終了")
        }, BackpressureStrategy.BUFFER)
            .doOnSubscribe { println("flowable: doOnSubscribe") }
            .retry(2)

        // 特定のケースでのみretryする（エラーコードやExceptionで判別）事も出来る
        Observable.range(1, 2)
                .retry { t1, t2 ->
                    return@retry t2 is IllegalStateException
                }
        flowable.subscribe(object : Subscriber<Int> {
            override fun onComplete() {
                println("完了")
            }

            override fun onSubscribe(s: Subscription?) {
                println("subscriber : onSubscribe")
                s?.request(Long.MAX_VALUE)
            }

            override fun onNext(t: Int?) {
                println("data=$t")
            }

            override fun onError(t: Throwable?) {
                println("エラー=$t")
            }
        })
    }

    @Test
    fun testErrorHandlingWithAltData() {
        Flowable.just(1, 3, 5, 0, 2, 4)
                // 数を割る
                .map { it -> 100 / it }
                // エラーの時は0を返す
                .onErrorReturn { 0 }
                .subscribe(object : DisposableSubscriber<Int>() {
                    override fun onComplete() {
                        println("完了")
                    }

                    override fun onNext(t: Int?) {
                        println("data=$t")
                    }

                    override fun onError(t: Throwable?) {
                        println("error=$t")
                    }
                })

    }
}