package jp.ginyolith.sandbox

import io.reactivex.processors.*
import org.junit.Test
import org.reactivestreams.Processor

class RxProcessorAndSubject {
    @Test
    fun testPublishProcessorSubject() {
        // publishはリアルタイムの通知のみ。subscribeする前のデータは取得できない
        testProcessor(PublishProcessor.create<Int>())
    }

    @Test
    fun testBehavior() {
        // 直近に通知されたデータのみキャッシュする
        testProcessor(BehaviorProcessor.create<Int>())

        // complete後はキャッシュされたデータも消える
    }

    @Test
    fun testReplay() {
        // すべてのデータをキャッシュする
        testProcessor(ReplayProcessor.create<Int>())
        // complete後はキャッシュされたすべてのデータを通知 or error
    }

    @Test
    fun testAsync() {
        // 完了の直前の最後のデータのみ通知
        testProcessor(AsyncProcessor.create<Int>())
    }

    @Test
    fun testUnicast() {
        // 一つのsubscriberにしか購読されない
        // 複数のsubscriberに購読されたら、Exception
        testProcessor(UnicastProcessor.create<Int>())
    }

    fun testProcessor(processor : Processor<Int, Int>) {
        // publishはリアルタイムの通知のみ。subscribeする前のデータは取得できない
        // Processorの生成
        // 通知前にSubscriberに購読させる
        processor.subscribe(DebugSubscriber("No. 1"))

        processor.onNext(1)
        processor.onNext(2)
        processor.onNext(3)

        // 別のSubscriberにも購読させる
        println("subscriber追加")
        processor.subscribe(DebugSubscriber("--- No. 2"))

        processor.onNext(4)
        processor.onNext(5)

        processor.onComplete()

        // 別のSubscriberにも購読
        println("subscriber追加")
        processor.subscribe(DebugSubscriber("--- No. 3"))
    }
}