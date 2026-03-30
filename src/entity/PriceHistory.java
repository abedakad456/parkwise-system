package entity;

public class PriceHistory {
	private int year;
    private double firstHourPrice;
    private double additionalHourPrice;
    private double fullDayPrice;

    public PriceHistory(int year, double first, double extra, double day) {
        this.year = year;
        this.firstHourPrice = first;
        this.additionalHourPrice = extra;
        this.fullDayPrice = day;
    }

    // Getters
    public int getYear() { return year; }
    public double getFirstHourPrice() { return firstHourPrice; }
    public double getAdditionalHourPrice() { return additionalHourPrice; }
    public double getFullDayPrice() { return fullDayPrice; }

}
