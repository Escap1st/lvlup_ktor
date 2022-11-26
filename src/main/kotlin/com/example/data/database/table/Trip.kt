package com.example.data.database.table

import com.example.data.database.entity.TripEntity
import org.ktorm.schema.*

object TripTable : Table<TripEntity>("trips") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val title = varchar("title").bindTo { it.title }
    val description = text("description").bindTo { it.description }
    val startDate = datetime("start_date").bindTo { it.startDate }
    val finishDate = datetime("finish_date").bindTo { it.finishDate }
}