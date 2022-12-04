package com.example.data.database.table

import com.example.data.database.entity.UsersActivitiesEntity
import com.example.data.database.entity.UsersEntity
import com.example.data.database.entity.UsersTripsEntity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object UsersTable : Table<UsersEntity>("users") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val surname = varchar("surname").bindTo { it.surname }
    val email = varchar("email").bindTo { it.email }
    val password = varchar("password").bindTo { it.password }
}

object UsersActivitiesTable : Table<UsersActivitiesEntity>("users_activities") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val userId = varchar("user_id").bindTo { it.userId }
    val activityId = int("activity_id").bindTo { it.activityId }
}

object UsersTripsTable : Table<UsersTripsEntity>("users_trips") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val userId = varchar("user_id").bindTo { it.userId }
    val tripId = varchar("activity_id").bindTo { it.tripId }
}
