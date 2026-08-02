package com.kb.auth.member.adapter.in.web;

import com.kb.auth.member.adapter.in.web.dto.ChangeRoleRequest;
import com.kb.auth.member.adapter.in.web.dto.MemberPageResponse;
import com.kb.auth.member.adapter.in.web.dto.MemberResponse;
import com.kb.auth.member.application.port.in.ChangeRoleUseCase;
import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final FindMemberUseCase findMemberUseCase;
    private final ChangeRoleUseCase changeRoleUseCase;

    @GetMapping
    public ResponseEntity<MemberPageResponse> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                MemberPageResponse.from(findMemberUseCase.findAll(PageRequest.of(page, size)))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
        MemberResult result = findMemberUseCase.findById(id);
        return ResponseEntity.ok(MemberResponse.from(result));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<MemberResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        MemberResult result = changeRoleUseCase.changeRole(request.toCommand(id));
        return ResponseEntity.ok(MemberResponse.from(result));
    }
}
