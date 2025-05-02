package com.mikrochek.server.database

object Collection {
    const val ORGANIZATION = "organizations"
    const val ORGANIZATION_UPDATE_LOGS = "org-update-logs"

    /** Client details collections. */
    const val CLIENT_ACTION_LOG = "cli-action-logs"
    const val CLIENT_UPDATE_LOG = "cli-update-logs"

    /** Employee details collections. */
    const val USER = "users"
    const val EMPLOYEE_ACTION_LOG = "emp-action-logs"
    const val EMPLOYEE_UPDATE_LOG = "emp-update-logs"
}