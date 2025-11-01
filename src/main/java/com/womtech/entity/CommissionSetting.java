package com.womtech.entity;

import com.womtech.enums.CommissionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "commission_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionType type;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(nullable = false)
    private Double rate;

	@UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

}