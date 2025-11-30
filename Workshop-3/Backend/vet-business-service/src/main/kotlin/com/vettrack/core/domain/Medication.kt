package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "medication")
class Medication(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @Column(nullable = false)
    var name: String,

    var dosage: String? = null,   // "25 mg"
    var route: String? = null,    // "PO", "IM", etc.
    var frequency: String? = null,// "BID", "q12h", etc.

    @Column(name = "start_date")
    var startDate: LocalDate? = null,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Column(name = "last_administered_at")
    var lastAdministeredAt: OffsetDateTime? = null,

    @Column(name = "next_due_at")
    var nextDueAt: OffsetDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "medication", fetch = FetchType.LAZY)
    var doseEvents: MutableList<DoseEvent> = mutableListOf()
) {
    fun isActive(at: OffsetDateTime = OffsetDateTime.now()): Boolean {
        val today = at.toLocalDate()
        return (startDate == null || !today.isBefore(startDate)) &&
                (endDate == null || !today.isAfter(endDate))
    }
}
