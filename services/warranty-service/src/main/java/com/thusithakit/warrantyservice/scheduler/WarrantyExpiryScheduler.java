package com.thusithakit.warrantyservice.scheduler;

import com.thusithakit.warrantyservice.service.WarrantyExpiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WarrantyExpiryScheduler {

  private static final Logger log =
      LoggerFactory.getLogger(WarrantyExpiryScheduler.class);

  private final WarrantyExpiryService expiryService;

  public WarrantyExpiryScheduler(WarrantyExpiryService expiryService) {
    this.expiryService = expiryService;
  }

  @Scheduled(fixedDelayString = "${app.expiry.poll-interval-ms:60000}")
  public void processExpiredWarranties() {

    int expiredCount = expiryService.expireWarranties();

    if (expiredCount > 0) {
      log.info("Expired {} warranties", expiredCount);
    }
  }
}