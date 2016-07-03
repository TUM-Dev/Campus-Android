package de.tum.in.tumcampusapp.auxiliary;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Hardcoded cafeteria prices
 */
public final class CafeteriaPrices {
    private static final Map<String, String> EMPLOYEE_PRICES;

    private static final Map<String, String> GUEST_PRICES;
    private static final Map<String, String> STUDENT_PRICES;
    private static final String
            PRICE_100 = "1,00",
            PRICE_155 = "1,55",
            PRICE_190 = "1,90",
            PRICE_220 = "2,20",
            PRICE_240 = "2,40",
            PRICE_260 = "2,60",
            PRICE_270 = "2,70",
            PRICE_280 = "2,80",
            PRICE_290 = "2,90",
            PRICE_300 = "3,00",
            PRICE_320 = "3,20",
            PRICE_330 = "3,30",
            PRICE_340 = "3,40",
            PRICE_350 = "3,50",
            PRICE_360 = "3,60",
            PRICE_370 = "3,70",
            PRICE_390 = "3,90",
            PRICE_400 = "4,00",
            PRICE_410 = "4,10",
            PRICE_440 = "4,40",
            PRICE_450 = "4,50",
            PRICE_490 = "4,90",
            PRICE_540 = "5,40";

    static {
        STUDENT_PRICES = new HashMap<>();
        STUDENT_PRICES.put("Tagesgericht 1", PRICE_100);
        STUDENT_PRICES.put("Tagesgericht 2", PRICE_155);
        STUDENT_PRICES.put("Tagesgericht 3", PRICE_190);
        STUDENT_PRICES.put("Tagesgericht 4", PRICE_240);

        STUDENT_PRICES.put("Aktionsessen 1", PRICE_155);
        STUDENT_PRICES.put("Aktionsessen 2", PRICE_190);
        STUDENT_PRICES.put("Aktionsessen 3", PRICE_240);
        STUDENT_PRICES.put("Aktionsessen 4", PRICE_260);
        STUDENT_PRICES.put("Aktionsessen 5", PRICE_280);
        STUDENT_PRICES.put("Aktionsessen 6", PRICE_300);
        STUDENT_PRICES.put("Aktionsessen 7", PRICE_320);
        STUDENT_PRICES.put("Aktionsessen 8", PRICE_350);
        STUDENT_PRICES.put("Aktionsessen 9", PRICE_400);
        STUDENT_PRICES.put("Aktionsessen 10", PRICE_450);

        STUDENT_PRICES.put("Biogericht 1", PRICE_155);
        STUDENT_PRICES.put("Biogericht 2", PRICE_190);
        STUDENT_PRICES.put("Biogericht 3", PRICE_240);
        STUDENT_PRICES.put("Biogericht 4", PRICE_260);
        STUDENT_PRICES.put("Biogericht 5", PRICE_280);
        STUDENT_PRICES.put("Biogericht 6", PRICE_300);
        STUDENT_PRICES.put("Biogericht 7", PRICE_320);
        STUDENT_PRICES.put("Biogericht 8", PRICE_350);
        STUDENT_PRICES.put("Biogericht 9", PRICE_400);
        STUDENT_PRICES.put("Biogericht 10", PRICE_450);

        EMPLOYEE_PRICES = new HashMap<>();
        EMPLOYEE_PRICES.put("Tagesgericht 1", PRICE_190);
        EMPLOYEE_PRICES.put("Tagesgericht 2", PRICE_220);
        EMPLOYEE_PRICES.put("Tagesgericht 3", PRICE_240);
        EMPLOYEE_PRICES.put("Tagesgericht 4", PRICE_280);

        EMPLOYEE_PRICES.put("Aktionsessen 1", PRICE_220);
        EMPLOYEE_PRICES.put("Aktionsessen 2", PRICE_240);
        EMPLOYEE_PRICES.put("Aktionsessen 3", PRICE_280);
        EMPLOYEE_PRICES.put("Aktionsessen 4", PRICE_300);
        EMPLOYEE_PRICES.put("Aktionsessen 5", PRICE_320);
        EMPLOYEE_PRICES.put("Aktionsessen 6", PRICE_340);
        EMPLOYEE_PRICES.put("Aktionsessen 7", PRICE_360);
        EMPLOYEE_PRICES.put("Aktionsessen 8", PRICE_390);
        EMPLOYEE_PRICES.put("Aktionsessen 9", PRICE_440);
        EMPLOYEE_PRICES.put("Aktionsessen 10", PRICE_490);

        EMPLOYEE_PRICES.put("Biogericht 1", PRICE_220);
        EMPLOYEE_PRICES.put("Biogericht 2", PRICE_240);
        EMPLOYEE_PRICES.put("Biogericht 3", PRICE_280);
        EMPLOYEE_PRICES.put("Biogericht 4", PRICE_300);
        EMPLOYEE_PRICES.put("Biogericht 5", PRICE_320);
        EMPLOYEE_PRICES.put("Biogericht 6", PRICE_340);
        EMPLOYEE_PRICES.put("Biogericht 7", PRICE_360);
        EMPLOYEE_PRICES.put("Biogericht 8", PRICE_390);
        EMPLOYEE_PRICES.put("Biogericht 9", PRICE_440);
        EMPLOYEE_PRICES.put("Biogericht 10", PRICE_490);

        GUEST_PRICES = new HashMap<>();
        GUEST_PRICES.put("Tagesgericht 1", PRICE_240);
        GUEST_PRICES.put("Tagesgericht 2", PRICE_270);
        GUEST_PRICES.put("Tagesgericht 3", PRICE_290);
        GUEST_PRICES.put("Tagesgericht 4", PRICE_330);

        GUEST_PRICES.put("Aktionsessen 1", PRICE_270);
        GUEST_PRICES.put("Aktionsessen 2", PRICE_290);
        GUEST_PRICES.put("Aktionsessen 3", PRICE_330);
        GUEST_PRICES.put("Aktionsessen 4", PRICE_350);
        GUEST_PRICES.put("Aktionsessen 5", PRICE_370);
        GUEST_PRICES.put("Aktionsessen 6", PRICE_390);
        GUEST_PRICES.put("Aktionsessen 7", PRICE_410);
        GUEST_PRICES.put("Aktionsessen 8", PRICE_440);
        GUEST_PRICES.put("Aktionsessen 9", PRICE_490);
        GUEST_PRICES.put("Aktionsessen 10", PRICE_540);

        GUEST_PRICES.put("Biogericht 1", PRICE_270);
        GUEST_PRICES.put("Biogericht 2", PRICE_290);
        GUEST_PRICES.put("Biogericht 3", PRICE_330);
        GUEST_PRICES.put("Biogericht 4", PRICE_350);
        GUEST_PRICES.put("Biogericht 5", PRICE_370);
        GUEST_PRICES.put("Biogericht 6", PRICE_390);
        GUEST_PRICES.put("Biogericht 7", PRICE_410);
        GUEST_PRICES.put("Biogericht 8", PRICE_440);
        GUEST_PRICES.put("Biogericht 9", PRICE_490);
        GUEST_PRICES.put("Biogericht 10", PRICE_540);
    }


    private CafeteriaPrices() {
        // CafeteriaPrices is a utility class
    }

    /**
     * Gets a {@link Map} which hashes cafeteria menu's long title
     * to prices.
     *
     * @param context Context
     * @return hash map
     */
    public static Map<String, String> getRolePrices(Context context) {
        String type = Utils.getSetting(context, Const.ROLE, "");
        switch (type) {
            case "0":
                return STUDENT_PRICES;
            case "1":
                return EMPLOYEE_PRICES;
            case "2":
                return GUEST_PRICES;
            default:
                return STUDENT_PRICES;
        }
    }
}
