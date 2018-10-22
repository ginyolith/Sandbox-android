package jp.ginyolith.sandbox

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.Assert.*
import org.junit.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

/**
 * Rxのテストは一般的ではない
 *
 * テスト用のメソッドがいくつか
 * do~~
 * -> 各通知を行う前に指定した処理を行う
 *
 * blocking~~
 * -> 呼び出し元のスレッドで強制的に動作させる
 *
 * testScheduler, testObserver
 * -> onNext等の中でassertなど使える
 *
 * TestScheduler
 * -> 時間を演算し想定される通知データを結果としてすぐ得られる
 */
class RxDebugAndTest() {
    @Test
    fun testDoOnNext() {
        Flowable.range(1, 5)
                .doOnNext { println("--- オリジナル: $it") }
                .filter{ it % 2 == 0}
                .doOnNext { println("--- filter後: $it") }
                .subscribe(DebugSubscriber())
    }

    @Test
    fun testDoOnComplete() {
        Flowable.range(1, 5)
                .doOnComplete { println("doOnComplete")}
                .subscribe(DebugSubscriber())
    }

    @Test
    fun testDoOnError() {
        Flowable.range(1,5)
                .doOnError { println("オリジナル: ${it.message}") }
                .map { if (it == 3) throw Exception("exception") else it}
                .doOnError { println("オリジナル: ${it.message}") }
                .subscribe(DebugSubscriber())
    }

//    @Test
//    略
//    fun testDoOnSubscribe() {

    @Test
    fun testDoOnRequest() {
        // データ数のリクエスト
        // BackPressure機能のないObservableには存在しない

        Flowable.range(1,3)
                // デフォルトのバッファサイズは128
                .doOnRequest{ println("オリジナル: size = $it") }
                .observeOn(Schedulers.computation())
                .doOnRequest{ println("observeOn後: size = $it") }
                .subscribe(object : Subscriber<Int> {
                    var subscription : Subscription? = null

                    override fun onComplete()
                        = println("完了")

                    override fun onSubscribe(s: Subscription?) {
                        this.subscription = s.also { it?.request(1) }
                    }

                    override fun onNext(t: Int?) {
                        println(t)
                        t?.let { subscription?.request(t.toLong()) }

                    }

                    override fun onError(t: Throwable?)
                        = println("エラー : $t")

                })

        Thread.sleep(500L)
    }

    // doOnCancel / doOnDispose
    // 購読を解除されたタイミングでの処理

    // blocking

    @Test
    fun testBlockingFirst() {
        // 最初の通知データを呼び出し元スレッドで取得
        val actual =
                Flowable.interval(300L, TimeUnit.MILLISECONDS)
                        .blockingFirst()

        assertEquals(actual, 0L)
    }

    @Test
    fun testBlockingLast() {
        val actual =
                Flowable.interval(300L, TimeUnit.MILLISECONDS)
                        .take(3)
                        .blockingLast()

        assertEquals(2L, actual)
    }

    @Test
    fun testBlockingIterable() {
        // 通知されるデータを随時取得する
        // Flowableに対して使う
        val result =
                Flowable.interval(300L, TimeUnit.MILLISECONDS)
                        .take(5)
                        .blockingIterable()

        val itr = result.iterator()

        assertTrue(itr.hasNext())

        assertEquals(0L, itr.next())
        assertEquals(1L, itr.next())
        assertEquals(2L, itr.next())

        Thread.sleep(1000L)
        assertEquals(3L, itr.next())
        assertEquals(4L, itr.next())

        assertFalse(itr.hasNext())
    }

    // blockingSubscribe
    // 通知の待ちをスレッドをblockingして行う

    @Test
    fun testTestSubscriber() {
        // どのようなデータが通知されるか見ることが可能

        val target = Flowable.interval(100L, TimeUnit.MILLISECONDS)
        val testSubscriber = target.test()

        testSubscriber.assertEmpty()

        testSubscriber.await(150L, TimeUnit.MILLISECONDS)

        testSubscriber.assertValues(0L)

        testSubscriber.await(100L, TimeUnit.MILLISECONDS)

        testSubscriber.assertValues(0L, 1L)

        // メソッドチェーンも可能
        testSubscriber
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun testTestScheduler() {
        // Rx内部の時間を進めることが出来る

        val start = System.currentTimeMillis()

        val testScheduler = TestScheduler()

        val flowable = Flowable.interval(500L, TimeUnit.MILLISECONDS, testScheduler)

        val result = flowable.test()

        println("data = ${result.values()}")
        result.assertEmpty()

        // 500ミリ秒進める
        testScheduler.advanceTimeBy(500L, TimeUnit.MILLISECONDS)

        println("data = ${result.values()}")
        result.assertValues(0L)
    }
}