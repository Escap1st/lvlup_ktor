package com.example.data.repository

import com.example.data.database.entity.AuthApplicationType
import com.example.data.database.table.AuthApplicationsTable
import com.example.data.database.table.RefreshTokensTable
import com.example.plugins.sha256
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDateTime

class AuthRepository(private val db: Database) {
    fun isUserApplied(userId: String) = db.from(AuthApplicationsTable)
        .select()
        .where(AuthApplicationsTable.userId eq userId)
        .totalRecords == 0

    fun getApplicationsByTokenAndType(token: String, types: List<AuthApplicationType>) = db.from(AuthApplicationsTable)
        .select()
        .where(
            (AuthApplicationsTable.token eq token) and
                    (AuthApplicationsTable.type inList types)
        )
        .map { AuthApplicationsTable.createEntity(it) }

    fun deleteApplication(token: String) = db.delete(AuthApplicationsTable) { it.token eq token }

    fun initApplication(token: String, userId: String, type: AuthApplicationType, code: String?) =
        db.insert(AuthApplicationsTable) {
            set(it.token, token)
            set(it.userId, userId)
            set(it.datetime, LocalDateTime.now())
            set(it.type, type)
            if (code != null) set(it.code, code.sha256())
        }

    fun recreateApplication(token: String, userId: String, type: AuthApplicationType, code: String?) =
        db.update(AuthApplicationsTable) {
            set(it.token, token)
            set(it.datetime, LocalDateTime.now())
            set(it.code, code?.sha256())
            set(it.type, type)
            where { it.userId eq userId }
        }

    fun updateApplicationCode(oldToken: String, newToken: String, newCode: String?) = db.update(AuthApplicationsTable) {
        set(it.code, newCode)
        set(it.token, newToken)
        set(it.datetime, LocalDateTime.now())
        where { it.token eq oldToken }
    }

    fun insertRefreshToken(token: String, userId: String) = db.insert(RefreshTokensTable) {
        set(it.token, token)
        set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
        set(it.userId, userId)
    }

    fun getUsersToken(userId: String, token: String) = db.from(RefreshTokensTable)
        .select()
        .where(
            (RefreshTokensTable.token eq token)
                    and (RefreshTokensTable.userId eq userId)
        )
        .map { RefreshTokensTable.createEntity(it) }
        .firstOrNull()

    fun updateToken(oldToken: String, newToken: String) = db.update(RefreshTokensTable) {
        set(it.token, newToken)
        set(it.expiresAt, LocalDateTime.now().plusWeeks(3))
        where { it.token eq oldToken }
    }
}