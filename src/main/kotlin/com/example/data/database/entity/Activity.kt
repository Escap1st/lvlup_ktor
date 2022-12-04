package com.example.data.database.entity

import org.ktorm.entity.Entity

interface ActivityEntity : Entity<ActivityEntity> {
    val id: Int
    val name: String
}