package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "patient")
class Patient(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: Owner? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var species: String,   // e.g. "Canine", "Feline"

    var breed: String? = null,
    var sex: String? = null,
    var dob: LocalDate? = null,
    var color: String? = null,

    @Column(name = "microchip_id", unique = true)
    var microchipId: String? = null,

    var allergies: String? = null,
    var notes: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var visits: MutableList<Visit> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var medications: MutableList<Medication> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var reminders: MutableList<Reminder> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var exams: MutableList<Exam> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var attachments: MutableList<Attachment> = mutableListOf()
) {
    // Example of aggregate-like behavior (matches CRC responsibilities)
    fun lastVisit(): Visit? = visits.maxByOrNull { it.dateTime }
}