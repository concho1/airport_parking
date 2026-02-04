package com.example.airport_parking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주차장 "현재 상태" 엔티티
 * - 폴링으로 받은 최신 값만 유지/갱신하는 목적
 * - 이력(시간대별 변화)까지 저장하려면 별도 History 테이블을 두는 것을 권장
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "parking_lot_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_parking_lot",
                        columnNames = {"lot_code", "terminal", "parking_type", "area_side"}
                )
        },
        indexes = {
                @Index(name = "idx_terminal_type", columnList = "terminal, parking_type"),
                @Index(name = "idx_updated_at", columnList = "updated_at")
        }
)
public class ParkingLotStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 공공데이터 기준 주차장 코드(또는 내부 코드) */
    @Column(name = "lot_code", length = 64, nullable = false)
    private String lotCode;

    /** 주차장 이름 */
    @Column(name = "lot_name", length = 200, nullable = false)
    private String lotName;

    /** 터미널 (예: T1/T2) */
    @Column(name = "terminal", length = 8, nullable = false)
    private String terminal;

    /** 주차 타입 (예: short/long) */
    @Column(name = "parking_type", length = 16, nullable = false)
    private String parkingType;

    /** 구역 (예: east/west/unknown) */
    @Column(name = "area_side", length = 16, nullable = false)
    private String areaSide;

    /** 총면수 */
    @Column(name = "capacity", nullable = false)
    private int capacity;

    /** 가용면수 */
    @Column(name = "available", nullable = false)
    private int available;

    /** 상태 (OK/BUSY/FULL/UNKNOWN) */
    @Column(name = "status", length = 16, nullable = false)
    private String status;

    /** 원본 데이터의 기준 시각(공공데이터가 제공하는 경우) */
    @Column(name = "source_ts")
    private LocalDateTime sourceTs;

    /** 우리 서버에서 갱신한 시각 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 저장/갱신 직전에 updatedAt을 자동 세팅
     * - 폴링할 때마다 계속 UPDATE되므로, 최신 반영 시간을 표준화하기 좋음
     */
    @PrePersist
    @PreUpdate
    public void onUpdateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

