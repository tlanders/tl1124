package com.tooldepot.pos.service;

import com.tooldepot.pos.domain.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class CheckoutService {
    @Autowired
    private ToolService toolService;

    public void checkout(String toolCode, int rentalDays,
                         int discountPercent, LocalDate checkoutDate
    ) throws PosServiceException {
        if(rentalDays <= 0) {
            throw new PosServiceException("Rental days must be greater than 0");
        }
        if(discountPercent < 0 || discountPercent > 100) {
            throw new PosServiceException("Discount percent must be between 0 and 100");
        }

        Optional<Tool> tool = toolService.getTool(toolCode);
        if(tool.isEmpty()) {
            throw new PosServiceException("Tool " + toolCode + " not found");
        }

        log.debug("Checkout service called - tool={}, rentalDays={}, discountPercent={}, checkoutDate={}",
                toolCode, rentalDays, discountPercent, checkoutDate);
    }
}