package com.sumi.pockon.core.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkStatusProvider {
    val isConnected: StateFlow<Boolean>
}
