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
    private int diemDoiMotLy = 50;
    
    private int globalPointsUsed = 0;
    private long globalDiscountAmount = 0;
    private RoleService roleService;
    private boolean hasPrintOrder = true;

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
            diemDoiMotLy = rules[2] > 0 ? rules[2] : 50;

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

        posPanel.setCartQuantityListener(new View.PosPanel.QuantityActionListener() {
            @Override
            public void onIncrease(int row) {
                if (row >= 0 && row < currentCart.size()) {
                    CartItemModel item = currentCart.get(row);
                    
                    // Tạo bản sao để kiểm tra kho
                    List<CartItemModel> testCart = new ArrayList<>();
                    for (CartItemModel cartItem : currentCart) {
                        CartItemModel cloneItem = new CartItemModel(
                            cartItem.getProduct(), 
                            cartItem.getSelectedVariant(), 
                            cartItem.getSelectedToppings(), 
                            cartItem.getQuantity(), 
                            cartItem.getNote()
                        );
                        cloneItem.setReward(cartItem.isReward());
                        testCart.add(cloneItem);
                    }
                    
                    // Tăng thử số lượng của dòng tương ứng trong testCart lên 1
                    testCart.get(row).setQuantity(testCart.get(row).getQuantity() + 1);
                    
                    // Thực hiện kiểm tra kho
                    String errorMsg = orderService.validateInventory(testCart);
                    if (errorMsg != null) {
                        JOptionPane.showMessageDialog(posPanel, 
                            "Kho không đủ nguyên liệu để thêm lượng này!\n\n" + errorMsg, 
                            "Cảnh báo Hết Hàng", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Nếu đủ nguyên liệu, tăng số lượng thật
                    item.setQuantity(item.getQuantity() + 1);
                    updateCartView();
                }
            }

            @Override
            public void onDecrease(int row) {
                if (row >= 0 && row < currentCart.size()) {
                    CartItemModel item = currentCart.get(row);
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        updateCartView();
                    } else {
                        int confirm = JOptionPane.showConfirmDialog(posPanel,
                            "Bạn có muốn xoá sản phẩm \"" + item.getDisplayName() + "\" khỏi giỏ hàng?",
                            "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION) {
                            currentCart.remove(row);
                            updateCartView();
                        }
                    }
                }
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

            for (CartItemModel item : currentCart) {
                finalTotal += item.getTotalPrice();
                totalVat += item.getTotalVatAmount();
            }
            
            long nonRewardTotal = 0;
            for (CartItemModel item : currentCart) {
                if (!item.isReward()) {
                    nonRewardTotal += item.getTotalPrice();
                }
            }
            
            long subTotal = finalTotal - Math.round(totalVat);
            
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
            View.CheckoutDialog dialog = new View.CheckoutDialog(parentFrame, subTotal, totalVat, finalTotal, nonRewardTotal, giaTriMotDiem, tienTichMotDiem, diemDoiMotLy, currentCart);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                int selectedCustomerId = dialog.getCustomerId(); // -1 nếu là khách vãng lai
                int pointsUsed = dialog.getPointsUsed();
                long discountAmt = dialog.getDiscountAmount();
                long actualFinalTotal = finalTotal - discountAmt;
                if (actualFinalTotal < 0) actualFinalTotal = 0;

                int pointsUsedForItems = 0;
                for (CartItemModel item : currentCart) {
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
                
                int totalPointsToDeduct = pointsUsedForItems + pointsUsed;

                // [SỬA] VAT luôn tính trên tiền hàng GỐC (trước khi giảm giá bằng điểm).
                // Không scale VAT theo tỉ lệ actualFinalTotal/finalTotal nữa.
                // Lý do: Nghĩa vụ thuế phát sinh từ giá trị hàng hoá, không phải từ số tiền khách trả.
                // ThanhTien (tiền khách trả thực tế) vẫn = 0đ, còn TongTienThue vẫn ghi đúng.
                double adjustedVat = totalVat; // Giữ nguyên VAT gốc

                int currentAccountId = 1; 
                boolean isTakeaway = posPanel.isTakeaway();
                boolean isHoliday = posPanel.isHoliday();

                int newOrderId = orderService.createOrder(
                    currentAccountId, 
                    selectedCustomerId > 0 ? selectedCustomerId : null,       
                    currentCart, 
                    actualFinalTotal, 
                    Math.round(adjustedVat), 
                    "Chờ tiếp nhận",   
                    "Chưa thanh toán",
                    isTakeaway, 
                    isHoliday,
                    0, 
                    totalPointsToDeduct,
                    discountAmt 
                );

                if (newOrderId > 0) {
                    // Tự động in hoá đơn
                    invoiceService.printInvoice(newOrderId, false);

                    // Tự động đồng bộ hạng khách hàng khi điểm thay đổi (do dùng điểm)
                    if (selectedCustomerId > 0 && totalPointsToDeduct > 0) {
                        try {
                            customerService.syncTiers();
                            if (Controller.CustomerController.getInstance() != null) {
                                Controller.CustomerController.getInstance().loadCustomers();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    JOptionPane.showMessageDialog(posPanel, "Tạo đơn hàng thành công (Chờ tiếp nhận)!");
                    currentCart.clear(); 
                    globalPointsUsed = 0;
                    globalDiscountAmount = 0;
                    currentCustomerId = null;
                    updateCartView();    
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

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
                
                // [SỬA BUG] Truyền đúng diemDoiMotLy (số điểm cần để đổi 1 ly), không phải giaTriMotDiem (giá trị tiền của 1 điểm)
                OrderOptionDialog dialog = new OrderOptionDialog(parentFrame, p, variants, toppings, isTakeaway, isHoliday, currentPoints, diemDoiMotLy);

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
                    
                    // Tải lại bảng nguyên liệu tồn kho ở Tab Nhập kho
                    try {
                        if (Controller.StockPanelController.getInstance() != null) {
                            Controller.StockPanelController.getInstance().loadIngredientToView();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    loadOrderList();
                    loadOrderDetails(currentSelectedOrderId);
                } else {
                    JOptionPane.showMessageDialog(orderPanel, "Có lỗi xảy ra khi hoàn thành món (Hoặc nguyên liệu không đủ)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        orderPanel.addPrintInvoiceListener(e -> {
            if (!hasPrintOrder) {
                JOptionPane.showMessageDialog(orderPanel, "Bạn không có quyền in hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
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
                String.format("%,d đ", order.getFinalTotal()),
                order.getDiemDaDung(),
                order.getTienGiamGia()
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
                paySuccess = orderService.updatePaymentStatus(currentSelectedOrderId, "Chưa thanh toán", "Chưa thanh toán");
            }

            if (prepSuccess && paySuccess) {
                // Hoàn lại điểm tích lũy đã dùng cho khách hàng (nếu có)
                if (order != null && order.getDiemDaDung() > 0) {
                    customerService.refundPointsToCustomerByOrderId(currentSelectedOrderId, order.getDiemDaDung());
                    if (Controller.CustomerController.getInstance() != null) {
                        Controller.CustomerController.getInstance().loadCustomers();
                    }
                }
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
                    customerService.syncTiers(); // Tự động quét đồng bộ lại hạng cho toàn bộ khách hàng khi điểm tăng
                    
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
        posPanel.setActionPermissions(hasAddPos, hasAddPos); // Edit/Delete trong cart cần quyền Add đơn

        // 2. Phân quyền Quản lý đơn hàng (Order)
        int functionIdOrder = roleService.getFunctionIdByName("Quản lý đơn hàng");
        if(functionIdOrder == -1) functionIdOrder = 7;
        boolean hasViewOrder = roleService.isPermissed("Xem", currentAccountId, functionIdOrder);
        boolean hasEditOrder = roleService.isPermissed("Sua", currentAccountId, functionIdOrder);
        boolean hasDeleteOrder = roleService.isPermissed("Xoa", currentAccountId, functionIdOrder);
        this.hasPrintOrder = roleService.isPermissed("XuatFile", currentAccountId, functionIdOrder);
        
        if (mainFrame != null) mainFrame.setMenuVisible("OrderList", hasViewOrder);
        orderPanel.setActionPermissions(hasEditOrder, hasDeleteOrder, hasPrintOrder);
    }
}