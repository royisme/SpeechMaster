package com.example.speechmaster.di

import com.example.speechmaster.domain.repository.IPronunciationAnalysisRepository
import com.example.speechmaster.data.repository.PronunciationAnalysisRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPronunciationAnalysisRepository(
        impl: PronunciationAnalysisRepositoryImpl
    ): IPronunciationAnalysisRepository
} 