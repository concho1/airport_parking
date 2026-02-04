package com.example.airport_parking.repository;

import com.example.airport_parking.entity.ParkingLotStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * ParkingLotStatusEntity 접근용 Repository
 * - JpaRepository 제공 메서드(save/findById/findAll 등) + 필요한 조회 메서드 추가
 */
public interface ParkingLotStatusRepository extends JpaRepository<ParkingLotStatusEntity, Long> {

    Optional<ParkingLotStatusEntity> findByLotCodeAndTerminalAndParkingTypeAndAreaSide(
            String lotCode,
            String terminal,
            String parkingType,
            String areaSide
    );

    List<ParkingLotStatusEntity> findAllByTerminalAndParkingType(String terminal, String parkingType);

    List<ParkingLotStatusEntity> findAllByTerminalAndParkingTypeAndAreaSide(String terminal, String parkingType, String areaSide);
}
