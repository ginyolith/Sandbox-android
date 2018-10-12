package jp.ginyolith.sandbox

fun printThreadInfo(it : Any) {
    println("${Thread.currentThread().name}: $it")
}