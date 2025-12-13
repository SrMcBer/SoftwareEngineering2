package com.vettrack.core.domain

enum class UserRole {
    vet,
    admin
}

enum class ReminderStatus {
    pending,
    done,
    dismissed,
    overdue,
}

enum class ExamStatus {
    draft,
    final
}