package com.example.airport_parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "단일 주차장 상태 항목")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLotItem {

    @Schema(description = "주차장 코드", example = "T1_L_E_P1")
    private String lotCode;

    @Schema(description = "주차장 이름", example = "장기주차장 P1")
    private String lotName;

    @Schema(description = "구역", example = "east")
    private String areaSide;

    @Schema(description = "총면수", example = "1200")
    private int capacity;

    @Schema(description = "가용면수", example = "120")
    private int available;

    @Schema(description = "상태(OK/BUSY/FULL/UNKNOWN)", example = "BUSY")
    private String status;

    @Schema(description = "원본 데이터 기준 시각(있는 경우)", example = "2026-02-04T14:29:50")
    private LocalDateTime sourceTs;

    @Schema(description = "DB 저장/갱신 시각", example = "2026-02-04T14:30:00")
    private LocalDateTime updatedAt;
}