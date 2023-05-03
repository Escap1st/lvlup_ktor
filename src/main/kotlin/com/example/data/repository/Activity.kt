package com.example.data.repository

import com.example.data.database.entity.ActivityEntity
import com.example.data.database.table.ActivityTable
import com.example.data.database.table.UsersActivitiesTable
import com.example.plugins.DatabaseConnection
import org.ktorm.dsl.*

object ActivityRepository {
    private val db = DatabaseConnection.database

    fun getAllActivities(): List<ActivityEntity> = db.from(ActivityTable)
        .select()
        .map { ActivityTable.createEntity(it) }

    fun getAllActivitiesIds(): List<Int> = getAllActivities().map { it.id }

    fun getUserFavorites(userId: String): List<ActivityEntity> {
        val ids = getUserFavoritesIds(userId)
        return db.from(ActivityTable)
            .select()
            .where(ActivityTable.id inList ids)
            .map { ActivityTable.createEntity(it) }
    }

    fun getUserFavoritesIds(userId: String): List<Int> = db.from(UsersActivitiesTable)
        .select()
        .where(UsersActivitiesTable.userId eq userId)
        .map { UsersActivitiesTable.createEntity(it) }
        .map { it.activityId }

    fun deleteUserActivities(userId: String, activitiesIds: List<Int>) =
        db.delete(UsersActivitiesTable) { (it.userId eq userId) and (it.activityId inList activitiesIds) }

    fun insertUserActivities(userId: String, activitiesIds: List<Int>) = db.batchInsert(UsersActivitiesTable) {
        activitiesIds.map { id ->
            item {
                set(it.userId, userId)
                set(it.activityId, id)
            }
        }
    }
}