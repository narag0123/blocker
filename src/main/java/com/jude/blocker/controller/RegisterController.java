package com.jude.blocker.controller;

import com.jude.blocker.dto.RegisterRequest;
import com.jude.blocker.entity.WhiteListEntry;
import com.jude.blocker.repository.WhiteListRepository;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/whitelist")
@RequiredArgsConstructor
@CrossOrigin(
    origins = "http://localhost:3000",
    allowCredentials = "false"
)
public class RegisterController {
    private final WhiteListRepository whitelistRepository;

    @PostMapping
    public Map<String, Object> create(@RequestBody RegisterRequest req) {
        final String plate = req.getPlate() == null ? "" : req.getPlate().trim();
        final String owner = req.getOwner() == null ? null : req.getOwner().trim();

        if(plate.isBlank()){
            return Map.of("ok", false, "error", "plate_required");
        }

        if(whitelistRepository.existsByPlate(plate)){
            return Map.of("ok", false, "error", "already_exists");
        }

        try {
            WhiteListEntry saved = whitelistRepository.save(
                WhiteListEntry.builder()
                    .plate(plate)
                    .owner(owner)
                    .createdAt(Instant.now())
                    .build()
            );
            return Map.of("ok", true, "id", saved.getId(), "plate", saved.getPlate());

        } catch(DataIntegrityViolationException e) {
            return Map.of("ok", false, "error", "duplicate_or_constraint");
        }
    }

}
