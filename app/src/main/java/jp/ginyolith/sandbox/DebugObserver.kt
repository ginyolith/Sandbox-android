package jp.ginyolith.sandbox

import io.reactivex.CompletableObserver
import io.reactivex.MaybeObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

class DebugSingleObserver<T> : SingleObserver<T> {

    private var label : String = ""

    constructor(label : String)  : this() {
        this.label = "$label : "
    }

    constructor()

    override fun onSubscribe(d: Disposable) = printRxDebugInfo(d = d, label = label)
    override fun onSuccess(t: T) = printRxDebugInfo(data = t, label = label)
    override fun onError(t: Throwable) = printRxDebugInfo(e = t, label = label)
}

class DebugMaybeObserver<T>() : MaybeObserver<T> {
    private var label : String = ""

    constructor(label : String)  : this() {
        this.label = "$label : "
    }

    override fun onSubscribe(d: Disposable) = printRxDebugInfo(d = d, label = label)
    override fun onSuccess(t: T) = printRxDebugInfo(data = t, label = label)
    override fun onError(t: Throwable) = printRxDebugInfo(e = t, label = label)
    override fun onComplete() = printRxDebugInfo(label = label)
}

class DebugCompletableObserver() : CompletableObserver {
    private var label : String = ""

    constructor(label : String)  : this() {
        this.label = "$label : "
    }

    override fun onSubscribe(d: Disposable) = printRxDebugInfo(d = d, label = label)
    override fun onError(t: Throwable) = printRxDebugInfo(e = t, label = label)
    override fun onComplete() = printRxDebugInfo(label = label)
}