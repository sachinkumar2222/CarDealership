package com.slt.cardealership.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * A simple singleton class to hold the session token in memory.
 * In a production app, you would use encrypted SharedPreferences or DataStore for persistence.
 */
@Singleton
class SessionManager @Inject constructor() {
    var authToken: String? = null
}
