package com.jude.blocker.controller;

import com.jude.blocker.dto.GateCmdRequest;
import com.jude.blocker.service.GateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gate")
@RequiredArgsConstructor
public class GateController {

    private final GateService gateService;

    @PostMapping("/cmd")
    public Map<String, Object> cmd(@RequestBody GateCmdRequest req) {
        return gateService.processMobileCommand(req);
    }
}