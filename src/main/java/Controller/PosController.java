package Controller;

import Model.CartItemModel;
import Model.CategoryModel;
import Model.CustomerModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.ProductModel;
import Model.ToppingModel;
import Model.VariantModel;
import Service.CategoryService;
import Service.CustomerService;
import Service.InvoiceService;
import Service.OrderService;
import Service.ProductService;
import Service.ToppingService;
import Service.VariantService;
import Service.RoleService;
import Model.SessionManager;
import View.MainFrame;
import View.OrderOptionDialog;
import View.OrderPanel;
import View.PosPanel;
import View.RedeemPointsDialog;

import javax.swing.*;
import java.awt.Color;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PosController {

    private MainFrame mainFrame;
    private PosPanel posPanel;
    private OrderPanel orderPanel;
    
    private ProductService productService;
    private CategoryService categoryService;
    private OrderService orderService;
    private VariantService variantService;
    private ToppingService toppingService;
    private CustomerService customerService;
    private InvoiceService invoiceService;
    
    private List<ProductModel> allProducts;
    private List<CartItemModel> currentCart;
    private String currentCategoryFilter = "Tất cả";
    
    private Integer currentCustomerId = null; 
    
    private int currentSelectedOrderId = -1;
    
    private int tienTichMotDiem = 10000;
    private int giaTriMotDiem = 100;
    
    private int globalPointsUsed = 0;
    private long globalDiscountAmount = 0;
    private RoleService roleService;

    public PosController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.posPanel = mainFrame.getPosPanel();
        this.orderPanel = mainFrame.getOrderPanel(); 
        
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.orderService = new OrderService();
        this.currentCart = new ArrayList<>();
        this.variantService = new VariantService();
        this.toppingService = new ToppingService();
        this.customerService = new CustomerService();
        this.invoiceService = new InvoiceService();
        this.roleService = new RoleService();
        
        try {
            hiddenButton();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (mainFrame != null) {
            mainFrame.registerPermissionReloader(() -> {
                try { hiddenButton(); } catch (Exception ex) { ex.printStackTrace(); }
            });
        }
        
        this.mainFrame.setPosController(this);
        
        initView();
        initPosListeners();
        initOrderPanelListeners(); 
    }

    public void reloadPosData() {
        try {
            int[] rules = customerService.getPointRule();
            tienTichMotDiem = rules[0] > 0 ? rules[0] : 10000;
            giaTriMotDiem = rules[1] > 0 ? rules[1] : 100;

            loadCategories();
            allProducts = productService.getProductList().getProductList(); 
            filterProducts(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        reloadPosData();
        updateCartView();
        loadOrderList(); 
    }

    private void initPosListeners() {
        posPanel.addSearchListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterProducts();
            }
        });
        
        posPanel.addCheckCustomerListener(e -> handleCheckCustomer());
        
        posPanel.addClearCustomerListener(e -> {
            posPanel.clearCustomerInfo();
            currentCustomerId = null; 
            globalPointsUsed = 0;
            globalDiscountAmount = 0;
            updateCartView(); 
        });
        
        posPanel.addRegisterCustomerListener(e -> {
            String phone = posPanel.getCustomerPhone();
            String name = posPanel.getCustomerName();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(posPanel, "Vui lòng nhập tên để đăng ký thành viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Model.CustomerModel newCustomer = customerService.registerNewCustomer(phone, name);
                
                if (newCustomer != null) {
                    JOptionPane.showMessageDialog(posPanel, "Đăng ký thành viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    // [ĐÃ SỬA] Hiển thị rõ HT và TL
                    String info = String.format("%s | HT: %d - TL: %d (%s)", 
                        newCustomer.getTenKH(), newCustomer.getDiemHienTai(), newCustomer.getDiemTichLuy(), newCustomer.getHangThanhVien());
                    posPanel.setCustomerStatus(info, false); 
                    
                    currentCustomerId = newCustomer.getMaKH();
                    updateCartView(); 
                } else {
                    JOptionPane.showMessageDialog(posPanel, "Đăng ký thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(posPanel, "Lỗi cơ sở dữ liệu khi đăng ký khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        posPanel.addClearCartListener(e -> {
            if (currentCart.isEmpty()) return;
            if (JOptionPane.showConfirmDialog(posPanel, "Bạn muốn hủy đơn hàng hiện tại?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                currentCart.clear();
                globalPointsUsed = 0;
                globalDiscountAmount = 0;
                updateCartView();
            }
        });

        posPanel.setCartDeleteListener(row -> {
            if (row >= 0 && row < currentCart.size()) {
                currentCart.remove(row);
                updateCartView();
            }
        });
        
        posPanel.addCreateOrderListener(e -> {
            if (currentCart.isEmpty()) {
                JOptionPane.showMessageDialog(posPanel, "Giỏ hàng trống!");
                return;
            }

            String inventoryCheckMsg = orderService.validateInventory(currentCart);
            if (inventoryCheckMsg != null) {
                JOptionPane.showMessageDialog(posPanel, 
                    "Không thể tạo đơn hàng do kho thiếu nguyên liệu:\n\n" + inventoryCheckMsg, 
                    "Cảnh báo Hết Hàng", JOptionPane.WARNING_MESSAGE);
                return; 
            }
            
            long finalTotal = 0; 
            double totalVat = 0; 
            int pointsUsedForItems = 0;  

            for (CartItemModel item : currentCart) {
                finalTotal += item.getTotalPrice();
                totalVat += item.getTotalVatAmount();
                if (item.isReward()) {
                    long unitPrice = item.getSelectedVariant().getDineInPrice();
                    if (posPanel.isHoliday()) unitPrice = item.getSelectedVariant().getHolidayPrice();
                    else if (posPanel.isTakeaway()) unitPrice = item.getSelectedVariant().getTakeawayPrice();
                    
                    long toppingPrice = 0;
                    for (ToppingModel t : item.getSelectedToppings()) {
                        toppingPrice += t.getPrice();
                    }
                    long originalTotal = (unitPrice + toppingPrice) * item.getQuantity();
                    pointsUsedForItems += (int) Math.ceil((double) originalTotal / giaTriMotDiem);
                } 
            }
            
            long actualFinalTotal = finalTotal - globalDiscountAmount;
            if (actualFinalTotal < 0) actualFinalTotal = 0;
            
            int totalPointsToDeduct = pointsUsedForItems + globalPointsUsed;

            int confirm = JOptionPane.showConfirmDialog(posPanel, 
                    "Bạn có chắc muốn tạo đơn hàng này?\nTổng tiền: " + String.format("%,d đ", actualFinalTotal), 
                    "Xác nhận tạo đơn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int currentAccountId = 1; 
                boolean isTakeaway = posPanel.isTakeaway();
                boolean isHoliday = posPanel.isHoliday();

                if (currentCustomerId == null && !posPanel.getCustomerName().isEmpty() && !posPanel.getCustomerPhone().isEmpty()) {
                    try {
                        CustomerModel newCus = customerService.registerNewCustomer(posPanel.getCustomerPhone(), posPanel.getCustomerName());
                        if (newCus != null) currentCustomerId = newCus.getMaKH();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(posPanel, "Lỗi khi lưu khách hàng mới!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                boolean isSuccess = orderService.createOrder(
                    currentAccountId, 
                    currentCustomerId,       
                    currentCart, 
                    actualFinalTotal, 
                    Math.round(totalVat), 
                    "Chờ tiếp nhận",   
                    "Chưa thanh toán",
                    isTakeaway, 
                    isHoliday,
                    0, 
                    totalPointsToDeduct
                );

                if (isSuccess) {
                    JOptionPane.showMessageDialog(posPanel, "Tạo đơn hàng thành công (Chờ tiếp nhận)!");
                    currentCart.clear(); 
                    globalPointsUsed = 0;
                    globalDiscountAmount = 0;
                    updateCartView();    
                    posPanel.clearCustomerInfo(); 
                    currentCustomerId = null;
                    loadOrderList(); 
                } else {
                    JOptionPane.showMessageDialog(posPanel, "Lỗi khi tạo đơn hàng. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        posPanel.addOrderOptionListener(e -> {
            boolean isTakeaway = posPanel.isTakeaway();
            boolean isHoliday = posPanel.isHoliday();
            for (CartItemModel item : currentCart) item.setOrderType(isTakeaway, isHoliday);
            updateCartView();
        });
        
        posPanel.addUsePointsListener(e -> {
            if (currentCart.isEmpty()) {
                JOptionPane.showMessageDialog(posPanel, "Giỏ hàng trống!");
                return;
            }
            if (currentCustomerId == null) {
                JOptionPane.showMessageDialog(posPanel, "Vui lòng nhập thông tin Khách hàng thành viên trước!");
                return;
            }

            CustomerModel c = customerService.findCustomerByPhone(posPanel.getCustomerPhone());
            // [CẬP NHẬT] Kiểm tra Điểm Hiện Tại chứ không phải Tích Lũy
            if (c == null || c.getDiemHienTai() <= 0) {
                JOptionPane.showMessageDialog(posPanel, "Khách hàng không có đủ điểm hiện tại để sử dụng!");
                return;
            }

            long currentBillTotal = 0;
            for (CartItemModel item : currentCart) currentBillTotal += item.getTotalPrice();

            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
            
            // [CẬP NHẬT] Truyền Điểm Hiện Tại vào Popup
            RedeemPointsDialog dialog = new RedeemPointsDialog(parentFrame, c.getDiemHienTai(), giaTriMotDiem, currentBillTotal);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                globalPointsUsed = dialog.getAppliedPoints();
                globalDiscountAmount = dialog.getDiscountAmount();
                updateCartView(); 
            }
        });
    }
    
    private void handleCheckCustomer() {
        String phone = posPanel.getCustomerPhone();
        
        if (phone.isEmpty()) {
            posPanel.clearCustomerInfo();
            currentCustomerId = null;
            globalPointsUsed = 0;
            globalDiscountAmount = 0;
            updateCartView();
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(posPanel, "Số điện thoại không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerModel customer = customerService.findCustomerByPhone(phone);
        
        if (customer != null) {
            // [CẬP NHẬT QUAN TRỌNG] Hiển thị rõ rệt Điểm Hiện Tại và Điểm Tích Lũy
            String info = String.format("%s | HT: %d - TL: %d (%s)", 
                    customer.getTenKH(), customer.getDiemHienTai(), customer.getDiemTichLuy(), customer.getHangThanhVien());
            posPanel.setCustomerStatus(info, false); 
            currentCustomerId = customer.getMaKH(); 
            updateCartView(); 
        } else {
            posPanel.setCustomerStatus("Khách mới! Nhập tên để tạo TK:", true); 
            currentCustomerId = null; 
            globalPointsUsed = 0;
            globalDiscountAmount = 0;
            updateCartView(); 
        }
    }

    private void loadCategories() throws SQLException, ClassNotFoundException {
        posPanel.clearCategories();
        posPanel.addCategoryButton("Tất cả", currentCategoryFilter.equals("Tất cả"), e -> {
            currentCategoryFilter = "Tất cả";
            try { 
                loadCategories();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            filterProducts();
        });

        List<CategoryModel> categories = categoryService.getAllCategories();
        if (categories != null) {
            for (CategoryModel cat : categories) {
                if (cat.getCategoryStatus().equals("Đang sử dụng")) {
                    posPanel.addCategoryButton(cat.getCategoryName(), currentCategoryFilter.equals(cat.getCategoryName()), e -> {
                        currentCategoryFilter = cat.getCategoryName();
                        try {
                            loadCategories();
                        } catch (Exception ex) {
                           ex.printStackTrace();
                        }
                        filterProducts();
                    });
                }
            }
        }
    }

    private void filterProducts() {
        String keyword = posPanel.getSearchText().toLowerCase();
        if (keyword.equals("tìm kiếm tên món...")) keyword = "";

        List<ProductModel> filteredList = new ArrayList<>();
        
        try {
            List<CategoryModel> activeCategories = categoryService.getAllCategories();
            
            for (ProductModel p : allProducts) {
                boolean isCategoryActive = false;
                for (CategoryModel cat : activeCategories) {
                    if (cat.getCategoryName().equals(p.getCategoryName()) && cat.getCategoryStatus().equals("Đang sử dụng")) {
                        isCategoryActive = true;
                        break;
                    }
                }
                
                if (!isCategoryActive) continue;
                
                if (p.getProductStatus() != null && p.getProductStatus().equalsIgnoreCase("Ngừng bán")) {
                    continue; 
                }

                boolean matchCategory = currentCategoryFilter.equals("Tất cả") || p.getCategoryName().equals(currentCategoryFilter);
                boolean matchName = p.getProductName().toLowerCase().contains(keyword);
                if (matchCategory && matchName) filteredList.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        displayProducts(filteredList);
    }

    private void displayProducts(List<ProductModel> products) {
        posPanel.clearProducts();
        for (ProductModel p : products) {
            posPanel.addProductCard(p, e -> {
                List<VariantModel> variants = variantService.getVariantsByProductId(p.getProductID());
                List<ToppingModel> toppings = toppingService.getToppingsByProductID(p.getProductID());
                
                boolean isTakeaway = posPanel.isTakeaway();
                boolean isHoliday = posPanel.isHoliday();

                int currentPoints = 0;
                if (currentCustomerId != null) {
                    CustomerModel c = customerService.findCustomerByPhone(posPanel.getCustomerPhone());
                    // [CẬP NHẬT] Chỉ truyền Điểm Hiện Tại vào Popup đổi quà
                    if (c != null) currentPoints = c.getDiemHienTai();
                }

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
                
                OrderOptionDialog dialog = new OrderOptionDialog(parentFrame, p, variants, toppings, isTakeaway, isHoliday, currentPoints, giaTriMotDiem);

                dialog.setInventoryValidator((newQty, variant, selToppings) -> {
                    List<CartItemModel> testCart = new ArrayList<>();
                    for(CartItemModel item : currentCart) {
                        CartItemModel cloneItem = new CartItemModel(item.getProduct(), item.getSelectedVariant(), item.getSelectedToppings(), item.getQuantity(), item.getNote());
                        cloneItem.setReward(item.isReward()); 
                        testCart.add(cloneItem);
                    }
                    
                    boolean merged = false;
                    for(CartItemModel testItem : testCart) {
                        if (testItem.isSameItem(p, variant, selToppings, "", false)) { 
                            testItem.setQuantity(testItem.getQuantity() + newQty);
                            merged = true; break;
                        }
                    }
                    if (!merged) {
                        testCart.add(new CartItemModel(p, variant, selToppings, newQty, ""));
                    }
                    
                    return orderService.validateInventory(testCart);
                });

                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Model.VariantModel selectedSize = dialog.getSelectedVariant();
                    List<Model.ToppingModel> selectedToppings = dialog.getSelectedToppings();
                    int qty = dialog.getQuantity();
                    String note = dialog.getFinalNote();
                    
                    boolean isReward = dialog.isReward(); 
                    
                    boolean isExist = false;
                    for (CartItemModel existingItem : currentCart) {
                        if (existingItem.isSameItem(p, selectedSize, selectedToppings, note, isReward)) {
                            existingItem.setQuantity(existingItem.getQuantity() + qty);
                            isExist = true;
                            break; 
                        }
                    }

                    if (!isExist) {
                        CartItemModel item = new CartItemModel(p, selectedSize, selectedToppings, qty, note);
                        item.setOrderType(isTakeaway, isHoliday);
                        item.setReward(isReward); 
                        currentCart.add(item);
                    }
                    updateCartView();
                }
            });
        }
    }
    
    private void updateCartView() {
        posPanel.updateCartTable(currentCart);
        
        if (currentCart.isEmpty()) {
            globalPointsUsed = 0;
            globalDiscountAmount = 0;
        }

        long cartTotal = 0; 
        double totalVat = 0; 
        long nonRewardTotal = 0; 
        
        for (CartItemModel item : currentCart) {
            cartTotal += item.getTotalPrice();
            totalVat += item.getTotalVatAmount(); 
            if (!item.isReward()) {
                nonRewardTotal += item.getTotalPrice();
            }
        }
        
        long subTotal = cartTotal - Math.round(totalVat);
        long finalTotal = cartTotal - globalDiscountAmount;
        if (finalTotal < 0) finalTotal = 0;
        
        int earnedPoints = 0;
        if (currentCustomerId != null && tienTichMotDiem > 0) {
            long amountPaid = nonRewardTotal - globalDiscountAmount;
            if (amountPaid < 0) amountPaid = 0;
            earnedPoints = (int) (amountPaid / tienTichMotDiem);
        }
        
        posPanel.updateSummary(subTotal, totalVat, globalDiscountAmount, finalTotal, earnedPoints);
    }

    private void initOrderPanelListeners() {
        orderPanel.addSearchListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                loadOrderList();
            }
        });
        
        orderPanel.addFilterListener(e -> loadOrderList());
        orderPanel.addRefreshListener(e -> loadOrderList());

        orderPanel.addTableSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = orderPanel.getSelectedOrderRow();
                if (row >= 0) {
                    currentSelectedOrderId = Integer.parseInt(orderPanel.getOrderTableModel().getValueAt(row, 0).toString().replace("#", ""));
                    loadOrderDetails(currentSelectedOrderId);
                } else {
                    currentSelectedOrderId = -1;
                    orderPanel.clearOrderInfo();
                }
            }
        });

        orderPanel.addAcceptListener(e -> {
            changeOrderPreparationStatus("Đang pha chế", "Xác nhận chuyển vào Bếp: Bắt đầu pha chế đơn hàng này?");
        });
        
        orderPanel.addPayListener(e -> {
            changeOrderPaymentStatus("Đã thanh toán");
        });
        
        orderPanel.addCancelListener(e -> {
            cancelEntireOrder();
        });
        
        orderPanel.addCompleteListener(e -> {
            if (currentSelectedOrderId <= 0) return;

            int confirm = JOptionPane.showConfirmDialog(orderPanel, 
                "Chuyển trạng thái sang HOÀN THÀNH? Hệ thống sẽ tiến hành trừ nguyên liệu trong kho.", 
                "Xác nhận Hoàn thành", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean isSuccess = orderService.completeOrderAndDeductInventory(currentSelectedOrderId);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(orderPanel, "Hoàn thành món và trừ kho thành công!");
                    checkAndRewardPoints(currentSelectedOrderId, "Đã hoàn thành", null);
                    loadOrderList();
                    loadOrderDetails(currentSelectedOrderId);
                } else {
                    JOptionPane.showMessageDialog(orderPanel, "Có lỗi xảy ra khi hoàn thành món (Hoặc nguyên liệu không đủ)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        orderPanel.addPrintInvoiceListener(e -> {
            if (currentSelectedOrderId > 0) {
                invoiceService.printInvoice(currentSelectedOrderId, false); 
            }
        });
    }

    private void loadOrderList() {
        String keyword = orderPanel.getSearchText();
        if (keyword.equals("Tìm theo mã đơn...")) keyword = "";
        String statusFilter = orderPanel.getSelectedFilter();

        List<OrderModel> orders = orderService.getAllOrders(statusFilter, keyword);
        
        javax.swing.table.DefaultTableModel model = orderPanel.getOrderTableModel();
        model.setRowCount(0);
        
        if (orders != null) {
            for (OrderModel o : orders) {
                model.addRow(new Object[]{
                    "#" + o.getOrderId(),
                    o.getOrderTime(),     
                    o.getOrderTypeNote(), 
                    o.getPreparationStatus(),
                    o.getPaymentStatus(),
                    String.format("%,d đ", o.getFinalTotal())
                });
            }
        }
        
        orderPanel.clearOrderInfo();
        currentSelectedOrderId = -1;
    }

    private void loadOrderDetails(int orderId) {
        OrderModel order = orderService.getOrderById(orderId);
        List<OrderDetailModel> details = orderService.getOrderDetailsByOrderId(orderId);
        
        if (order != null) {
            orderPanel.setOrderInfo(
                String.valueOf(order.getOrderId()), 
                order.getOrderTime(), 
                order.getOrderTypeNote(), 
                order.getPreparationStatus(), 
                order.getPaymentStatus(),
                String.format("%,d đ", order.getFinalTotal())
            );
            
            orderPanel.updateActionButtons(order.getPreparationStatus(), order.getPaymentStatus());
            
            javax.swing.table.DefaultTableModel detailModel = orderPanel.getDetailTableModel();
            detailModel.setRowCount(0);
            
            for (OrderDetailModel d : details) {
                detailModel.addRow(new Object[]{
                    d.getDisplayName(), 
                    d.getQuantity(),
                    String.format("%,d đ", d.getTotalRowPrice())
                });
            }
        }
    }

    private void changeOrderPreparationStatus(String newStatus, String msg) {
        if (currentSelectedOrderId <= 0) return;
        
        int confirm = JOptionPane.showConfirmDialog(orderPanel, msg, "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isSuccess = orderService.updatePreparationStatus(currentSelectedOrderId, newStatus);
            if (isSuccess) {
                if (newStatus.equals("Đã hoàn thành")) {
                    checkAndRewardPoints(currentSelectedOrderId, "Đã hoàn thành", null);
                }
                loadOrderList(); 
                loadOrderDetails(currentSelectedOrderId); 
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Không thể cập nhật trạng thái pha chế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void changeOrderPaymentStatus(String newStatus) {
        if (currentSelectedOrderId <= 0) return;
        
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(orderPanel);
        View.PaymentMethodDialog dialog = new View.PaymentMethodDialog(parentFrame, currentSelectedOrderId);
        dialog.setVisible(true); 
            
        int choice = dialog.getSelectedOption();

        if (choice == 0 || choice == 1) {
            String phuongThucThanhToan = (choice == 0) ? "Tiền mặt" : "Chuyển khoản";
            
            boolean isSuccess = orderService.updatePaymentStatus(currentSelectedOrderId, newStatus, phuongThucThanhToan);
            
            if (isSuccess) {
                if (newStatus.equals("Đã thanh toán")) {
                    checkAndRewardPoints(currentSelectedOrderId, null, "Đã thanh toán");
                }
                loadOrderList(); 
                loadOrderDetails(currentSelectedOrderId); 
                JOptionPane.showMessageDialog(orderPanel, "Xác nhận thanh toán thành công bằng " + phuongThucThanhToan + "!");
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Không thể cập nhật trạng thái thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cancelEntireOrder() {
        if (currentSelectedOrderId <= 0) return;
        
        int confirm = JOptionPane.showConfirmDialog(orderPanel, 
            "CẢNH BÁO: Bạn có chắc chắn muốn hủy đơn hàng này không?\nThao tác này không thể hoàn tác.", 
            "Xác nhận Hủy Đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            OrderModel order = orderService.getOrderById(currentSelectedOrderId);
            
            boolean prepSuccess = orderService.updatePreparationStatus(currentSelectedOrderId, "Đã hủy");
            
            boolean paySuccess = true;
            if (order.getPaymentStatus().equals("Đã thanh toán")) {
                paySuccess = orderService.updatePaymentStatus(currentSelectedOrderId, "Đã hoàn tiền", "Chưa thanh toán");
                JOptionPane.showMessageDialog(orderPanel, "Đơn hàng đã thanh toán trước đó. Vui lòng hoàn lại tiền cho khách: " + String.format("%,d đ", order.getFinalTotal()));
            } else {
                paySuccess = orderService.updatePaymentStatus(currentSelectedOrderId, "Đã hủy", "Chưa thanh toán");
            }

            if (prepSuccess && paySuccess) {
                loadOrderList(); 
                loadOrderDetails(currentSelectedOrderId); 
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Có lỗi xảy ra khi hủy đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkAndRewardPoints(int orderId, String newPrepStatus, String newPayStatus) {
        OrderModel order = orderService.getOrderById(orderId);
        if (order == null) return;

        String prep = newPrepStatus != null ? newPrepStatus : order.getPreparationStatus();
        String pay = newPayStatus != null ? newPayStatus : order.getPaymentStatus();

        if ("Đã hoàn thành".equals(prep) && "Đã thanh toán".equals(pay)) {
            try {
                List<OrderDetailModel> details = orderService.getOrderDetailsByOrderId(orderId);
                long eligibleAmount = 0;
                
                if (details != null) {
                    for (OrderDetailModel d : details) {
                        if (d.getTotalRowPrice() > 0 && !d.getDisplayName().contains("Hàng quy đổi điểm")) {
                            eligibleAmount += d.getTotalRowPrice();
                        }
                    }
                }
                
                int[] rules = customerService.getPointRule();
                int tienTich = rules[0] > 0 ? rules[0] : 10000;
                int pointsToAdd = (int) (eligibleAmount / tienTich);
                
                if (pointsToAdd > 0) {
                    customerService.addPointsToCustomerByOrderId(orderId, pointsToAdd);
                    
                    // [CẬP NHẬT QUAN TRỌNG] Gọi lệnh Tự Động Refresh lại Tab Khách Hàng sau khi Đơn hàng hoàn tất
                    if (Controller.CustomerController.getInstance() != null) {
                        Controller.CustomerController.getInstance().loadCustomers();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void hiddenButton() throws Exception {
        int currentAccountId = SessionManager.getAccountId();
        
        // 1. Phân quyền Bán hàng (POS)
        int functionIdPos = roleService.getFunctionIdByName("Bán hàng");
        if(functionIdPos == -1) functionIdPos = 6;
        boolean hasViewPos = roleService.isPermissed("Xem", currentAccountId, functionIdPos);
        boolean hasAddPos = roleService.isPermissed("Them", currentAccountId, functionIdPos);
        
        if (mainFrame != null) mainFrame.setMenuVisible("Order", hasViewPos);
        if (posPanel.getBtnCreateOrder() != null) posPanel.getBtnCreateOrder().setVisible(hasAddPos);
        if (posPanel.getBtnClearCart() != null) posPanel.getBtnClearCart().setVisible(hasAddPos);
        if (posPanel.getBtnCheckCustomer() != null) posPanel.getBtnCheckCustomer().setVisible(hasAddPos);
        if (posPanel.getBtnRegisterCustomer() != null) posPanel.getBtnRegisterCustomer().setVisible(hasAddPos);
        if (posPanel.getBtnUsePoints() != null) posPanel.getBtnUsePoints().setVisible(hasAddPos);
        
        posPanel.setActionPermissions(hasAddPos, hasAddPos); // Edit/Delete trong cart cần quyền Add đơn

        // 2. Phân quyền Quản lý đơn hàng (Order)
        int functionIdOrder = roleService.getFunctionIdByName("Quản lý đơn hàng");
        if(functionIdOrder == -1) functionIdOrder = 7;
        boolean hasViewOrder = roleService.isPermissed("Xem", currentAccountId, functionIdOrder);
        boolean hasEditOrder = roleService.isPermissed("Sua", currentAccountId, functionIdOrder);
        boolean hasDeleteOrder = roleService.isPermissed("Xoa", currentAccountId, functionIdOrder);
        
        if (mainFrame != null) mainFrame.setMenuVisible("OrderList", hasViewOrder);
        orderPanel.setActionPermissions(hasEditOrder, hasDeleteOrder);
    }
}