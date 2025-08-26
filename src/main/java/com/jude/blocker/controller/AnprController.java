package com.jude.blocker.controller;

import com.jude.blocker.dto.AnprEventRequest;
import com.jude.blocker.service.GateService;
import java.util.Map;
import lombok.*;
import org.springframework.web.bind.annotation.*;


// AnprController (ANPR)
@RestController
@RequestMapping("/anpr")
@RequiredArgsConstructor
public class AnprController {

    private final GateService gateService;

    @PostMapping("/event")
    public Map<String, Object> onEvent(@RequestBody AnprEventRequest ev) {
        return gateService.processAnprEvent(ev);
    }
}