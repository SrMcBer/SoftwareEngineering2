package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "reminder")
class Reminder(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @Column(nullable = false)
    var title: String,

    @Column(name = "due_at", nullable = false)
    var dueAt: OffsetDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReminderStatus = ReminderStatus.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun markDone() {
        status = ReminderStatus.DONE
        updatedAt = OffsetDateTime.now()
    }

    fun isOverdue(now: OffsetDateTime = OffsetDateTime.now()): Boolean =
        status == ReminderStatus.PENDING && dueAt.isBefore(now)
}