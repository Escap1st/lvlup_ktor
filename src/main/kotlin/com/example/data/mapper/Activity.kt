package com.example.data.mapper

import com.example.data.database.entity.ActivityEntity
import com.example.data.model.response.ActivityResponse

fun ActivityEntity.toResponse(): ActivityResponse {
    return ActivityResponse(
        id,
        getActivityDisplayName(name),
        "/static/$name.png"
    )
}

// TODO: deal with translations
fun getActivityDisplayName(name: String): String {
    return when (name) {
        "hiking" -> "Походы"
        "climbing" -> "Восхождения"
        "surfing" -> "Серфинг"
        "camping" -> "Кемпинг"
        "fishing" -> "Рыбалка"
        "beach" -> "Пляжи"
        else -> "Без имени"
    }
}