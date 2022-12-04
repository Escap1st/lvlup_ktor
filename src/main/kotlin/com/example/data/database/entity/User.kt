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
    val id: String
    val userId: String
    val activityId: Int
}

interface UsersTripsEntity : Entity<UsersTripsEntity> {
    val id: String
    val userId: String
    val tripId: String
}