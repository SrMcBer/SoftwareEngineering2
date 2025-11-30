package com.vettrack.vetbusinessservice.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "visit")
class Visit(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @Column(name = "date_time", nullable = false)
    var dateTime: OffsetDateTime = OffsetDateTime.now(),

    var reason: String? = null,

    // You can later switch this to a proper JSONB mapping with a custom type
    @Column(name = "vitals_json", columnDefinition = "jsonb")
    var vitalsJson: String? = null,

    @Column(name = "exam_notes")
    var examNotes: String? = null,

    var diagnoses: String? = null,
    var procedures: String? = null,
    var recommendations: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "visit", fetch = FetchType.LAZY)
    var exams: MutableList<Exam> = mutableListOf(),

    @OneToMany(mappedBy = "visit", fetch = FetchType.LAZY)
    var attachments: MutableList<Attachment> = mutableListOf()
)
