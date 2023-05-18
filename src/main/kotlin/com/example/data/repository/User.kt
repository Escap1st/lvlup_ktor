package com.example.data.repository

import com.example.data.database.entity.UsersEntity
import com.example.data.database.table.UsersTable
import org.ktorm.database.Database
import org.ktorm.dsl.*

class UserRepository(private val db: Database) {
    fun getUserById(userId: String): UsersEntity? = db.from(UsersTable)
        .select()
        .where(UsersTable.id eq userId)
        .map { UsersTable.createEntity(it) }
        .singleOrNull()

    fun getUserByEmail(email: String): UsersEntity? = db.from(UsersTable)
        .select()
        .where(UsersTable.email eq email)
        .map { UsersTable.createEntity(it) }
        .singleOrNull()

    fun getUserByCredentials(email: String, password: String): UsersEntity? = db.from(UsersTable)
        .select()
        .where((UsersTable.email eq email) and (UsersTable.password eq password))
        .map { UsersTable.createEntity(it) }
        .singleOrNull()

    fun getUsersPassword(userId: String) = db.from(UsersTable)
        .select(UsersTable.password)
        .where { UsersTable.id eq userId }
        .map { it[UsersTable.password] }
        .single()!!

    fun changePassword(userId: String, password: String): Int = db.update(UsersTable) {
        set(it.password, password)
        where { it.id eq userId }
    }

    fun initUser(userId: String, name: String, surname: String, email: String) = db.insert(UsersTable) {
        set(it.id, userId)
        set(it.name, name)
        set(it.surname, surname)
        set(it.email, email)
    }

    fun renameUser(userId: String, name: String, surname: String) = db.update(UsersTable) {
        set(it.name, name)
        set(it.surname, surname)
        where { it.id eq userId }
    }
}