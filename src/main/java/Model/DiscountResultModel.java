package Model;

public class DiscountResultModel {
    private int pointsUsed;
    private long discountAmount;
    private long tierDiscountAmount;
    private int earnedPoints;
    private String errorMessage;
    private String successMessage;

    public DiscountResultModel() {
        this.pointsUsed = 0;
        this.discountAmount = 0;
        this.tierDiscountAmount = 0;
        this.earnedPoints = 0;
        this.errorMessage = "";
        this.successMessage = "";
    }

    public int getPointsUsed() { return pointsUsed; }
    public void setPointsUsed(int pointsUsed) { this.pointsUsed = pointsUsed; }

    public long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(long discountAmount) { this.discountAmount = discountAmount; }

    public long getTierDiscountAmount() { return tierDiscountAmount; }
    public void setTierDiscountAmount(long tierDiscountAmount) { this.tierDiscountAmount = tierDiscountAmount; }

    public int getEarnedPoints() { return earnedPoints; }
    public void setEarnedPoints(int earnedPoints) { this.earnedPoints = earnedPoints; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSuccessMessage() { return successMessage; }
    public void setSuccessMessage(String successMessage) { this.successMessage = successMessage; }
}
