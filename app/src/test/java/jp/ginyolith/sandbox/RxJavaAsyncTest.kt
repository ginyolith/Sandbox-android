package jp.ginyolith.sandbox

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxJavaAsyncTest {
    @Test
    fun testSpeed() {
        Flowable.interval(1000L, TimeUnit.MICROSECONDS)
                .doOnNext {
                    println("emit:${System.currentTimeMillis()}ミリ秒:$it")
                }
                .subscribe { Thread.sleep(2000L)}

        Thread.sleep(5000L)
    }

    @Test
    fun testSpeed2() {
        println("start")
        Flowable.just(1,2,3)
                .subscribe(object : ResourceSubscriber<Int>() {
                    override fun onNext(t: Int?) {
                        val threadName = Thread.currentThread().name
                        println("$threadName: $t")
                    }

                    override fun onError(t: Throwable?) {
                        t?.printStackTrace()
                    }

                    override fun onComplete() {
                        val threadName = Thread.currentThread().name
                        println("$threadName: 完了")
                    }

                })
        println("end")
    }

    @Test
    fun testAsync() {
        println("start")
        Flowable.interval(300L,TimeUnit.MILLISECONDS)
                .subscribe(object : ResourceSubscriber<Long>() {
                    override fun onNext(t: Long?) {
                        val threadName = Thread.currentThread().name
                        println("$threadName: $t")
                    }

                    override fun onError(t: Throwable?) {
                        t?.printStackTrace()
                    }

                    override fun onComplete() {
                        val threadName = Thread.currentThread().name
                        println("$threadName: 完了")
                    }

                })
        println("end")

        Thread.sleep(1000L)
    }

    @Test
    fun testSubscribeOn() {
        Flowable.just(1,2,3,4,5)
                .subscribeOn(Schedulers.computation())//どのスレッドで通知処理を行うか
                .subscribeOn(Schedulers.io()) // 最初のcomputationスレッドで処理を行うしか適用されない。
                .subscribeOn(Schedulers.single())
                .subscribe {
                    println("${Thread.currentThread().name}: $it")
                }

        Thread.sleep(500)
    }

    @Test
    fun testObserveOnBufferSize() {
        // 300milsecごとに0から始まるデータを通知する
        val flowable
                = Flowable.interval(300L, TimeUnit.MILLISECONDS)
                    // BackpressureMode.dropを設定した時と同じ挙動に
                .onBackpressureDrop()

        flowable
                // 非同期でデータを受け取る。 バッファサイズを1に
                .observeOn(Schedulers.computation(), false, 2)
                .subscribe {
                    // 重い処理をしているとみなし1000milsec待つ
                    try {
                        Thread.sleep(1000L)
                    } catch (e : InterruptedException) {
                        e.printStackTrace()
                        System.exit(1)
                    }

                    println("${Thread.currentThread().name}: $it")
                }

        // 暫く待つ
        Thread.sleep(7000L)
    }

    @Test
    fun testFlatMap() {

        // Flatmapメソッドを起動すると、
        val flowable
                = Flowable.just("A", "B", "C")
                // 1000ミリ秒遅れてデータを通知するFlowableを生成
                .flatMap {
                    printThreadInfo(it)
                    Flowable
                                .just(it)
                                .delay(1000L, TimeUnit.MILLISECONDS)
                }
                // 購読する

        flowable.subscribe {
            // 順番がバラバラになっている
            printThreadInfo(it)
        }

        Thread.sleep(7000L)

    }
}