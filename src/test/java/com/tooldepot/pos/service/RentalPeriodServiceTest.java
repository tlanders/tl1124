package com.tooldepot.pos.service;

import com.tooldepot.pos.domain.RentalPeriod;
import com.tooldepot.pos.domain.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class RentalPeriodServiceTest {
    @Autowired
    private RentalPeriodService rentalPeriodService;

    @Test
    public void testGetRentalPeriodAllDaysCharged() {
        testRentalPeriodCalculations(3, true, true, true,
                LocalDate.of(2024, 11, 11), 3);   // Mon, 3 days, no holidays
        testRentalPeriodCalculations(4, true, true, true,
                LocalDate.of(2024, 11, 16), 4);   // Sat, 4 days, no holidays
    }

    @Test
    public void testGetRentalPeriodNoHolidayCharge() {
        ToolType noHolidayChargeToolType = ToolType.Ladder;

        testRentalPeriodCalculations(5, noHolidayChargeToolType, LocalDate.of(2024, 11, 16), 5);   // Sat, 5 days, no holidays
        testRentalPeriodCalculations(2, noHolidayChargeToolType, LocalDate.of(2024, 11, 15), 2);   // Fri, 2 days, no holidays
        testRentalPeriodCalculations(2, noHolidayChargeToolType, LocalDate.of(2024, 11, 17), 2);   // Sun, 2 days, no holidays

        // July 4th is Thu 7/4/2024
        testRentalPeriodCalculations(2, noHolidayChargeToolType, LocalDate.of(2024, 7, 3), 3);

        // Labor Day is Mon 9/2/2024
        testRentalPeriodCalculations(2, noHolidayChargeToolType, LocalDate.of(2024, 9, 1), 3);

        testRentalPeriodCalculations(0, noHolidayChargeToolType, LocalDate.of(2024, 11, 1), 0);   // Fri, 0 days, no holidays
    }

    @Test
    public void testGetRentalPeriodNoWeekendCharge() {
        ToolType noWeekendChargeToolType = ToolType.Chainsaw;

        testRentalPeriodCalculations(3, noWeekendChargeToolType, LocalDate.of(2024, 11, 16), 5);   // Sat, 5 days, no holidays
        testRentalPeriodCalculations(1, noWeekendChargeToolType, LocalDate.of(2024, 11, 15), 2);   // Fri, 2 days, no holidays
        testRentalPeriodCalculations(1, noWeekendChargeToolType, LocalDate.of(2024, 11, 17), 2);   // Sun, 2 days, no holidays
        testRentalPeriodCalculations(0, noWeekendChargeToolType, LocalDate.of(2024, 11, 16), 2);   // Sat, 2 days, no holidays
        testRentalPeriodCalculations(5, noWeekendChargeToolType, LocalDate.of(2024, 11, 16), 9);   // Sat, 9 days, no holidays
        testRentalPeriodCalculations(5, noWeekendChargeToolType, LocalDate.of(2024, 11, 17), 7);   // Sun, 7 days, no holidays
        testRentalPeriodCalculations(21, noWeekendChargeToolType, LocalDate.of(2024, 11, 1), 31);   // Fri, 31 days, no holidays

        testRentalPeriodCalculations(0, noWeekendChargeToolType, LocalDate.of(2024, 11, 1), 0);   // Fri, 0 days, no holidays
    }

    @Test
    public void testGetRentalPeriodNoWeekdayCharge() {
        testRentalPeriodCalculations(2, false, true, true,
                LocalDate.of(2024, 11, 16), 5);   // Sat, 5 days, no holidays
        testRentalPeriodCalculations(1, false, true, true,
                LocalDate.of(2024, 11, 15), 2);   // Fri, 2 days, no holidays
        testRentalPeriodCalculations(1, false, true, true,
                LocalDate.of(2024, 11, 17), 2);   // Sun, 2 days, no holidays
        testRentalPeriodCalculations(2, false, true, true,
                LocalDate.of(2024, 11, 16), 2);   // Sat, 2 days, no holidays
        testRentalPeriodCalculations(4, false, true, true,
                LocalDate.of(2024, 11, 16), 9);   // Sat, 9 days, no holidays
        testRentalPeriodCalculations(2, false, true, true,
                LocalDate.of(2024, 11, 17), 7);   // Sun, 7 days, no holidays
        testRentalPeriodCalculations(10, false, true, true,
                LocalDate.of(2024, 11, 1), 31);   // Fri, 31 days, no holidays

        testRentalPeriodCalculations(0, false, true, true,
                LocalDate.of(2024, 11, 1), 0);   // Fri, 0 days, no holidays
    }

    @Test
    public void testGetRentalPeriod_invalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> rentalPeriodService.getRentalPeriod(ToolType.Chainsaw, LocalDate.of(2024, 11, 11), -1));
        assertThrows(NullPointerException.class, () -> rentalPeriodService.getRentalPeriod(null, LocalDate.of(2024, 11, 11), 2));
        assertThrows(NullPointerException.class, () -> rentalPeriodService.getRentalPeriod(ToolType.Chainsaw, null, 1));
    }

    private void testRentalPeriodCalculations(int expectedChargeDays, ToolType testToolType, LocalDate checkoutDate, int rentalDays) {
        RentalPeriod period = rentalPeriodService.getRentalPeriod(testToolType, checkoutDate, rentalDays);

        assertAll("rentalPeriod",
                () -> assertEquals(expectedChargeDays, period.chargeDays()),
                () -> assertEquals(rentalDays, period.rentalDays()),
                () -> assertEquals(checkoutDate, period.checkoutDate()),
                () -> assertEquals(checkoutDate.plusDays(rentalDays), period.returnDate())
        );
    }

    private void testRentalPeriodCalculations(int expectedChargeDays, boolean isWeekdayCharge, boolean isWeekendCharge,
            boolean isHolidayCharge, LocalDate checkoutDate, int rentalDays) {
        RentalPeriod period = rentalPeriodService.getRentalPeriod(checkoutDate, rentalDays, isWeekdayCharge, isWeekendCharge, isHolidayCharge);

        assertAll("rentalPeriod",
                () -> assertEquals(expectedChargeDays, period.chargeDays()),
                () -> assertEquals(rentalDays, period.rentalDays()),
                () -> assertEquals(checkoutDate, period.checkoutDate()),
                () -> assertEquals(checkoutDate.plusDays(rentalDays), period.returnDate())
        );
    }
}