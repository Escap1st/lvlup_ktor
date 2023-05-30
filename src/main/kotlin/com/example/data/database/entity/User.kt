package com.example.data.database.entity

import org.ktorm.entity.Entity

interface UsersEntity : Entity<UsersEntity> {
    val id: String
    val name: String
    val surname: String
    val email: String
    val password: String?
}

interface UsersActivitiesEntity : Entity<UsersActivitiesEntity> {
    val userId: String
    val activityId: Int
}