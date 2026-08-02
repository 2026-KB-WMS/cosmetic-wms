package com.kb.auth.member.adapter.in.web.dto;

import com.kb.auth.member.application.port.in.dto.MemberResult;
import org.springframework.data.domain.Page;

import java.util.List;

public record MemberPageResponse(
        List<MemberResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static MemberPageResponse from(Page<MemberResult> page) {
        return new MemberPageResponse(
                page.getContent().stream().map(MemberResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
