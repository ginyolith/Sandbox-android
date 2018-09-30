package jp.ginyolith.sandbox

import javax.inject.Inject

class HogeRepository {

    @Inject constructor()

    fun exec() {
        println("hogehoge")
    }

}