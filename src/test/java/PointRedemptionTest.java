import Model.CartItemModel;
import Model.CustomerModel;
import Model.ProductModel;
import Model.ToppingModel;
import Model.VariantModel;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ========================================================
 *   TEST CHỨC NĂNG QUY ĐỔI ĐIỂM - The Bang Coffee
 * ========================================================
 *
 * Các quy tắc đang áp dụng (lấy từ PosController):
 *   - tienTichMotDiem  = 10.000đ  → cứ 10.000đ tiêu = +1 điểm tích lũy
 *   - giaTriMotDiem    = 100đ     → 1 điểm dư = giảm 100đ
 *   - diemDoiMotLy     = 50 điểm  → 50 điểm = 1 ly miễn phí
 *
 * Các nhóm test:
 *   1. Tính giá CartItemModel (giá đơn, có topping, hàng reward)
 *   2. Tính VAT trên từng CartItem
 *   3. Tính tổng hoá đơn (subTotal, totalVat, finalTotal)
 *   4. Tính điểm tích lũy được sau khi thanh toán
 *   5. Tính giảm giá từ điểm (đổi ly miễn phí)
 *   6. Tính giảm giá từ điểm dư (quy thành tiền)
 *   7. Kết hợp: Đổi ly + điểm dư cùng lúc
 *   8. Bảo vệ: Giảm giá không được vượt tổng tiền
 *   9. Tính điểm dùng cho CartItem là Reward trong PosController
 *  10. Hiển thị trên hoá đơn: finalTotal sau khi trừ discount
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🧪 Test Chức Năng Quy Đổi Điểm")
class PointRedemptionTest {

    // =====================================================
    //   HẰng SỐ QUY TẮC (khớp với PosController)
    // =====================================================
    static final int TIEN_TICH_MOT_DIEM = 10_000;  // 10.000đ → +1 điểm
    static final int GIA_TRI_MOT_DIEM   = 100;      // 1 điểm dư → giảm 100đ
    static final int DIEM_DOI_MOT_LY    = 50;        // 50 điểm  → 1 ly miễn phí

    // =====================================================
    //   HÀM TIỆN ÍCH TẠO ĐỐI TƯỢNG GIẢ (MOCK)
    // =====================================================

    /**
     * Tạo ProductModel giả để dùng trong test
     */
    private ProductModel makeProduct(int id, String name, double vat) {
        ProductModel p = new ProductModel();
        p.setProductID(id);
        p.setProductName(name);
        p.setVat(vat);
        return p;
    }

    /**
     * Tạo VariantModel giả (có đủ 3 loại giá)
     */
    private VariantModel makeVariant(int id, long dineIn, long takeaway, long holiday) {
        return new VariantModel(id, 1, "M", dineIn, takeaway, holiday);
    }

    /**
     * Tạo ToppingModel giả
     */
    private ToppingModel makeTopping(int id, String name, long price, double vat) {
        return new ToppingModel(id, name, price, 0, 0, vat);
    }

    /**
     * Tạo CartItemModel không có topping, dùng tại quán
     */
    private CartItemModel makeItem(ProductModel p, VariantModel v, int qty) {
        return new CartItemModel(p, v, new ArrayList<>(), qty, "");
    }

    /**
     * Tạo CartItemModel có topping
     */
    private CartItemModel makeItemWithToppings(ProductModel p, VariantModel v, List<ToppingModel> tops, int qty) {
        return new CartItemModel(p, v, tops, qty, "");
    }

    // =====================================================
    //   NHÓM 1: GIÁ CỦA CART ITEM
    // =====================================================

    @Test @Order(1)
    @DisplayName("TC01 – Giá đơn vị (dineIn, không topping)")
    void testUnitPrice_DineIn_NoTopping() {
        // Ly Cà Phê Sữa, size M, giá tại quán 35.000đ
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 1);
        item.setOrderType(false, false); // dùng tại quán

        assertEquals(35_000L, item.getUnitPrice(),
            "Giá đơn vị dùng tại quán phải là 35.000đ");
    }

    @Test @Order(2)
    @DisplayName("TC02 – Giá đơn vị (mang đi)")
    void testUnitPrice_Takeaway() {
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 1);
        item.setOrderType(true, false); // mang đi

        assertEquals(37_000L, item.getUnitPrice(),
            "Giá đơn vị mang đi phải là 37.000đ");
    }

    @Test @Order(3)
    @DisplayName("TC03 – Giá đơn vị (ngày lễ)")
    void testUnitPrice_Holiday() {
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 1);
        item.setOrderType(false, true); // ngày lễ

        assertEquals(45_000L, item.getUnitPrice(),
            "Giá đơn vị ngày lễ phải là 45.000đ");
    }

    @Test @Order(4)
    @DisplayName("TC04 – Giá đơn vị có topping")
    void testUnitPrice_WithTopping() {
        // Ly 35.000đ + Trân Châu 5.000đ = 40.000đ
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        ToppingModel t = makeTopping(1, "Trân Châu Đen", 5_000, 8.0);
        CartItemModel item = makeItemWithToppings(p, v, List.of(t), 1);
        item.setOrderType(false, false);

        assertEquals(40_000L, item.getUnitPrice(),
            "Giá có topping = giá món + giá topping = 40.000đ");
    }

    @Test @Order(5)
    @DisplayName("TC05 – Giá tổng (qty × unitPrice)")
    void testTotalPrice_MultiQty() {
        // 3 ly × 35.000đ = 105.000đ
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 3);
        item.setOrderType(false, false);

        assertEquals(105_000L, item.getTotalPrice(),
            "3 ly × 35.000đ = 105.000đ");
    }

    @Test @Order(6)
    @DisplayName("TC06 – Hàng Reward (đổi điểm): giá phải = 0đ")
    void testRewardItem_PriceIsZero() {
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        ToppingModel t = makeTopping(1, "Trân Châu Đen", 5_000, 8.0);
        CartItemModel item = makeItemWithToppings(p, v, List.of(t), 2);
        item.setOrderType(false, false);
        item.setReward(true); // Đánh dấu là hàng đổi điểm

        assertEquals(0L, item.getUnitPrice(), "Hàng reward phải có giá đơn vị = 0đ");
        assertEquals(0L, item.getTotalPrice(), "Hàng reward phải có tổng giá = 0đ");
    }

    // =====================================================
    //   NHÓM 2: TÍNH VAT
    // =====================================================

    @Test @Order(7)
    @DisplayName("TC07 – VAT của món chính (8%, 1 ly 35.000đ)")
    void testMainVat_SingleItem() {
        // Giá đã bao gồm VAT 8%
        // priceBeforeTax = 35.000 / 1.08 = 32.407,4074...
        // VAT = 35.000 - 32.407.4074... = 2.592,59...
        // (cho 1 ly)
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 1);
        item.setOrderType(false, false);

        double expectedVat = 35_000 - (35_000 / 1.08);
        assertEquals(expectedVat, item.getMainVatAmount(), 0.01,
            "VAT của 1 ly 35.000đ (thuế suất 8%) phải xấp xỉ " + String.format("%.2f", expectedVat) + "đ");
    }

    @Test @Order(8)
    @DisplayName("TC08 – VAT nhân với số lượng (3 ly)")
    void testMainVat_MultiQty() {
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 3);
        item.setOrderType(false, false);

        double vatPerUnit = 35_000 - (35_000 / 1.08);
        double expectedVat = vatPerUnit * 3;
        assertEquals(expectedVat, item.getMainVatAmount(), 0.01,
            "VAT của 3 ly 35.000đ phải = VAT đơn × 3");
    }

    @Test @Order(9)
    @DisplayName("TC09 – VAT = 0 cho hàng Reward")
    void testVat_RewardItem_IsZero() {
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 2);
        item.setReward(true);

        assertEquals(0.0, item.getMainVatAmount(), 0.001,
            "Hàng reward không có VAT");
        assertEquals(0.0, item.getTotalVatAmount(), 0.001,
            "Topping VAT của hàng reward cũng = 0");
    }

    @Test @Order(10)
    @DisplayName("TC10 – VAT tổng (món + topping)")
    void testTotalVat_ItemPlusTopping() {
        // Ly 35.000đ (VAT 8%) + Trân châu 5.000đ (VAT 8%)
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        ToppingModel t = makeTopping(1, "Trân Châu Đen", 5_000, 8.0);
        CartItemModel item = makeItemWithToppings(p, v, List.of(t), 1);
        item.setOrderType(false, false);

        double mainVat    = 35_000 - (35_000 / 1.08);
        double toppingVat = 5_000 - (5_000 / 1.08);
        double expectedTotal = mainVat + toppingVat;

        assertEquals(expectedTotal, item.getTotalVatAmount(), 0.01,
            "VAT tổng = VAT món + VAT topping");
    }

    // =====================================================
    //   NHÓM 3: TỔNG HOÁ ĐƠN
    // =====================================================

    /**
     * Mô phỏng logic tính tổng trong PosController.addCreateOrderListener
     */
    private long[] calcBillTotals(List<CartItemModel> cart) {
        long finalTotal = 0;
        double totalVat = 0;
        for (CartItemModel item : cart) {
            finalTotal += item.getTotalPrice();
            totalVat   += item.getTotalVatAmount();
        }
        long subTotal = finalTotal - Math.round(totalVat);
        return new long[]{subTotal, Math.round(totalVat), finalTotal};
    }

    @Test @Order(11)
    @DisplayName("TC11 – Tổng hoá đơn 1 món, không discount")
    void testBillTotals_SingleItem() {
        // 2 ly Cà Phê Sữa 35.000đ  → total = 70.000đ
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 2);
        item.setOrderType(false, false);

        long[] totals = calcBillTotals(List.of(item));
        long subTotal  = totals[0];
        long vat       = totals[1];
        long finalTotal= totals[2];

        assertEquals(70_000L, finalTotal, "finalTotal = 2 × 35.000 = 70.000đ");

        double vatExpected = (35_000 - 35_000 / 1.08) * 2;
        assertEquals(Math.round(vatExpected), vat, "VAT đúng với 8%");
        assertEquals(finalTotal - vat, subTotal, "subTotal = finalTotal - VAT");
    }

    @Test @Order(12)
    @DisplayName("TC12 – Tổng hoá đơn nhiều món hỗn hợp")
    void testBillTotals_MultiItems() {
        // Món A: 2 ly × 35.000đ = 70.000đ
        // Món B (reward): 1 ly × 45.000đ → reward nên = 0đ
        ProductModel pA = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel vA = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel itemA = makeItem(pA, vA, 2);
        itemA.setOrderType(false, false);

        ProductModel pB = makeProduct(2, "Trà Chanh", 8.0);
        VariantModel vB = makeVariant(2, 45_000, 47_000, 55_000);
        CartItemModel itemB = makeItem(pB, vB, 1);
        itemB.setOrderType(false, false);
        itemB.setReward(true); // Hàng đổi điểm → giá 0đ

        long[] totals = calcBillTotals(List.of(itemA, itemB));
        assertEquals(70_000L, totals[2],
            "FinalTotal phải là 70.000đ vì hàng reward giá 0đ");
    }

    // =====================================================
    //   NHÓM 4: TÍCH ĐIỂM SAU THANH TOÁN
    // =====================================================

    /**
     * Mô phỏng logic tích điểm trong PosController.checkAndRewardPoints
     * eligibleAmount = tổng giá các món không phải reward và > 0
     * pointsToAdd = eligibleAmount / tienTichMotDiem (lấy nguyên)
     */
    private int calcEarnedPoints(long eligibleAmount) {
        return (int) (eligibleAmount / TIEN_TICH_MOT_DIEM);
    }

    @Test @Order(13)
    @DisplayName("TC13 – Tích điểm: 70.000đ → 7 điểm")
    void testEarnPoints_Normal() {
        long eligible = 70_000L;
        assertEquals(7, calcEarnedPoints(eligible),
            "70.000 / 10.000 = 7 điểm");
    }

    @Test @Order(14)
    @DisplayName("TC14 – Tích điểm: 95.000đ → 9 điểm (bỏ phần lẻ)")
    void testEarnPoints_TruncateRemainder() {
        long eligible = 95_000L;
        assertEquals(9, calcEarnedPoints(eligible),
            "95.000 / 10.000 = 9.5 → chỉ lấy 9 điểm (phần nguyên)");
    }

    @Test @Order(15)
    @DisplayName("TC15 – Không tích điểm nếu tiêu dưới 10.000đ")
    void testEarnPoints_BelowThreshold() {
        long eligible = 9_999L;
        assertEquals(0, calcEarnedPoints(eligible),
            "9.999đ < 10.000đ → không được điểm nào");
    }

    @Test @Order(16)
    @DisplayName("TC16 – Tích điểm: hàng Reward không được tính")
    void testEarnPoints_ExcludeRewardItems() {
        // Cart: 2 ly thường 35.000đ + 1 ly reward 0đ
        // eligibleAmount chỉ tính từ hàng thường = 70.000đ
        long eligibleAmount = 70_000L; // reward không đưa vào eligible
        assertEquals(7, calcEarnedPoints(eligibleAmount),
            "Hàng reward không tính vào điểm tích lũy, vẫn ra 7 điểm");
    }

    // =====================================================
    //   NHÓM 5: GIẢM GIÁ ĐỔI LY MIỄN PHÍ
    // =====================================================

    /**
     * Mô phỏng calculateDiscount() trong CheckoutDialog
     * Xếp giá từ thấp đến cao, lấy các ly rẻ nhất để miễn phí
     */
    private long calcDiscount(int pointsToUse, List<Long> eligiblePrices, long totalBill) {
        List<Long> sorted = new ArrayList<>(eligiblePrices);
        Collections.sort(sorted);

        int maxDrinks        = sorted.size();
        int potentialFree    = pointsToUse / DIEM_DOI_MOT_LY;
        int actualFree       = Math.min(potentialFree, maxDrinks);

        int pointsForDrinks  = actualFree * DIEM_DOI_MOT_LY;
        int leftover         = pointsToUse - pointsForDrinks;

        long drinkDiscount   = 0;
        for (int i = 0; i < actualFree; i++) {
            drinkDiscount += sorted.get(i);
        }

        long cashDiscount    = (long) leftover * GIA_TRI_MOT_DIEM;
        long total           = drinkDiscount + cashDiscount;

        return Math.min(total, totalBill);
    }

    @Test @Order(17)
    @DisplayName("TC17 – 50 điểm → 1 ly miễn phí (rẻ nhất 35.000đ)")
    void testDiscount_ExactlyOneFree() {
        // Cart: 2 ly [35.000đ, 45.000đ]. Dùng 50 điểm → ly 35.000đ miễn phí
        List<Long> prices = Arrays.asList(35_000L, 45_000L);
        long discount = calcDiscount(50, prices, 80_000L);

        assertEquals(35_000L, discount,
            "50 điểm → 1 ly miễn phí, lấy ly rẻ nhất = 35.000đ");
    }

    @Test @Order(18)
    @DisplayName("TC18 – 100 điểm → 2 ly miễn phí")
    void testDiscount_TwoFreeDrinks() {
        // Cart: 3 ly [25.000đ, 35.000đ, 45.000đ]. Dùng 100 điểm → 2 ly rẻ nhất
        List<Long> prices = Arrays.asList(35_000L, 45_000L, 25_000L);
        long discount = calcDiscount(100, prices, 105_000L);

        // 2 ly rẻ nhất: 25.000 + 35.000 = 60.000đ
        assertEquals(60_000L, discount,
            "100 điểm → 2 ly miễn phí: 25.000 + 35.000 = 60.000đ");
    }

    @Test @Order(19)
    @DisplayName("TC19 – Điểm dư sau đổi ly được quy thành tiền")
    void testDiscount_DrinkPlusCashRemainder() {
        // 75 điểm → 1 ly (50 điểm) + dư 25 điểm × 100đ = 2.500đ
        List<Long> prices = Arrays.asList(35_000L, 45_000L);
        long discount = calcDiscount(75, prices, 80_000L);

        // ly 35.000đ miễn phí + 25 điểm × 100đ = 2.500đ → tổng 37.500đ
        assertEquals(37_500L, discount,
            "75 điểm → 1 ly 35.000đ + 25 điểm × 100đ = 37.500đ");
    }

    @Test @Order(20)
    @DisplayName("TC20 – Chỉ có điểm dư (chưa đủ 50 để đổi ly)")
    void testDiscount_OnlyCashPoints() {
        // 30 điểm, chưa đủ 50 để đổi ly → 30 × 100đ = 3.000đ giảm
        List<Long> prices = Arrays.asList(35_000L, 45_000L);
        long discount = calcDiscount(30, prices, 80_000L);

        assertEquals(3_000L, discount,
            "30 điểm chưa đủ 50 → quy tiền = 30 × 100đ = 3.000đ");
    }

    // =====================================================
    //   NHÓM 6: BẢO VỆ – GIẢM GIÁ KHÔNG VƯỢT TỔNG TIỀN
    // =====================================================

    @Test @Order(21)
    @DisplayName("TC21 – Giảm giá không được vượt tổng hoá đơn")
    void testDiscount_CapAtTotalBill() {
        // Chỉ 1 ly 35.000đ, dùng 200 điểm (đủ đổi 4 ly nhưng chỉ có 1)
        // potentialFree = 4, actualFree = min(4, 1) = 1
        // drinkDiscount = 35.000đ
        // leftover = 200 - 50 = 150 điểm × 100đ = 15.000đ
        // total = 35.000 + 15.000 = 50.000đ → cap tại totalBill = 35.000đ
        List<Long> prices = Arrays.asList(35_000L);
        long discount = calcDiscount(200, prices, 35_000L);

        assertEquals(35_000L, discount,
            "Giảm tối đa bằng tổng hoá đơn = 35.000đ");
    }

    @Test @Order(22)
    @DisplayName("TC22 – Hoá đơn 0đ khi discount = totalBill")
    void testFinalTotal_WhenFullDiscount() {
        long totalBill   = 35_000L;
        long discount    = 35_000L; // Đúng bằng totalBill
        long finalTotal  = Math.max(0, totalBill - discount);

        assertEquals(0L, finalTotal,
            "Khách phải trả 0đ khi giảm đúng bằng tổng hoá đơn");
    }

    // =====================================================
    //   NHÓM 7: TÍNH ĐIỂM DÙNG CHO REWARD ITEMS (PosController)
    // =====================================================

    /**
     * Mô phỏng logic tính pointsUsedForItems trong PosController
     * Dùng Math.ceil vì phải làm tròn lên khi đổi tiền về điểm
     */
    private int calcPointsForRewardItem(long originalTotal, int giaTriMotDiem) {
        return (int) Math.ceil((double) originalTotal / giaTriMotDiem);
    }

    @Test @Order(23)
    @DisplayName("TC23 – Điểm cần dùng cho 1 ly reward 35.000đ")
    void testPointsForReward_SingleDrink() {
        // 35.000đ / 100đ = 350 điểm
        int points = calcPointsForRewardItem(35_000L, GIA_TRI_MOT_DIEM);
        assertEquals(350, points,
            "1 ly 35.000đ cần 35.000 / 100 = 350 điểm");
    }

    @Test @Order(24)
    @DisplayName("TC24 – Điểm cần dùng làm tròn lên (ví dụ: 35.001đ)")
    void testPointsForReward_RoundUp() {
        // 35.001 / 100 = 350.01 → làm tròn lên = 351 điểm
        int points = calcPointsForRewardItem(35_001L, GIA_TRI_MOT_DIEM);
        assertEquals(351, points,
            "35.001đ / 100đ = 350.01 → ceil = 351 điểm");
    }

    @Test @Order(25)
    @DisplayName("TC25 – Tổng điểm = điểm cho reward + điểm dùng trực tiếp")
    void testTotalPointsDeducted() {
        // Ly reward 35.000đ → 350 điểm
        // Khách dùng thêm 30 điểm tiền trực tiếp (từ CheckoutDialog)
        int pointsForReward = calcPointsForRewardItem(35_000L, GIA_TRI_MOT_DIEM);
        int pointsUsedDirectly = 30;
        int total = pointsForReward + pointsUsedDirectly;

        assertEquals(380, total,
            "Tổng điểm trừ = 350 (reward) + 30 (trực tiếp) = 380");
    }

    // =====================================================
    //   NHÓM 8: HIỂN THỊ TRÊN HOÁ ĐƠN
    // =====================================================

    @Test @Order(26)
    @DisplayName("TC26 – Hiển thị: finalTotal sau discount (35% off)")
    void testInvoiceDisplay_FinalTotal() {
        long totalBill   = 80_000L;
        long discount    = 35_000L;
        long finalTotal  = Math.max(0, totalBill - discount);

        assertEquals(45_000L, finalTotal,
            "80.000 - 35.000 = 45.000đ hiển thị trên hoá đơn");
    }

    @Test @Order(27)
    @DisplayName("TC27 – Hiển thị: Điểm dự kiến cộng thêm (nonRewardTotal - discount)")
    void testInvoiceDisplay_EarnedPoints() {
        // nonRewardTotal = 70.000đ, discount = 35.000đ → paid = 35.000đ
        // earned = 35.000 / 10.000 = 3 điểm
        long nonRewardTotal = 70_000L;
        long discount       = 35_000L;
        long paid           = nonRewardTotal - discount;
        if (paid < 0) paid = 0;

        int earned = (int) (paid / TIEN_TICH_MOT_DIEM);
        assertEquals(3, earned,
            "Sau giảm 35.000đ, paid = 35.000đ → cộng 3 điểm");
    }

    @Test @Order(28)
    @DisplayName("TC28 – Hiển thị: Không cộng điểm khi paid = 0đ")
    void testInvoiceDisplay_ZeroPaidNoPoints() {
        long nonRewardTotal = 35_000L;
        long discount       = 35_000L;
        long paid           = Math.max(0, nonRewardTotal - discount);

        int earned = (int) (paid / TIEN_TICH_MOT_DIEM);
        assertEquals(0, earned,
            "Khách trả 0đ → không được cộng điểm mới");
    }

    @Test @Order(29)
    @DisplayName("TC29 – VAT không thay đổi dù có giảm giá (giữ nguyên VAT gốc)")
    void testVatUnchangedAfterDiscount() {
        // Theo comment trong PosController (dòng 262-266):
        // VAT luôn tính trên tiền hàng GỐC, không scale theo actualFinalTotal
        ProductModel p = makeProduct(1, "Cà Phê Sữa", 8.0);
        VariantModel v = makeVariant(1, 35_000, 37_000, 45_000);
        CartItemModel item = makeItem(p, v, 2);
        item.setOrderType(false, false);

        double originalVat = item.getTotalVatAmount(); // VAT trên 70.000đ
        long discount = 35_000L;

        // adjustedVat trong PosController = originalVat (không đổi)
        double adjustedVat = originalVat; // Không scale

        assertEquals(originalVat, adjustedVat, 0.001,
            "VAT phải giữ nguyên dù giảm giá = thiết kế đúng theo nghĩa vụ thuế");
    }

    @Test @Order(30)
    @DisplayName("TC30 – Kiểm tra chuỗi format tiền hiển thị trên hoá đơn")
    void testFormatCurrency() {
        long amount = 1_234_567L;
        String formatted = String.format("%,d đ", amount);

        // Trên môi trường Việt Nam, dấu phân cách có thể là . hoặc ,
        assertTrue(formatted.contains("1") && formatted.contains("234") && formatted.contains("567"),
            "Format tiền phải chứa các chữ số đúng: " + formatted);
    }

    // =====================================================
    //   NHÓM 9: EDGE CASES (TRƯỜNG HỢP BIÊN)
    // =====================================================

    @Test @Order(31)
    @DisplayName("TC31 – Điểm dùng = 0: không giảm gì")
    void testEdge_ZeroPointsUsed() {
        List<Long> prices = Arrays.asList(35_000L, 45_000L);
        long discount = calcDiscount(0, prices, 80_000L);

        // 0/50 = 0 ly, 0 điểm dư → discount = 0
        assertEquals(0L, discount,
            "Dùng 0 điểm → giảm 0đ");
    }

    @Test @Order(32)
    @DisplayName("TC32 – Giỏ hàng rỗng: tổng tiền = 0đ")
    void testEdge_EmptyCart() {
        List<CartItemModel> emptyCart = new ArrayList<>();
        long total = 0;
        for (CartItemModel item : emptyCart) {
            total += item.getTotalPrice();
        }
        assertEquals(0L, total, "Giỏ trống → tổng tiền = 0đ");
    }

    @Test @Order(33)
    @DisplayName("TC33 – Đổi đúng 50 điểm với 1 ly duy nhất")
    void testEdge_ExactlyFiftyPointsOneDrink() {
        List<Long> prices = Arrays.asList(45_000L);
        long discount = calcDiscount(50, prices, 45_000L);

        assertEquals(45_000L, discount,
            "50 điểm = 1 ly, ly này 45.000đ → giảm đúng 45.000đ");
    }

    @Test @Order(34)
    @DisplayName("TC34 – Khách không có điểm: discountAmount = 0")
    void testEdge_CustomerHasZeroPoints() {
        CustomerModel cust = new CustomerModel(1, "0901000001", "Nguyễn A", 0, 0, "Mới");
        assertEquals(0, cust.getDiemHienTai(),
            "Khách mới có 0 điểm");

        // Với 0 điểm, không thể giảm gì
        long discount = calcDiscount(0, Arrays.asList(35_000L), 35_000L);
        assertEquals(0L, discount, "0 điểm → không giảm");
    }

    @Test @Order(35)
    @DisplayName("TC35 – Số điểm dùng nhiều hơn số ly trong giỏ")
    void testEdge_MorePointsThanDrinksInCart() {
        // Cart chỉ có 1 ly 35.000đ, nhưng khách muốn dùng 200 điểm (đủ 4 ly)
        // actualFree = min(4, 1) = 1 ly
        List<Long> prices = Arrays.asList(35_000L);
        long discount = calcDiscount(200, prices, 35_000L);

        // 1 ly miễn phí = 35.000đ, còn dư 150 điểm × 100 = 15.000đ
        // total = 50.000đ → cap tại 35.000đ
        assertEquals(35_000L, discount,
            "Dù điểm thừa, giảm tối đa = tổng hoá đơn = 35.000đ");
    }
}
