package com.vettrack.core.service

import com.vettrack.core.domain.User
import com.vettrack.core.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getById(id: UUID): User =
        userRepository.findById(id).orElseThrow {
            NoSuchElementException("User $id not found")
        }

    fun findByEmail(email: String): User? =
        userRepository.findByEmailIgnoreCase(email)

}