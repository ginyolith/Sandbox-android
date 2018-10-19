package jp.ginyolith.sandbox

import android.os.Debug
import io.reactivex.Flowable
import org.junit.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.math.BigDecimal
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import java.util.function.Supplier

class RxOperatorTest {
    @Test
    fun justTest() {
        // 引数のデータを通知する
        Flowable.just(1, 2, 3).subscribe(DebugSubscriber())
    }

    @Test
    fun fromArrayOrIterableTest() {
        // 配列のデータを追加する
        Flowable.fromArray(1, 2,3,5,2,3).subscribe(DebugSubscriber())

        // イテレータブルのデータ追加
        Flowable.fromIterable(listOf(12,8921,22)).subscribe(DebugSubscriber())
    }

    @Test
    fun fromCallableTest() {
        // java.util.concurrent.Callableインターフェイスに対応
        val callable : Callable<Long> = object : Callable<Long> {
            override fun call(): Long {
                return System.currentTimeMillis()
            }
        }

        Flowable.fromCallable(callable).subscribe(DebugSubscriber())
    }

//    @Test
//    fun rangeTest
//    大体わかるので省略

    // フォーマッタ
    val formatter = DateTimeFormatter.ofPattern("mm:ss.SSS")
    val getNowStr = {LocalTime.now().format(formatter)}
    val getThreadName = { Thread.currentThread().name}
    @Test
    fun interval() {
        // 指定した通知間隔
        // デフォルトでcompurationスケジューラで実行される
        // デフォルトでは、最初の通知までもインターバルが適用される。メソッドで設定変更可能

        // 1000ミリ秒ごとに数値を通知する
        val f = Flowable.interval(1000L, TimeUnit.MILLISECONDS)

        // 処理開始する前の時間
        println("開始時間: ${getNowStr()}")

        f.subscribe {
            println("${getThreadName()}: ${getNowStr()}: data=:$it")
        }

        Thread.sleep(5000L)
    }

    @Test
    fun timerTest() {
        // 指定した時間の後に0を通知する
        // compuration scheduler 上で実行（デフォ）


        // 処理開始する前の時間
        println("開始時間: ${getNowStr()}")

        // 1000milsecあとに0を通知する
        Flowable.timer(1000L, TimeUnit.MILLISECONDS).subscribe({
            println("${getThreadName()}: ${getNowStr()}: data=:$it")
        }, {
            println("エラー＝$it")
        }, {
            println("完了")
        })

        Thread.sleep(1500L)
    }

    @Test
    fun deferTest() {
        // 購読されるたびに新しいFlowable/obsevableを生成

        // 呼び出されると現在時間を通知するFlowable
        val f = Flowable.defer { Flowable.just(LocalTime.now()) }

        f.subscribe(DebugSubscriber("No. 1"))

        Thread.sleep(2000L)

        f.subscribe(DebugSubscriber("No. 2"))
    }

    @Test
    fun emptyTest() {
        // 空のFlowableを生成
        // nullの代わりなどに使う
        Flowable.empty<Any>().subscribe(DebugSubscriber())
    }

    @Test
    fun errorTest() {
        // エラーのみを通知
        // flabMap時にエラーを意図的に作りたい時に使う
        Flowable.error<Any>(Exception("例外発生")).subscribe(DebugSubscriber())
    }

    @Test
    fun neverTest() {
        // 何も通知しない
        // 完了も通知されないのでemptyと混同しないよう注意
        Flowable.never<Any>().subscribe(DebugSubscriber())
    }

//    @Test
//    fun map
//    データ変換。分かるので省略

    @Test
    fun flatMap() {
        // 受け取ったデータをFlowable/Observableに変換
        // そのデータを通知

        // flowableを連鎖出来る

        // flatMap(mapper, combiner)
        // biFunction
        val bf = object : BiFunction<Int, Long, String> {
            /**
             * 元のデータとMapされたデータをコンビネーション = combiner
             * @param source = 元のデータ
             * @param mapped = mapperで生成されたデータ
             */
            override fun apply(source: Int, mapped: Long): String {
                return "[$source]$mapped"
            }
        }

        // onNextMapper       = 正常時   ： 引数に通知データ
        // onErrorMapper      = エラー時 ： 引数にThrowable
        // onCompleteSupplier = 完了時に通知

        // サンプル(mapper)
        Flowable.just("A", "", "B", "", "C")
            // flatMapを使って空文字を除き、かつ小文字に変換
                .flatMap {
                    when {
                        it.isEmpty() -> Flowable.empty<String>()
                        else -> Flowable.just(it.toLowerCase())
                    }
                }.subscribe(DebugSubscriber())


        // サンプル(mapper, combinator)
        Flowable.range(1, 3)
                .flatMap({
                    // データを受け取ったら新しいFlowableを作成
                    Flowable.interval(100L, TimeUnit.MILLISECONDS).take(3)//3回まで
                }, {source, mapped ->
                   // 第二引数：元データと変換したデータを使って新たな通知を行う
                    "[$source]$mapped"
                })
                .subscribe(DebugSubscriber())

        Thread.sleep(1000L)

        // サンプル
        // エラーが発生するflowable
        val original = Flowable.just(1,2,0,4,5)
                .map { 10 / it } // 0が来たら例外となる

        // 通常時、エラー発生時、完了時指定したデータに変換
        original.flatMap({
            // 通常時
            Flowable.just(it)
        }, {
            // エラー時
            Flowable.just(-1)
        }, {
            // 完了時
            Flowable.just(100)
        }).subscribe(DebugSubscriber())
    }

    @Test
    fun testConcatMap() {
        // 受け取ったデータを順に実行する
        Flowable.range(10,3)
                .concatMap { source ->
                    Flowable.interval(500L, TimeUnit.MILLISECONDS)
                            // 2件まで
                            .take(2)
                            .map {
                                "${System.currentTimeMillis()}ms:[$source] $it"
                            }
                }.subscribe(DebugSubscriber())

        Thread.sleep(4000L)
    }

    @Test
    fun testConcatMapEager() {
        // 受け取ったデータの変換は並列だが、通知は直列
        // 通知はバッファされるためメモリ不足に注意
        Flowable.range(10,3)
                .concatMapEager { source ->
                    Flowable.interval(500L, TimeUnit.MILLISECONDS)
                            // 2件まで
                            .take(2)
                            .map {
                                "${System.currentTimeMillis()}ms:[$source] $it"
                            }
                }.subscribe(DebugSubscriber())

        Thread.sleep(4000L)
    }

    @Test
    fun testConcatMapEagerDelayError() {
        // 受け取ったデータの変換は並列だが、通知は直列
        // エラー時を考慮したconcatMapEager
        Flowable.range(10,3)
                .concatMapEagerDelayError({ source ->
                    Flowable.interval(500L, TimeUnit.MILLISECONDS)
                            // 3件まで
                            .take(3)
                            .doOnNext {
                                // 特定のタイミングでException
                                if (source == 11 && it == 1L) {
                                    throw Exception("例外発生")
                                }
                            }
                            .map {
                                "${System.currentTimeMillis()}ms:[$source] $it"
                            }
                },true //エラーの通知するタイミング。エラーが起きても通知は続く
                )
                .subscribe(DebugSubscriber())

        Thread.sleep(4000L)
    }

    @Test
    fun testBuffer() {
        // 毎回通知ではんく、いくつかにまとめて通知する（Collectionとなる）

        // count指定
        Flowable.interval(100L, TimeUnit.MILLISECONDS)
                .take(10)
                .buffer(3)
                .subscribe(DebugSubscriber())

        Thread.sleep(3000L)
    }

    @Test
    fun testBufferBoundaryIndicatorSupplier() {
        // TODO 上手く出来ない
        // BoundaryIndicatorSupplier
        Flowable.interval(300L, TimeUnit.MILLISECONDS)
                .take(7)
                .buffer(Callable<Publisher<Flowable<Long>>> {
                    Publisher { Flowable.timer(1000L, TimeUnit.MILLISECONDS) } })
                .subscribe(DebugSubscriber())

        Thread.sleep(4000L)
    }

    @Test
    fun testToList() {
        // 大量データの場合メモリ不足に注意　戻り値はシングル
        Flowable.just("A", "B" , "C", "D", "E", "F")
                .toList()
                .subscribe(DebugSingleObserver())
        // なぜObserve?
        // singleの通知結果なので、データの通知のみで処理が完了するため。
    }

    @Test
    fun toMap() {
        // 通知するデータをキーと1つの値をもつmapとして通知する
        // キーが重複した場合上書きされる
        val flowable = Flowable.just("1A", "2B", "3C", "1D", "2E")
        flowable.toMap {
                    it.substring(0,1).toLong()
                }.subscribe(DebugSingleObserver())

        // Key, ValueSelectorを使った場合
        flowable.toMap(
                {
                    it.substring(0, 1).toLong()
                },
                {
                    it.substring(1)
                }
        ).subscribe(DebugSingleObserver())
    }

    @Test
    fun testToMultiMap() {
        // Valueが重複した場合は、Collectionに値を追加していく
        Flowable.interval(500L, TimeUnit.MILLISECONDS)
                .take(5)
                .toMultimap {
                    when {
                        (it % 2) == 0L -> "偶数"
                        else -> "奇数"
                    }
                }.subscribe(DebugSingleObserver())

        Thread.sleep(3000L)
    }

//    filter
    //　なんとなく分かるので略

    // distinct
    //　なんとなく分かるので略
    @Test
    fun testDistinct() {
        Flowable.just("A", "a", "B", "b", "C", "c", "A", "a", "B", "b")
                // 小文字大文字関係なく比較
                .distinct{it.toLowerCase()}
                .subscribe(DebugSubscriber())
    }

    @Test
    fun testDistinctUntilChanged() {
        // 連続して重複したデータは通知しない
        Flowable.just("A" , "a", "a", "A", "a")
                .distinctUntilChanged()
                .subscribe(DebugSubscriber())

        Flowable.just("1", "1.0", "0.1", "0.10", "1")
                .distinctUntilChanged { t1, t2 ->
                    BigDecimal(t1).compareTo(BigDecimal(t2))  == 0
                }.subscribe(DebugSubscriber())
    }


    // take() // なんとなく分かるので略

    @Test
    fun testTakeUntil() {
        // 指定した条件になると通知が終わる
        Flowable.interval(300L, TimeUnit.MILLISECONDS)
                .takeUntil {it == 3L }
                .subscribe(DebugSubscriber())

        Thread.sleep(3000L)

        // 時間で通知完了を決める
        Flowable.interval(300L, TimeUnit.MILLISECONDS)
                .takeUntil(Flowable.timer(1000L, TimeUnit.MILLISECONDS))
                .subscribe(DebugSubscriber())

        Thread.sleep(3000L)

    }

    // takeWhile -- 指定した条件がtrueの間のみ通知

    @Test
    fun testTakeLast() {
        // 最後から数えて指定した数、期間のデータを通知
        Flowable.interval(800L, TimeUnit.MILLISECONDS)
                .take(5) // 5件通知
                .takeLast(2) // そのなかの最後の2兼のみ通知
                .subscribe(DebugSubscriber())

        Thread.sleep(5000L)
        Flowable.interval(300L, TimeUnit.MILLISECONDS)
                .take(10) // 10件通知
                .takeLast(2, 1000L, TimeUnit.MILLISECONDS) // 最後の1000msに通知されたデータのうち、最後の2件を通知
                .subscribe(DebugSubscriber())

        Thread.sleep(5000L)

    }
}