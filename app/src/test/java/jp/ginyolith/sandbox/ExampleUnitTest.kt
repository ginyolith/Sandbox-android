package jp.ginyolith.sandbox

import android.view.View
import org.junit.Test

import org.assertj.android.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertThat("hoge")
                .`as`("ホゲをテストします")
                .isEqualTo("hoge")
                .contains("og")
                .doesNotContain("123")
                .hasLineCount(2)
    }
}
