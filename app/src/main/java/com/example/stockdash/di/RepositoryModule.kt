package com.example.stockdash.di

import com.example.stockdash.data.repository.StockRepository // The interface
import com.example.stockdash.data.repository.StockRepositoryImpl // The implementation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This module's bindings will live as long as the application
abstract class RepositoryModule {

    @Binds // function just returns its input parameter (or a subtype)
    @Singleton // Ensures only one instance of StockRepositoryImpl is created
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl // Hilt knows how to create StockRepositoryImpl because it has @Inject constructor
    ): StockRepository // Tells Hilt: when StockRepository is requested, provide StockRepositoryImpl

}