package com.vettrack.core.auth

import org.springframework.stereotype.Component

@Component
class CurrentUserHolder {
    private val holder: ThreadLocal<AuthenticatedUser?> = ThreadLocal()

    fun set(user: AuthenticatedUser?) {
        holder.set(user)
    }

    fun get(): AuthenticatedUser? = holder.get()

    fun clear() {
        holder.remove()
    }
}