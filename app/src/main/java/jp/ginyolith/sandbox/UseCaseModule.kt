package jp.ginyolith.sandbox

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UseCaseModule() {

    @Provides
    @Singleton
    fun provideHogeRepository() : HogeRepository {
        return HogeRepository()
    }

    @Provides
    @Singleton
    fun provideFugaRepository() : FugaRepository{
        return FugaRepository()
    }
}