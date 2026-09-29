package com.sumi.pockon.domain.model

data class Gift(
    var id: String = "",
    val uid: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = "",
    val cash: String = "",
    var isFavorite: Boolean = false
)
