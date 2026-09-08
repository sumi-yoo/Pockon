package com.sumi.pockon.domain.model

data class NotificationSettings(
    val isEnabled: Boolean,
    val daysBeforeExpiry: Int,
    val hour: Int,
    val minute: Int
)
