package com.br.techbuilderun.dto;

import java.util.List;

public record FeedDto(List<FeedItemDto> feedItems,int page, int pageSize, int totalPages, Long totalElements) {
}
