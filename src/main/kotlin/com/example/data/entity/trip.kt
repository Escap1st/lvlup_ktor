package com.example.data.entity

import org.ktorm.schema.*

object TripEntity : Table<Nothing>("trips") {
    val id = int("id").primaryKey()
    val title = varchar("title")
    val description = text("description")
    val startDate = date("start_date")
    val finishDate = date("finish_date")
}