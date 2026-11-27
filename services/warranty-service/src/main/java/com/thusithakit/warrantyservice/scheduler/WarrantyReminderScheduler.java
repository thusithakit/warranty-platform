package com.thusithakit.warrantyservice.scheduler;

import com.thusithakit.warrantyservice.service.WarrantyReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WarrantyReminderScheduler {

  private static final Logger log =
      LoggerFactory.getLogger(WarrantyReminderScheduler.class);

  private final WarrantyReminderService reminderService;

  public WarrantyReminderScheduler(
      WarrantyReminderService reminderService
  ) {
    this.reminderService = reminderService;
  }

  @Scheduled(
      cron = "${app.reminders.cron:0 5 0 * * *}",
      zone = "Asia/Colombo"
  )
  public void generateDueReminders() {
    int generated = reminderService.generateDueReminders();

    log.info(
        "Warranty reminder generation completed. Generated {} reminders",
        generated
    );
  }
}