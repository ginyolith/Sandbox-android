package jp.ginyolith.sandbox

import io.reactivex.subscribers.DisposableSubscriber

class DebugSubscriber<T> constructor(): DisposableSubscriber<T>() {
    private var label : String = ""

    constructor(label : String)  : this() {
        this.label = "$label : "
    }

    override fun onComplete() = printRxDebugInfo(label = label)
    override fun onNext(t: T) = printRxDebugInfo(data = t, label = label)
    override fun onError(t: Throwable?) = printRxDebugInfo(e = t, label = label)
}