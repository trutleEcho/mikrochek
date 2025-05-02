package com.mikrochek.server.repository.customer

import com.mikrochek.server.database.models.Customer
import java.util.UUID

class CustomerRepositoryImpl : CustomerRepository {
    private val customers = mutableMapOf<String, Customer>()

    override fun createCustomer(customer: Customer): Result<Customer> = runCatching {
        val id = customer.id.ifEmpty { UUID.randomUUID().toString() }
        val newCustomer = customer.copy(id = id)
        customers[id] = newCustomer
        newCustomer
    }

    override fun updateCustomer(customer: Customer): Result<Customer> = runCatching {
        if (!customers.containsKey(customer.id)) {
            throw IllegalArgumentException("Customer not found with id: ${customer.id}")
        }
        customers[customer.id] = customer
        customer
    }

    override fun deleteCustomer(id: String): Result<Unit> = runCatching {
        if (!customers.containsKey(id)) {
            throw IllegalArgumentException("Customer not found with id: $id")
        }
        customers.remove(id)
    }

    override fun getCustomerById(id: String): Customer? = customers[id]

    override fun getAllCustomers(isActive: Boolean): List<Customer> =
        customers.values.filter { it.isActive == isActive }

    override fun searchCustomers(query: String): List<Customer> =
        customers.values.filter { customer ->
            customer.customerCode.contains(query, ignoreCase = true) ||
            customer.companyName.contains(query, ignoreCase = true) ||
            customer.contactName.contains(query, ignoreCase = true) ||
            customer.contactEmail.contains(query, ignoreCase = true) ||
            customer.gstNumber?.contains(query, ignoreCase = true) == true ||
            customer.panNumber?.contains(query, ignoreCase = true) == true
        }

    override fun getCustomersByCity(city: String): List<Customer> =
        customers.values.filter { it.billingCity.equals(city, ignoreCase = true) }

    override fun getCustomersByState(state: String): List<Customer> =
        customers.values.filter { it.billingState.equals(state, ignoreCase = true) }

    override fun getCustomersExceedingCreditLimit(): List<Customer> =
        customers.values.filter { it.creditLimit > 0 }

    // Analytics implementations
    override fun getCustomerCountByState(): Map<String, Int> =
        customers.values
            .groupBy { it.billingState }
            .mapValues { it.value.size }

    override fun getTotalCreditExposure(): Double =
        customers.values.sumOf { it.creditLimit }

    override fun getTopCustomersByCredit(limit: Int): List<Customer> =
        customers.values
            .sortedByDescending { it.creditLimit }
            .take(limit)

    override fun getInactiveCustomers(): List<Customer> =
        customers.values.filter { !it.isActive }
} 