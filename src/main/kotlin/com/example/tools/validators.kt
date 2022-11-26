package com.example.tools

class EmailValidator {
    companion object {
        private const val EMAIL_REGEX = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$";

        fun isEmailValid(email: String): Boolean {
            return EMAIL_REGEX.toRegex().matches(email)
        }
    }
}

class PasswordValidator {
    companion object {
        private val requirements =
            listOf(String::isLongEnough, String::hasEnoughDigits, String::isMixedCase, String::hasSpecialChar)

        fun isPasswordValid(password: String): Boolean {
            return requirements.all { check -> check(password) }
        }
    }
}

private fun String.isLongEnough() = length >= 8
private fun String.hasEnoughDigits() = any(Char::isDigit)
private fun String.isMixedCase() = any(Char::isLowerCase) && any(Char::isUpperCase)
private fun String.hasSpecialChar() = any { !it.isLetterOrDigit() }
