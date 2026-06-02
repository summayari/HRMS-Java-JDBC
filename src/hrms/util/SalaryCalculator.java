package hrms.util;

/**
 * Auto salary calculator.
 * Formula: Net = Basic + Bonus + Overtime_Pay - Tax - Deductions
 * Overtime Pay = (Basic / 26 / 8) * 1.5 * overtime_hours
 * Tax = (Basic + Bonus) * taxPercent / 100
 */
public class SalaryCalculator {

    public static final double OVERTIME_RATE = 1.5; // 1.5x hourly rate
    public static final int    WORKING_DAYS  = 26;
    public static final int    HOURS_PER_DAY = 8;

    public static double calcOvertimePay(double basicSalary, double overtimeHours) {
        double hourlyRate = basicSalary / WORKING_DAYS / HOURS_PER_DAY;
        return hourlyRate * OVERTIME_RATE * overtimeHours;
    }

    public static double calcTax(double basicSalary, double bonus, double taxPercent) {
        return (basicSalary + bonus) * taxPercent / 100.0;
    }

    public static double calcNet(double basic, double bonus, double overtimeHours,
                                  double taxPercent, double deductions) {
        double overtimePay = calcOvertimePay(basic, overtimeHours);
        double tax         = calcTax(basic, bonus, taxPercent);
        return basic + bonus + overtimePay - tax - deductions;
    }

    /** Returns a formatted breakdown string for display. */
    public static String breakdown(double basic, double bonus, double overtimeHours,
                                    double taxPercent, double deductions) {
        double otPay = calcOvertimePay(basic, overtimeHours);
        double tax   = calcTax(basic, bonus, taxPercent);
        double net   = calcNet(basic, bonus, overtimeHours, taxPercent, deductions);
        return String.format(
            "Basic: PKR %,.0f\n" +
            "Bonus: PKR %,.0f\n" +
            "Overtime (%,.1f hrs): PKR %,.0f\n" +
            "Tax (%.1f%%): - PKR %,.0f\n" +
            "Other Deductions: - PKR %,.0f\n" +
            "─────────────────────────\n" +
            "NET SALARY: PKR %,.0f",
            basic, bonus, overtimeHours, otPay, taxPercent, tax, deductions, net);
    }
}
