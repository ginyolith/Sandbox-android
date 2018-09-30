package jp.ginyolith.sandbox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import dagger.Component
import dagger.Provides

class MainActivity : AppCompatActivity() {

    private lateinit var useCase : UseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val component = DaggerUseCaseComponent
                                            .builder()
                                            .build()
        useCase = component.inject()

    }

}
