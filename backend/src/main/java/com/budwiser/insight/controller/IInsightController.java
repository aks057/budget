package com.budwiser.insight.controller;

import com.budwiser.common.response.Response;
import com.budwiser.insight.dto.InsightDtos.InsightDto;
import com.budwiser.insight.dto.InsightDtos.MarkAllReadDto;
import com.budwiser.insight.dto.InsightDtos.RefreshResultDto;
import com.budwiser.insight.dto.InsightDtos.UnreadCountDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insights")
public interface IInsightController {

  /** Most recent 50, newest first. */
  @GetMapping
  Response<List<InsightDto>> list();

  @GetMapping("/unread-count")
  Response<UnreadCountDto> unreadCount();

  @PostMapping("/{id}/read")
  Response<Void> markRead(@PathVariable Long id);

  @PostMapping("/read-all")
  Response<MarkAllReadDto> markAllRead();

  /** Evaluate the rules for the current user now; idempotent, so repeated calls only add genuinely new facts. */
  @PostMapping("/refresh")
  Response<RefreshResultDto> refresh();
}
