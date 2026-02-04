package com.example.airport_parking.service;

import com.example.airport_parking.dto.ParkingLotItem;
import com.example.airport_parking.entity.ParkingLotStatusEntity;
import com.example.airport_parking.repository.ParkingLotStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주차장 상태 서비스
 * - 폴링 결과를 저장/갱신(upsert)
 * - 터미널/타입 기준 조회
 *
 * 운영 팁:
 * - 실시간 표출 트래픽이 많아지면 DB 조회를 Cache/Redis로 흡수하는 것을 권장
 */
@Service
@RequiredArgsConstructor
public class ParkingLotStatusService {

    private final ParkingLotStatusRepository repository;

    /**
     * 주차장 상태 업서트(있으면 갱신 / 없으면 생성)
     * - 유니크 키: lotCode + terminal + parkingType + areaSide
     */
    @Transactional
    public ParkingLotStatusEntity upsert(ParkingLotStatusEntity input) {
        var existingOpt = repository.findByLotCodeAndTerminalAndParkingTypeAndAreaSide(
                input.getLotCode(),
                input.getTerminal(),
                input.getParkingType(),
                input.getAreaSide()
        );

        if (existingOpt.isPresent()) {
            // 이미 존재 → 변경 필드만 업데이트
            ParkingLotStatusEntity e = existingOpt.get();
            e.setLotName(input.getLotName());
            e.setCapacity(input.getCapacity());
            e.setAvailable(input.getAvailable());
            e.setStatus(input.getStatus());
            e.setSourceTs(input.getSourceTs());
            // updatedAt은 @PreUpdate에서 자동 세팅
            return repository.save(e);
        }

        // 신규 생성
        // updatedAt은 @PrePersist에서 자동 세팅
        return repository.save(input);
    }

    /**
     * 터미널/타입으로 조회 (east/west 모두)
     */
    public List<ParkingLotStatusEntity> getByTerminalAndType(String terminal, String parkingType) {
        return repository.findAllByTerminalAndParkingType(terminal, parkingType);
    }

    /**
     * 터미널/타입 + 구역(east/west)으로 조회
     */
    public List<ParkingLotStatusEntity> getByTerminalTypeAndSide(String terminal, String parkingType, String areaSide) {
        return repository.findAllByTerminalAndParkingTypeAndAreaSide(terminal, parkingType, areaSide);
    }

    /**
     * 개발용 더미 데이터 삽입(시드)
     * - API 연동 전 UI/조회 테스트를 위해 한 번 호출하면 됨
     */
    @Transactional
    public void seedDummy() {
        LocalDateTime now = LocalDateTime.now();

        upsert(ParkingLotStatusEntity.builder()
                .lotCode("T1_L_E_P1")
                .lotName("장기주차장 P1")
                .terminal("T1")
                .parkingType("long")
                .areaSide("east")
                .capacity(1200)
                .available(120)
                .status(calcStatus(120, 1200))
                .sourceTs(now.minusSeconds(10))
                .build());

        upsert(ParkingLotStatusEntity.builder()
                .lotCode("T1_L_W_P2")
                .lotName("장기주차장 P2")
                .terminal("T1")
                .parkingType("long")
                .areaSide("west")
                .capacity(900)
                .available(0)
                .status(calcStatus(0, 900))
                .sourceTs(now.minusSeconds(10))
                .build());

        upsert(ParkingLotStatusEntity.builder()
                .lotCode("T1_L_E_TOWER")
                .lotName("주차타워(동)")
                .terminal("T1")
                .parkingType("long")
                .areaSide("east")
                .capacity(500)
                .available(8)
                .status(calcStatus(8, 500))
                .sourceTs(now.minusSeconds(10))
                .build());
    }

    /**
     * 단순 혼잡도 계산 예시
     * - 실 서비스에서는 공공데이터의 상태값이 있으면 그대로 사용하거나,
     *   정책(예: 점유율 85% 이상 BUSY, 95% 이상 FULL)을 명시적으로 관리하는 것을 권장
     */
    public String calcStatus(int available, int capacity) {
        if (capacity <= 0) return "UNKNOWN";
        if (available <= 0) return "FULL";
        int used = capacity - available;
        double occ = (used * 100.0) / capacity;
        if (occ >= 95.0) return "FULL";
        if (occ >= 85.0) return "BUSY";
        return "OK";
    }
}
