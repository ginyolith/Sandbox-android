package jp.ginyolith.sandbox

import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class RxJavaAtomicTest {
    class AtomicCounter {
        private val count = AtomicInteger(0)

        fun increment()
            = count.incrementAndGet()

        fun get() : Int
            = count.get()
    }

    @Test
    fun testAtomic() {
        val counter = AtomicCounter()

        val task = {
            for (i in 1..10000) {
                counter.increment()
            }
        }

        val executorService = Executors.newCachedThreadPool()

        val future1 = executorService.submit(task, true)
        val future2 = executorService.submit(task, true)

        if (future1.get() && future2.get()) {
            println(counter.get())
        } else {
            fail()
        }

        executorService.shutdown()
    }

    open class Point {
        private val x = AtomicInteger(0)
        private val y = AtomicInteger(0)

        fun rightUp() {
            x.incrementAndGet()
            y.incrementAndGet()
        }

        fun getX() : Int {
            return x.get()
        }

        fun getY() : Int {
            return y.get()
        }
    }

    class SyncgronizedPoint {
        private val lock = Any()
        private var x : Int = 0
        private var y : Int = 0

        fun rightUp() {
            synchronized(lock) {
                x += 1
                y += 1
            }
        }

        fun getX() : Int {
            synchronized(lock) {
                return x
            }
        }

        fun getY() : Int {
            synchronized(lock) {
                return  y
            }
        }
    }

    @Test
    fun testSynchronized() {

    }
}
