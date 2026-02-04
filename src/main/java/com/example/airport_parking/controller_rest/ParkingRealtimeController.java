package com.example.airport_parking.controller_rest;

import com.example.airport_parking.dto.ParkingRealtimeResponse;
import com.example.airport_parking.dto.ParkingLotItem;
import com.example.airport_parking.entity.ParkingLotStatusEntity;
import com.example.airport_parking.service.ParkingLotStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "주차장 실시간 API",
        description = "공공데이터 폴링 결과(현재 상태)를 조회하기 위한 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking")
public class ParkingRealtimeController {

    private final ParkingLotStatusService service;

    @Operation(
            summary = "주차장 실시간 조회(현재 상태)",
            description = """
            터미널/주차타입 기준으로 현재 주차장 상태를 조회합니다.
            
            - 저장 구조: DB에는 '최신 상태'만 유지됩니다(폴링 결과로 upsert).
            - 화면 표출: UI는 이 API를 주기 호출하거나(SSE/WebSocket 전환 가능), 갱신 이벤트 기반으로 구성할 수 있습니다.
            
            응답은 동편(east) / 서편(west) 목록으로 분리해 제공합니다.
            """
    )
    @GetMapping("/realtime")
    public ParkingRealtimeResponse getRealtime(
            @Parameter(description = "터미널 (T1 또는 T2)", example = "T1")
            @RequestParam(defaultValue = "T1")
            @Pattern(regexp = "T1|T2", message = "terminal은 T1 또는 T2만 허용")
            String terminal,

            @Parameter(description = "주차 타입 (short 또는 long)", example = "long")
            @RequestParam(defaultValue = "long")
            @Pattern(regexp = "short|long", message = "type은 short 또는 long만 허용")
            String type
    ) {
        List<ParkingLotStatusEntity> east = service.getByTerminalTypeAndSide(terminal, type, "east");
        List<ParkingLotStatusEntity> west = service.getByTerminalTypeAndSide(terminal, type, "west");

        return ParkingRealtimeResponse.builder()
                .lastUpdated(LocalDateTime.now())
                .terminal(terminal)
                .type(type)
                .east(east.stream().map(this::toItem).toList())
                .west(west.stream().map(this::toItem).toList())
                .build();
    }

    @Operation(
            summary = "개발용: 더미 데이터 시드",
            description = """
            API 연동 전 UI/조회 테스트를 위해 더미 데이터를 DB에 upsert로 삽입합니다.
            
            ⚠ 운영 환경에서는 반드시 비활성화하거나 관리자 인증/권한을 적용하세요.
            """
    )
    @PostMapping("/admin/seed-dummy")
    public void seedDummy() {
        service.seedDummy();
    }

    private ParkingLotItem toItem(ParkingLotStatusEntity e) {
        return ParkingLotItem.builder()
                .lotCode(e.getLotCode())
                .lotName(e.getLotName())
                .areaSide(e.getAreaSide())
                .capacity(e.getCapacity())
                .available(e.getAvailable())
                .status(e.getStatus())
                .sourceTs(e.getSourceTs())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
