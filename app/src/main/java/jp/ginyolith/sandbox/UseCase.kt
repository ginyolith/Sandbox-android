package jp.ginyolith.sandbox

import javax.inject.Inject

class UseCase {
    @Inject lateinit var hogeRepository : HogeRepository
    @Inject lateinit var fugaRepository : FugaRepository

    @Inject constructor()

    fun exec() {
        hogeRepository.exec()
        fugaRepository.exec()
    }
}