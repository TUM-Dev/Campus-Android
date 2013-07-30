package de.tum.in.tumcampusapp.auxiliary;

import java.util.HashMap;

public class CafetariaPrices {
	public static HashMap<String, String> student_prices;
	public static HashMap<String, String> employee_prices;
	public static HashMap<String, String> guest_prices;

	static {
		student_prices = new HashMap<String, String>();
		student_prices.put("Tagesgericht 1", "1,00");
		student_prices.put("Tagesgericht 2", "1,55");
		student_prices.put("Tagesgericht 3", "1,90");
		student_prices.put("Tagesgericht 4", "2,40");

		student_prices.put("Aktionsessen 1", "1,55");
		student_prices.put("Aktionsessen 2", "1,90");
		student_prices.put("Aktionsessen 3", "2,40");
		student_prices.put("Aktionsessen 4", "2,60");
		student_prices.put("Aktionsessen 5", "2,80");
		student_prices.put("Aktionsessen 6", "3,00");
		student_prices.put("Aktionsessen 7", "3,20");
		student_prices.put("Aktionsessen 8", "3,50");
		student_prices.put("Aktionsessen 9", "4,00");
		student_prices.put("Aktionsessen 10", "4,50");

		student_prices.put("Biogericht 1", "1,55");
		student_prices.put("Biogericht 2", "1,90");
		student_prices.put("Biogericht 3", "2,40");
		student_prices.put("Biogericht 4", "2,60");
		student_prices.put("Biogericht 5", "2,80");
		student_prices.put("Biogericht 6", "3,00");
		student_prices.put("Biogericht 7", "3,20");
		student_prices.put("Biogericht 8", "3,50");
		student_prices.put("Biogericht 9", "4,00");
		student_prices.put("Biogericht 10", "4,50");

		employee_prices = new HashMap<String, String>();
		employee_prices.put("Tagesgericht 1", "1,90");
		employee_prices.put("Tagesgericht 2", "2,20");
		employee_prices.put("Tagesgericht 3", "2,40");
		employee_prices.put("Tagesgericht 4", "2,80");

		employee_prices.put("Aktionsessen 1", "2,20");
		employee_prices.put("Aktionsessen 2", "2,40");
		employee_prices.put("Aktionsessen 3", "2,80");
		employee_prices.put("Aktionsessen 4", "3,00");
		employee_prices.put("Aktionsessen 5", "3,20");
		employee_prices.put("Aktionsessen 6", "3,40");
		employee_prices.put("Aktionsessen 7", "3,60");
		employee_prices.put("Aktionsessen 8", "3,90");
		employee_prices.put("Aktionsessen 9", "4,40");
		employee_prices.put("Aktionsessen 10", "4,90");

		employee_prices.put("Biogericht 1", "2,20");
		employee_prices.put("Biogericht 2", "2,40");
		employee_prices.put("Biogericht 3", "2,80");
		employee_prices.put("Biogericht 4", "3,00");
		employee_prices.put("Biogericht 5", "3,20");
		employee_prices.put("Biogericht 6", "3,40");
		employee_prices.put("Biogericht 7", "3,60");
		employee_prices.put("Biogericht 8", "3,90");
		employee_prices.put("Biogericht 9", "4,40");
		employee_prices.put("Biogericht 10", "4,90");

		guest_prices = new HashMap<String, String>();
		guest_prices.put("Tagesgericht 1", "2,40");
		guest_prices.put("Tagesgericht 2", "2,70");
		guest_prices.put("Tagesgericht 3", "2,90");
		guest_prices.put("Tagesgericht 4", "3,30");

		guest_prices.put("Aktionsessen 1", "2,70");
		guest_prices.put("Aktionsessen 2", "2,90");
		guest_prices.put("Aktionsessen 3", "3,30");
		guest_prices.put("Aktionsessen 4", "3,50");
		guest_prices.put("Aktionsessen 5", "3,70");
		guest_prices.put("Aktionsessen 6", "3,90");
		guest_prices.put("Aktionsessen 7", "4,10");
		guest_prices.put("Aktionsessen 8", "4,40");
		guest_prices.put("Aktionsessen 9", "4,90");
		guest_prices.put("Aktionsessen 10", "5,40");

		guest_prices.put("Biogericht 1", "2,70");
		guest_prices.put("Biogericht 2", "2,90");
		guest_prices.put("Biogericht 3", "3,30");
		guest_prices.put("Biogericht 4", "3,50");
		guest_prices.put("Biogericht 5", "3,70");
		guest_prices.put("Biogericht 6", "3,90");
		guest_prices.put("Biogericht 7", "4,10");
		guest_prices.put("Biogericht 8", "4,40");
		guest_prices.put("Biogericht 9", "4,90");
		guest_prices.put("Biogericht 10", "5,40");

	}

}
