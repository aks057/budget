package com.budwiser.insight.controller.impl;

import com.budwiser.common.response.Response;
import com.budwiser.insight.controller.IInsightController;
import com.budwiser.insight.dto.InsightDtos.InsightDto;
import com.budwiser.insight.dto.InsightDtos.MarkAllReadDto;
import com.budwiser.insight.dto.InsightDtos.RefreshResultDto;
import com.budwiser.insight.dto.InsightDtos.UnreadCountDto;
import com.budwiser.insight.service.IInsightService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsightController implements IInsightController {
  private final IInsightService insightService;

  @Override
  public Response<List<InsightDto>> list() {
    return Response.<List<InsightDto>>builder().data(insightService.list()).build();
  }

  @Override
  public Response<UnreadCountDto> unreadCount() {
    return Response.<UnreadCountDto>builder().data(insightService.unreadCount()).build();
  }

  @Override
  public Response<Void> markRead(Long id) {
    insightService.markRead(id);
    return Response.<Void>builder().build();
  }

  @Override
  public Response<MarkAllReadDto> markAllRead() {
    return Response.<MarkAllReadDto>builder().data(insightService.markAllRead()).build();
  }

  @Override
  public Response<RefreshResultDto> refresh() {
    return Response.<RefreshResultDto>builder().data(insightService.refresh()).build();
  }
}
