package com.waveofmymind.user.presentation

import com.waveofmymind.user.presentation.feign.CreateAccountRequest

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String
) {
    fun toCreateAccountRequest(password: String) = CreateAccountRequest(id, name, password)

    companion object {
        @JvmStatic
        fun from(id: Long, name: String, email: String) = UserResponse(id, name, email)
    }
}
