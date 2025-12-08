package com.vettrack.core.domain

enum class UserRole {
    vet,
    admin
}

enum class ReminderStatus {
    PENDING,
    DONE,
    OVERDUE,
    DISMISSED,
}

enum class ExamStatus {
    DRAFT,
    FINAL
}