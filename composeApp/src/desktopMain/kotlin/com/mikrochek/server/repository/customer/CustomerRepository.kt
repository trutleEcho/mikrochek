package com.mikrochek.server.repository.customer

import com.mikrochek.server.database.models.Customer

interface CustomerRepository {
    fun createCustomer(customer: Customer): Result<Customer>
    fun updateCustomer(customer: Customer): Result<Customer>
    fun deleteCustomer(id: String): Result<Unit>
    fun getCustomerById(id: String): Customer?
    fun getAllCustomers(isActive: Boolean = true): List<Customer>
    fun searchCustomers(query: String): List<Customer>
    fun getCustomersByCity(city: String): List<Customer>
    fun getCustomersByState(state: String): List<Customer>
    fun getCustomersExceedingCreditLimit(): List<Customer>
    
    // Analytics
    fun getCustomerCountByState(): Map<String, Int>
    fun getTotalCreditExposure(): Double
    fun getTopCustomersByCredit(limit: Int = 10): List<Customer>
    fun getInactiveCustomers(): List<Customer>
} 