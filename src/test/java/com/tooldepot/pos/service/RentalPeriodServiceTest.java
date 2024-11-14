package com.tooldepot.pos.service;

import com.tooldepot.pos.domain.RentalPeriod;
import com.tooldepot.pos.domain.Tool;
import com.tooldepot.pos.domain.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static com.tooldepot.pos.util.BigDecimalUtil.newBD;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class RentalPeriodServiceTest {
    @Autowired
    private RentalPeriodService rentalPeriodService;

    @Test
    public void testGetRentalPeriodAllDaysCharged() {
        Tool toolAllDaysCharged = new Tool("LADW", ToolType.LADDER, "Werner",
                newBD("1.99"), true, true, true);

        testRentalPeriodCalculations(3, toolAllDaysCharged, LocalDate.of(2024, 11, 11), 3);   // Mon, 3 days, no holidays
        testRentalPeriodCalculations(4, toolAllDaysCharged, LocalDate.of(2024, 11, 16), 4);   // Sat, 4 days, no holidays
    }

    @Test
    public void testGetRentalPeriodNoWeekendCharge() {
        Tool toolNoWeekendCharge = new Tool("LADW", ToolType.LADDER, "Werner",
                newBD("1.99"), true, false, true);

        testRentalPeriodCalculations(3, toolNoWeekendCharge, LocalDate.of(2024, 11, 16), 5);   // Sat, 5 days, no holidays
        testRentalPeriodCalculations(1, toolNoWeekendCharge, LocalDate.of(2024, 11, 15), 2);   // Fri, 2 days, no holidays
        testRentalPeriodCalculations(1, toolNoWeekendCharge, LocalDate.of(2024, 11, 17), 2);   // Sun, 2 days, no holidays
        testRentalPeriodCalculations(0, toolNoWeekendCharge, LocalDate.of(2024, 11, 16), 2);   // Sat, 2 days, no holidays
        testRentalPeriodCalculations(5, toolNoWeekendCharge, LocalDate.of(2024, 11, 16), 9);   // Sat, 9 days, no holidays
        testRentalPeriodCalculations(5, toolNoWeekendCharge, LocalDate.of(2024, 11, 17), 7);   // Sun, 7 days, no holidays
        testRentalPeriodCalculations(21, toolNoWeekendCharge, LocalDate.of(2024, 11, 1), 31);   // Fri, 31 days, no holidays

        testRentalPeriodCalculations(0, toolNoWeekendCharge, LocalDate.of(2024, 11, 1), 0);   // Fri, 0 days, no holidays
    }

    @Test
    public void testGetRentalPeriodNoWeekdayCharge() {
        Tool toolNoWeekendCharge = new Tool("LADW", ToolType.LADDER, "Werner",
                newBD("1.99"), false, true, true);

        testRentalPeriodCalculations(2, toolNoWeekendCharge, LocalDate.of(2024, 11, 16), 5);   // Sat, 5 days, no holidays
        testRentalPeriodCalculations(1, toolNoWeekendCharge, LocalDate.of(2024, 11, 15), 2);   // Fri, 2 days, no holidays
        testRentalPeriodCalculations(1, toolNoWeekendCharge, LocalDate.of(2024, 11, 17), 2);   // Sun, 2 days, no holidays
        testRentalPeriodCalculations(2, toolNoWeekendCharge, LocalDate.of(2024, 11, 16), 2);   // Sat, 2 days, no holidays
        testRentalPeriodCalculations(4, toolNoWeekendCharge, LocalDate.of(2024, 11, 16), 9);   // Sat, 9 days, no holidays
        testRentalPeriodCalculations(2, toolNoWeekendCharge, LocalDate.of(2024, 11, 17), 7);   // Sun, 7 days, no holidays
        testRentalPeriodCalculations(10, toolNoWeekendCharge, LocalDate.of(2024, 11, 1), 31);   // Fri, 31 days, no holidays

        testRentalPeriodCalculations(0, toolNoWeekendCharge, LocalDate.of(2024, 11, 1), 0);   // Fri, 0 days, no holidays
    }

    private void testRentalPeriodCalculations(int expectedChargeDays, Tool testTool, LocalDate checkoutDate, int rentalDays) {
        RentalPeriod period = rentalPeriodService.getRentalPeriod(testTool, checkoutDate, rentalDays);

        assertAll("rentalPeriod",
                () -> assertEquals(expectedChargeDays, period.chargeDays()),
                () -> assertEquals(rentalDays, period.rentalDays()),
                () -> assertEquals(checkoutDate, period.checkoutDate()),
                () -> assertEquals(checkoutDate.plusDays(rentalDays), period.returnDate())
        );
    }
}