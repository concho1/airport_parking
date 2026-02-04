package com.example.airport_parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주차장 실시간 조회 응답(현재 상태)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRealtimeResponse {

    @Schema(description = "서버 기준 마지막 갱신 시각(응답 생성 시각)", example = "2026-02-04T14:30:00")
    private LocalDateTime lastUpdated;

    @Schema(description = "터미널", example = "T1")
    private String terminal;

    @Schema(description = "주차 타입", example = "long")
    private String type;

    @Schema(description = "동편 목록")
    private List<ParkingLotItem> east;

    @Schema(description = "서편 목록")
    private List<ParkingLotItem> west;
}
