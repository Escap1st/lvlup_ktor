package com.example.data.database.table

import com.example.data.database.entity.ActivityEntity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ActivityTable : Table<ActivityEntity>("activities") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
}