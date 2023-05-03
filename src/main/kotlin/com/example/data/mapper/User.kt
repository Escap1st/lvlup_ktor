package com.example.data.mapper

import com.example.data.database.entity.UsersEntity
import com.example.data.model.response.UserResponse

fun UsersEntity.toResponse(): UserResponse {
    return UserResponse(
        id,
        name,
        surname
    )
}