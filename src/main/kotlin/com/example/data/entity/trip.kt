package com.example.data.entity

import org.ktorm.schema.*

object TripEntity : Table<Nothing>("trips") {
    val id = varchar("id").primaryKey()
    val title = varchar("title")
    val description = text("description")
    val startDate = datetime("start_date")
    val finishDate = datetime("finish_date")
}