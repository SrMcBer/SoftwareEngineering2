package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "attachment")
class Attachment(

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    var exam: Exam? = null,

    @Column(nullable = false)
    var type: String,   // image/pdf/video

    @Column(nullable = false)
    var url: String,

    var filename: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    var uploadedBy: User? = null,

    @Column(name = "uploaded_at", nullable = false)
    var uploadedAt: OffsetDateTime = OffsetDateTime.now()
) {
    init {
        // Mirror DB constraint: must be linked to visit XOR exam (but always patient)
        require(!(visit != null && exam != null)) {
            "Attachment cannot be linked to both a visit and an exam"
        }
    }
}