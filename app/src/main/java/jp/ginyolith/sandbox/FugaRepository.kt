package jp.ginyolith.sandbox

import javax.inject.Inject

class FugaRepository {

    @Inject constructor()
    fun exec() {
        println("fugafuga")
    }
}