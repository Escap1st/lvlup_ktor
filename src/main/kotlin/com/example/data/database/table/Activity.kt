package com.example.data.database.table

import com.example.data.database.entity.ActivityEntity
import com.example.data.database.table.TripTable.bindTo
import com.example.data.database.table.TripTable.primaryKey
import org.ktorm.schema.*

object ActivityTable : Table<ActivityEntity>("activities") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
}