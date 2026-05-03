package Controller;

import Model.CartItemModel;
import Model.CategoryModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.ProductModel;
import Model.ToppingModel;
import Model.VariantModel;
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
    private OrderPanel orderPanel; // [MỚI] Giao diện quản lý đơn hàng
    
    private ProductService productService;
    private ProductCategoryService categoryService;
    private OrderService orderService;
    private VariantService variantService;
    private ToppingService toppingService;

    // Data cho POS
    private List<ProductModel> allProducts;
    private List<CartItemModel> currentCart;
    private String currentCategoryFilter = "Tất cả";
    
    // Data cho Order Tracking
    private int currentSelectedOrderId = -1;

    public PosController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.posPanel = mainFrame.getPosPanel();
        this.orderPanel = mainFrame.getOrderPanel(); // [MỚI]
        
        this.productService = new ProductService();
        this.categoryService = new ProductCategoryService();
        this.orderService = new OrderService();
        this.currentCart = new ArrayList<>();
        this.variantService = new VariantService();
        this.toppingService = new ToppingService();
        
        initView();
        initPosListeners();
        initOrderPanelListeners(); // [MỚI]
    }

    private void initView() {
        // --- VIEW POS ---
        loadCategories();
        allProducts = productService.getProductList().getProductList(); 
        displayProducts(allProducts);
        updateCartView();
        
        // --- VIEW ORDER TRACKING ---
        loadOrderList(); // Gọi tải danh sách đơn hàng khi mở app
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

            // 1. Validate tồn kho trước khi cho phép lưu
            String inventoryCheckMsg = orderService.validateInventory(currentCart);
            if (inventoryCheckMsg != null) {
                JOptionPane.showMessageDialog(posPanel, 
                    "Không thể tạo đơn hàng do kho thiếu nguyên liệu:\n\n" + inventoryCheckMsg, 
                    "Cảnh báo Hết Hàng", 
                    JOptionPane.WARNING_MESSAGE);
                return; 
            }
            
            // 2. Tính tiền
            long finalTotal = 0; 
            double totalVat = 0; 
            for (CartItemModel item : currentCart) {
                finalTotal += item.getTotalPrice();
                totalVat += item.getTotalVatAmount();
            }

            int confirm = JOptionPane.showConfirmDialog(posPanel, 
                    "Bạn có chắc muốn tạo đơn hàng này?\nTổng tiền: " + String.format("%,d đ", finalTotal), 
                    "Xác nhận tạo đơn", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int currentAccountId = 1; // [HARDCODE] Cần đổi thành Session của nhân viên
                boolean isTakeaway = posPanel.isTakeaway();
                boolean isHoliday = posPanel.isHoliday();

                // 3. Tạo đơn hàng vào CSDL
                boolean isSuccess = orderService.createOrder(
                    currentAccountId, currentCart, finalTotal, Math.round(totalVat), 
                    "Chờ tiếp nhận", isTakeaway, isHoliday
                );

                if (isSuccess) {
                    JOptionPane.showMessageDialog(posPanel, "Tạo đơn hàng thành công (Chờ tiếp nhận)!");
                    currentCart.clear(); 
                    updateCartView();    
                    
                    // [MỚI] Cập nhật lại danh sách đơn hàng bên tab Order Tracking
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
        // 1. Tìm kiếm và Lọc trạng thái
        orderPanel.addSearchListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                loadOrderList();
            }
        });
        
        orderPanel.addFilterListener(e -> loadOrderList());
        orderPanel.addRefreshListener(e -> loadOrderList());

        // 2. Click chọn 1 dòng trên bảng đơn hàng -> Tải chi tiết
        orderPanel.addTableSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = orderPanel.getSelectedOrderRow();
                if (row >= 0) {
                    // Lấy mã đơn hàng ở cột số 0
                    currentSelectedOrderId = Integer.parseInt(orderPanel.getOrderTableModel().getValueAt(row, 0).toString().replace("#", ""));
                    loadOrderDetails(currentSelectedOrderId);
                } else {
                    currentSelectedOrderId = -1;
                    orderPanel.clearOrderInfo();
                }
            }
        });

        // 3. Xử lý các nút thay đổi trạng thái
        orderPanel.addAcceptListener(e -> changeOrderStatus("Đang pha chế"));
        orderPanel.addPayListener(e -> changeOrderStatus("Thành công"));
        orderPanel.addCancelListener(e -> changeOrderStatus("Thất bại"));
        
        // Nút Hoàn thành sẽ kích hoạt Trừ kho
        orderPanel.addCompleteListener(e -> {
            if (currentSelectedOrderId <= 0) return;

            int confirm = JOptionPane.showConfirmDialog(orderPanel, 
                "Chuyển trạng thái sang HOÀN THÀNH? Hệ thống sẽ tiến hành trừ nguyên liệu trong kho.", 
                "Xác nhận Hoàn thành", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // [HÀM CẦN THÊM VÀO ORDER SERVICE] Hàm này sẽ vừa đổi status thành "Hoàn thành", vừa gọi trừ kho
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

        // [HÀM CẦN THÊM VÀO ORDER SERVICE] Truy xuất danh sách OrderModel từ CSDL
        List<OrderModel> orders = orderService.getAllOrders(statusFilter, keyword);
        
        javax.swing.table.DefaultTableModel model = orderPanel.getOrderTableModel();
        model.setRowCount(0);
        
        if (orders != null) {
            for (OrderModel o : orders) {
                model.addRow(new Object[]{
                    "#" + o.getOrderId(),
                    o.getOrderTime(),     // String dạng "DD/MM/YYYY HH:mm"
                    o.getOrderTypeNote(), // "Dùng tại quán", "[LỄ] Mang đi"...
                    o.getStatus(),
                    String.format("%,d đ", o.getFinalTotal())
                });
            }
        }
        
        // Tạm thời xóa lựa chọn hiện tại nếu có
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
            
            // Cập nhật trạng thái các nút
            orderPanel.updateActionButtons(order.getStatus());
            
            javax.swing.table.DefaultTableModel detailModel = orderPanel.getDetailTableModel();
            detailModel.setRowCount(0);
            
            for (OrderDetailModel d : details) {
                detailModel.addRow(new Object[]{
                    d.getDisplayName(), // VD: "Trà sữa thái xanh (Size L, Trân châu)"
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
                loadOrderList(); // Render lại bảng
                loadOrderDetails(currentSelectedOrderId); // Cập nhật lại UI chi tiết
            } else {
                JOptionPane.showMessageDialog(orderPanel, "Không thể cập nhật trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }
}