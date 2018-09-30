package jp.ginyolith.sandbox

import org.junit.Test

class DITest {
    @Test
    fun testDI() {
        val component = DaggerUseCaseComponent
                .builder()
                .build()
        val useCase = component.inject()
        useCase.exec()
    }
}