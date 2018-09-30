package jp.ginyolith.sandbox

import android.app.Application
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(UseCaseModule::class))
interface UseCaseComponent{
    fun inject() : UseCase
}