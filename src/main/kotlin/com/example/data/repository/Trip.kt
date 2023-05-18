package com.example.data.repository

import com.example.data.database.table.TripTable
import org.ktorm.database.Database
import org.ktorm.dsl.*

class TripRepository(private val db: Database) {
    fun getAllTrips() = db.from(TripTable)
        .select()
        .map { TripTable.createEntity(it) }

    fun getTripById(tripId: String) = db.from(TripTable)
        .select()
        .where { TripTable.id eq tripId }
        .map { TripTable.createEntity(it) }
        .firstOrNull()
}