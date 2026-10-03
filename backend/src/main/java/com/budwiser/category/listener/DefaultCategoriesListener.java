package com.budwiser.category.listener;

import com.budwiser.auth.event.UserRegisteredEvent;
import com.budwiser.category.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds starter categories for new users. Decoupled from auth via an event: auth doesn't know categories exist.
 */
@Component
@RequiredArgsConstructor
public class DefaultCategoriesListener {
  private final ICategoryService categoryService;

  @EventListener
  public void onUserRegistered(UserRegisteredEvent event) {
    categoryService.createDefaultCategories(event.userId());
  }
}
