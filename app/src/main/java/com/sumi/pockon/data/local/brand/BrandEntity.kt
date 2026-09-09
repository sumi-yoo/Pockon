package com.sumi.pockon.data.local.brand

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BrandEntity(
    @PrimaryKey val keyword: String,
    val documents: List<BrandLocationLocal>
)
