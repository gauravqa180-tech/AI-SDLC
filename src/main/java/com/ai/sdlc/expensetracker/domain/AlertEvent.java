package com.ai.sdlc.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "alert_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_alert_events_month_scope_threshold",
                        columnNames = {"event_month", "scope", "category", "threshold"}
                )
        },
        indexes = {
                @Index(name = "idx_alert_events_month", columnList = "event_month")
        }
)
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** YYYY-MM */
    @Column(name = "event_month", nullable = false, length = 7)
    private String month;

    /** OVERALL or CATEGORY */
    @Column(name = "scope", nullable = false, length = 20)
    private String scope;

    /** Only for CATEGORY scope */
    @Column(name = "category", length = 100)
    private String category;

    /** 0.80 or 1.00 */
    @Column(name = "threshold", nullable = false)
    private Double threshold;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
