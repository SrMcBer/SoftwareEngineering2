package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "exam")
class Exam(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    var visit: Visit? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    var template: ExamTemplate,

    @Column(name = "template_version", nullable = false)
    var templateVersion: Int,

    @Column(name = "performed_at", nullable = false)
    var performedAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    var performedBy: User? = null,

    @Column(name = "vitals_json", columnDefinition = "jsonb")
    var vitalsJson: String? = null,

    @Column(name = "results_json", columnDefinition = "jsonb", nullable = false)
    var resultsJson: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ExamStatus = ExamStatus.DRAFT,

    var notes: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    var attachments: MutableList<Attachment> = mutableListOf()
) {
    fun finish() {
        status = ExamStatus.FINAL
        updatedAt = OffsetDateTime.now()
    }
}