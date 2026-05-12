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
    
    // Data cho POS
    private List<ProductModel> allProducts;
    private List<CartItemModel> currentCart;
    private String currentCategoryFilter = "Tất cả";
    
    // [MỚI] Biến lưu trữ ID khách hàng hiện tại đang được chọn (null = khách vãng lai)
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
        
        // Bấm nút [X]: Hủy chọn khách hàng
        posPanel.addClearCustomerListener(e -> {
            posPanel.clearCustomerInfo();
            currentCustomerId = null; // Trở về khách vãng lai
        });
        
        // Đăng ký khách hàng độc lập
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
                    
                    // Gán ID khách hàng vừa tạo vào hệ thống
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

            int confirm = JOptionPane.showConfirmDialog(posPanel, 
                    "Bạn có chắc muốn tạo đơn hàng này?\nTổng tiền: " + String.format("%,d đ", finalTotal), 
                    "Xác nhận tạo đơn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int currentAccountId = 1; // [HARDCODE] 
                boolean isTakeaway = posPanel.isTakeaway();
                boolean isHoliday = posPanel.isHoliday();

                // Tạo Khách mới NẾU đang nhập lỡ dở ở màn hình (Thu ngân bấm Tạo đơn luôn thay vì bấm nút Đăng ký nhỏ)
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
                    finalTotal, 
                    Math.round(totalVat), 
                    "Chờ tiếp nhận", 
                    isTakeaway, 
                    isHoliday
                );

                if (isSuccess) {
                    JOptionPane.showMessageDialog(posPanel, "Tạo đơn hàng thành công (Chờ tiếp nhận)!");
                    currentCart.clear(); 
                    updateCartView();    
                    
                    // Reset Khách Hàng sau khi mua xong
                    posPanel.clearCustomerInfo(); 
                    currentCustomerId = null;
                    
                    loadOrderList(); 
                } else {
                    JOptionPane.showMessageDialog(posPanel, "Lỗi khi tạo đơn hàng. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        posPanel.setCartDeleteListener(row -> {
            if (row >= 0 && row < currentCart.size()) {
                currentCart.remove(row);
                updateCartView();
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
            currentCustomerId = customer.getMaKH(); // Cập nhật ID khách
        } else {
            posPanel.setCustomerStatus("Khách mới! Nhập tên để tạo TK:", true); 
            currentCustomerId = null; // Khách vãng lai chờ đăng ký
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

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
                OrderOptionDialog dialog = new OrderOptionDialog(parentFrame, p, variants, toppings, isTakeaway, isHoliday);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Model.VariantModel selectedSize = dialog.getSelectedVariant();
                    List<Model.ToppingModel> selectedToppings = dialog.getSelectedToppings();
                    int qty = dialog.getQuantity();

                    String note = dialog.getFinalNote();
                    
                    boolean isExist = false;
                    for (CartItemModel existingItem : currentCart) {
                        if (existingItem.isSameItem(p, selectedSize, selectedToppings, note)) {
                            existingItem.setQuantity(existingItem.getQuantity() + qty);
                            isExist = true;
                            break; 
                        }
                    }

                    if (!isExist) {
                        CartItemModel item = new CartItemModel(p, selectedSize, selectedToppings, qty, note);
                        item.setOrderType(isTakeaway, isHoliday);
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

        orderPanel.addAcceptListener(e -> changeOrderStatus("Đang pha chế"));
        orderPanel.addPayListener(e -> changeOrderStatus("Thành công"));
        orderPanel.addCancelListener(e -> changeOrderStatus("Thất bại"));
        
        orderPanel.addCompleteListener(e -> {
            if (currentSelectedOrderId <= 0) return;

            int confirm = JOptionPane.showConfirmDialog(orderPanel, 
                "Chuyển trạng thái sang HOÀN THÀNH? Hệ thống sẽ tiến hành trừ nguyên liệu trong kho.", 
                "Xác nhận Hoàn thành", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean isSuccess = orderService.completeOrderAndDeductInventory(currentSelectedOrderId);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(orderPanel, "Hoàn thành đơn và trừ kho thành công!");
                    loadOrderList();
                    loadOrderDetails(currentSelectedOrderId);
                } else {
                    JOptionPane.showMessageDialog(orderPanel, "Có lỗi xảy ra khi hoàn thành đơn (Hoặc nguyên liệu không đủ)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
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
                    o.getStatus(),
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
                order.getStatus(), 
                String.format("%,d đ", order.getFinalTotal())
            );
            
            orderPanel.updateActionButtons(order.getStatus());
            
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

    private void changeOrderStatus(String newStatus) {
        if (currentSelectedOrderId <= 0) return;
        
        int confirm = JOptionPane.showConfirmDialog(orderPanel, 
            "Chuyển trạng thái đơn hàng thành: [" + newStatus + "] ?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isSuccess = orderService.updateOrderStatus(currentSelectedOrderId, newStatus);
            if (isSuccess) {
                loadOrderList(); 
                loadOrderDetails(currentSelectedOrderId); 
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Không thể cập nhật trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }
}