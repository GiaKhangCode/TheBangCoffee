package Controller;

import Model.CartItemModel;
import Model.CategoryModel;
import Model.CustomerModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.ProductModel;
import Model.ToppingModel;
import Model.VariantModel;
import Service.CustomerService;
import Service.InvoiceService;
import Service.OrderService;
import Service.ProductCategoryService;
import Service.ProductService;
import Service.ToppingService;
import Service.VariantService;
import View.MainFrame;
import View.OrderOptionDialog;
import View.OrderPanel;
import View.PosPanel;

import javax.swing.*;
import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

public class PosController {

    private MainFrame mainFrame;
    private PosPanel posPanel;
    private OrderPanel orderPanel;
    
    private ProductService productService;
    private ProductCategoryService categoryService;
    private OrderService orderService;
    private VariantService variantService;
    private ToppingService toppingService;
    private CustomerService customerService;
    private InvoiceService invoiceService;
    
    // Data cho POS
    private List<ProductModel> allProducts;
    private List<CartItemModel> currentCart;
    private String currentCategoryFilter = "Tất cả";
    
    private Integer currentCustomerId = null; 
    
    // Data cho Order Tracking
    private int currentSelectedOrderId = -1;
    private int currentCategoryId = 0;

    public PosController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.posPanel = mainFrame.getPosPanel();
        this.orderPanel = mainFrame.getOrderPanel(); 
        
        this.productService = new ProductService();
        this.categoryService = new ProductCategoryService();
        this.orderService = new OrderService();
        this.currentCart = new ArrayList<>();
        this.variantService = new VariantService();
        this.toppingService = new ToppingService();
        this.customerService = new CustomerService();
        this.invoiceService = new InvoiceService();
        
        initView();
        initPosListeners();
        initOrderPanelListeners(); 
    }

    private void initView() {
        loadCategories();
        allProducts = productService.getProductList().getProductList(); 
        displayProducts(allProducts);
        updateCartView();
        loadOrderList(); 
    }

    // =========================================================================
    // KHU VỰC 1: XỬ LÝ SỰ KIỆN CHO MÀN HÌNH TẠO ĐƠN (POS)
    // =========================================================================
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
                    String info = String.format("%s | Điểm: %d (%s)", 
                        newCustomer.getTenKH(), newCustomer.getDiemTichLuy(), newCustomer.getHangThanhVien());
                    posPanel.setCustomerStatus(info, false); 
                    
                    currentCustomerId = newCustomer.getMaKH();
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
            int pointsEarned = 0; 
            int pointsUsed = 0;  
 
            for (CartItemModel item : currentCart) {
                finalTotal += item.getTotalPrice();
                totalVat += item.getTotalVatAmount();
                if (item.isReward()) {
                    pointsUsed += (item.getQuantity() * 12);
                } else {
                    pointsEarned += item.getQuantity(); // Đếm số ly nhưng ta sẽ khoan cộng
                }
            }
 
            int confirm = JOptionPane.showConfirmDialog(posPanel, 
                    "Bạn có chắc muốn tạo đơn hàng này?\nTổng tiền: " + String.format("%,d đ", finalTotal), 
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
 
                // [CẬP NHẬT] Gửi điểm vào là 0 để hoãn cộng điểm tích lũy, đợi khi "Hoàn Thành" + "Đã Thanh Toán"
                boolean isSuccess = orderService.createOrder(
                    currentAccountId, 
                    currentCustomerId,       
                    currentCart, 
                    finalTotal, 
                    Math.round(totalVat), 
                    "Chờ tiếp nhận",   
                    "Chưa thanh toán",
                    isTakeaway, 
                    isHoliday,
                    0, // Truyền 0 để chưa tích điểm vội
                    pointsUsed
                );
 
                if (isSuccess) {
                    JOptionPane.showMessageDialog(posPanel, "Tạo đơn hàng thành công (Chờ tiếp nhận)!");
                    currentCart.clear(); 
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
    }
    
    private void handleCheckCustomer() {
        String phone = posPanel.getCustomerPhone();
        
        if (phone.isEmpty()) {
            posPanel.clearCustomerInfo();
            currentCustomerId = null;
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(posPanel, "Số điện thoại không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerModel customer = customerService.findCustomerByPhone(phone);
        
        if (customer != null) {
            String info = String.format("%s | Điểm: %d (%s)", 
                    customer.getTenKH(), customer.getDiemTichLuy(), customer.getHangThanhVien());
            posPanel.setCustomerStatus(info, false); 
            currentCustomerId = customer.getMaKH(); 
        } else {
            posPanel.setCustomerStatus("Khách mới! Nhập tên để tạo TK:", true); 
            currentCustomerId = null; 
        }
    }

    private void loadCategories() {
        posPanel.clearCategories();
        posPanel.addCategoryButton("Tất cả", currentCategoryFilter.equals("Tất cả"), e -> {
            currentCategoryFilter = "Tất cả";
            loadCategories(); 
            filterProducts();
        });

        List<CategoryModel> categories = categoryService.getAllCategory().getProductCategoryList();
        if (categories != null) {
            for (CategoryModel cat : categories) {
                posPanel.addCategoryButton(cat.getCategoryName(), currentCategoryFilter.equals(cat.getCategoryName()), e -> {
                    currentCategoryFilter = cat.getCategoryName();
                    loadCategories();
                    filterProducts();
                });
            }
        }
    }

    private void filterProducts() {
        String keyword = posPanel.getSearchText().toLowerCase();
        if (keyword.equals("tìm kiếm tên món...")) keyword = "";

        List<ProductModel> filteredList = new ArrayList<>();
        for (ProductModel p : allProducts) {
            boolean matchCategory = currentCategoryFilter.equals("Tất cả") || p.getCategoryName().equals(currentCategoryFilter);
            boolean matchName = p.getProductName().toLowerCase().contains(keyword);
            if (matchCategory && matchName) filteredList.add(p);
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
                    if (c != null) currentPoints = c.getDiemTichLuy();
                }

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
                OrderOptionDialog dialog = new OrderOptionDialog(parentFrame, p, variants, toppings, isTakeaway, isHoliday, currentPoints);

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
        long finalTotal = 0; 
        double totalVat = 0; 
        for (CartItemModel item : currentCart) {
            finalTotal += item.getTotalPrice();
            totalVat += item.getTotalVatAmount(); 
        }
        long subTotal = finalTotal - Math.round(totalVat);
        posPanel.updateSummary(subTotal, totalVat, finalTotal);
    }

    // =========================================================================
    // KHU VỰC 2: XỬ LÝ SỰ KIỆN CHO MÀN HÌNH QUẢN LÝ ĐƠN HÀNG (ORDER TRACKING)
    // =========================================================================
    
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
                    
                    // [MỚI] Kiểm tra và cộng điểm nếu đã thanh toán
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
                // Tham số false để mở cửa sổ Preview xem trước khi in
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
        
        // Gọi Custom Dialog mới thiết kế
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(orderPanel);
        View.PaymentMethodDialog dialog = new View.PaymentMethodDialog(parentFrame, currentSelectedOrderId);
        dialog.setVisible(true); // Popup sẽ dừng code ở đây chờ người dùng bấm nút
            
        int choice = dialog.getSelectedOption();

        // Xử lý nếu người dùng chọn Tiền mặt (0) hoặc Chuyển khoản (1)
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

    // =========================================================================
    // [SỬA LỖI ORA-00904] XỬ LÝ LOGIC TÍNH TOÁN BẰNG JAVA THAY VÌ BẰNG SQL
    // =========================================================================
    private void checkAndRewardPoints(int orderId, String newPrepStatus, String newPayStatus) {
        OrderModel order = orderService.getOrderById(orderId);
        if (order == null) return;

        // Ưu tiên trạng thái mới truyền vào (vì Database có thể chưa kịp load lại hoàn toàn)
        String prep = newPrepStatus != null ? newPrepStatus : order.getPreparationStatus();
        String pay = newPayStatus != null ? newPayStatus : order.getPaymentStatus();

        if ("Đã hoàn thành".equals(prep) && "Đã thanh toán".equals(pay)) {
            try {
                // Fetch details đã có sẵn của OrderService
                List<OrderDetailModel> details = orderService.getOrderDetailsByOrderId(orderId);
                int pointsToAdd = 0;
                
                if (details != null) {
                    for (OrderDetailModel d : details) {
                        // Bỏ qua các món có giá = 0 (hàng tặng) hoặc tên món chứa chữ "quy đổi điểm"
                        if (d.getTotalRowPrice() > 0 && !d.getDisplayName().contains("Hàng quy đổi điểm")) {
                            pointsToAdd += d.getQuantity();
                        }
                    }
                }
                
                // Cập nhật xuống CSDL nếu có điểm để cộng
                if (pointsToAdd > 0) {
                    customerService.addPointsToCustomerByOrderId(orderId, pointsToAdd);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}