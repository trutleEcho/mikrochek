package com.mikrochek.di

import com.mikrochek.config.EnvVariables
import com.mikrochek.server.repository.auth.AuthRepository
import com.mikrochek.server.repository.auth.AuthRepositorySQLiteImpl
import com.mikrochek.server.repository.customer.CustomerRepository
import com.mikrochek.server.repository.customer.CustomerRepositoryImpl
import com.mikrochek.server.repository.employee.EmployeeRepository
import com.mikrochek.server.repository.employee.EmployeeRepositorySQLiteImpl
import com.mikrochek.server.repository.po.PurchaseOrderRepository
import com.mikrochek.server.repository.po.PurchaseOrderRepositorySQLiteImpl
import com.mikrochek.server.repository.product.ProductRepository
import com.mikrochek.server.repository.product.ProductRepositorySQLiteImpl
import com.mikrochek.server.repository.quotation.QuotationRepository
import com.mikrochek.server.repository.quotation.QuotationRepositorySQLiteImpl
import com.mikrochek.server.repository.user.UserRepository
import com.mikrochek.server.repository.user.UserRepositorySQLiteImpl
import com.mikrochek.server.service.DocumentService
import com.mikrochek.server.service.ProductService

import com.mongodb.ConnectionString
import org.koin.core.module.Module
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val appModule: Module = module {
    single<CoroutineDatabase> {
        val client = KMongo.createClient(ConnectionString(EnvVariables.CONNECTION_STRING)).coroutine
        client.getDatabase(EnvVariables.DB_NAME)
    }

    single<CoroutineClient> {
        KMongo.createClient(ConnectionString(EnvVariables.CONNECTION_STRING)).coroutine
    }
    single<UserRepository> { UserRepositorySQLiteImpl() }
    single<AuthRepository> { AuthRepositorySQLiteImpl(get()) }
    single<ProductRepository> { ProductRepositorySQLiteImpl() }
    single<PurchaseOrderRepository> { PurchaseOrderRepositorySQLiteImpl() }
    single<QuotationRepository> { QuotationRepositorySQLiteImpl() }
    single<EmployeeRepository> { EmployeeRepositorySQLiteImpl() }
    single<CustomerRepository> { CustomerRepositoryImpl() }
    single { DocumentService() }
    single { ProductService() }
}