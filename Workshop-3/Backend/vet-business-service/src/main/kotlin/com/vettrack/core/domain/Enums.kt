package com.vettrack.core.domain

enum class UserRole {
    VET,
    ADMIN
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