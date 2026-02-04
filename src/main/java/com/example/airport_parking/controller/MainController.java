package com.example.airport_parking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(
        name = "페이지",
        description = "주차장 실시간 조회 화면"
)
@Controller
public class MainController {

    @Operation(
            summary = "메인 페이지 조회",
            description = """
            주차장 실시간 조회 메인 화면을 반환합니다.
            - / 또는 /home 으로 접근 가능
            - 현재는 더미 데이터를 사용합니다.
            """
    )
    @GetMapping({"/", "/home"})
    public String center() {
        return "/pages/home.html";
    }
}
