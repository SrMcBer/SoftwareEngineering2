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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
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
    var status: ExamStatus = ExamStatus.draft,

    var notes: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    var attachments: MutableList<Attachment> = mutableListOf()
) {
    fun finish() {
        status = ExamStatus.final
        updatedAt = OffsetDateTime.now()
    }
    /**
     * Creates a shallow copy of the Exam object for auditing and tracking changes.
     * This captures the 'before' state of the mutable fields (JSONs, status, notes).
     */
    fun shallowCopy(): Exam {
        return Exam(
            id = this.id,
            patient = this.patient,
            visit = this.visit,
            template = this.template,
            templateVersion = this.templateVersion,
            performedAt = this.performedAt,
            performedBy = this.performedBy,
            vitalsJson = this.vitalsJson,
            resultsJson = this.resultsJson,
            status = this.status,
            notes = this.notes,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
            // Note: Lazy collections (attachments) are intentionally not copied.
        )
    }
}