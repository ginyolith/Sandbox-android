package jp.ginyolith.sandbox

import io.reactivex.disposables.Disposable

fun printThreadInfo(it : Any) {
    println("${Thread.currentThread().name}: $it")
}

fun printRxDebugInfo(d : Disposable? = null, e : Throwable? = null, data : Any? = null, label : String) {
    val thread = Thread.currentThread().name
    val prefix  = "$thread : $label"
    val postfix = when {
        data != null -> data
        e != null -> "エラー: $e"
        d != null -> d
        else -> "完了"
    }
    println("$prefix$postfix")

}