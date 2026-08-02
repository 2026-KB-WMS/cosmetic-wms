package com.kb.auth.member.adapter.in.web;

import com.kb.auth.member.adapter.in.web.dto.MemberPageResponse;
import com.kb.auth.member.application.port.in.FindMemberUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final FindMemberUseCase findMemberUseCase;

    @GetMapping
    public ResponseEntity<MemberPageResponse> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MemberPageResponse response = MemberPageResponse.from(
                findMemberUseCase.findAll(PageRequest.of(page, size))
        );
        return ResponseEntity.ok(response);
    }
}
