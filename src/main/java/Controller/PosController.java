package Controller;

import Model.CartItemModel;
import Model.CategoryModel;
import Model.CustomerModel;

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

import View.PosPanel;

import javax.swing.*;
import java.awt.Color;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PosController {

    private MainFrame mainFrame;
    private PosPanel posPanel;

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
    
    private int tienTichMotDiem = 10000;
    private int giaTriMotDiem = 100;
    private int diemDoiMotLy = 50;
    
    private int globalPointsUsed = 0;
    private long globalDiscountAmount = 0;
    private RoleService roleService;

    public PosController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.posPanel = mainFrame.getPosPanel();
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
    }

    public void reloadPosData() {
        try {
            int[] rules = customerService.getPointRule();
            tienTichMotDiem = rules[0] > 0 ? rules[0] : 10000;
            giaTriMotDiem = rules[1] > 0 ? rules[1] : 100;
            diemDoiMotLy = rules[2] > 0 ? rules[2] : 50;

            loadCategories();
            allProducts = productService.getProductList(); 
            filterProducts(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        reloadPosData();
        updateCartView();
    }

    private void initPosListeners() {
        posPanel.addSearchListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterProducts();
            }
        });

        
        // nút huỷ đơn - xoá giỏ hàng
        posPanel.addClearCartListener(e -> {
            if (currentCart.isEmpty()) return;
            if (JOptionPane.showConfirmDialog(posPanel, "Bạn muốn hủy đơn hàng hiện tại?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                currentCart.clear();
                globalPointsUsed = 0;
                globalDiscountAmount = 0;
                updateCartView();
            }
        });

        // nút xoá 1 món trong giỏ hàng
        posPanel.setCartDeleteListener(row -> {
            if (row >= 0 && row < currentCart.size()) {
                currentCart.remove(row);
                updateCartView();
            }
        });

        // nút tăng giảm số lượng 
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
        
        // nút tạo đơn
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
            
            long subTotal = finalTotal - Math.round(totalVat);
            
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
            View.CheckoutDialog dialog = new View.CheckoutDialog(parentFrame, subTotal, totalVat, finalTotal, giaTriMotDiem, tienTichMotDiem, diemDoiMotLy, currentCart);
            
            final long effectiveFinalTotal = finalTotal;
            Runnable updateDiscountState = () -> {
                String text = dialog.getPointsInputText();
                Model.DiscountResultModel result = orderService.calculateOrderDiscount(
                    text, dialog.getCurrentCustomer(), effectiveFinalTotal, diemDoiMotLy, giaTriMotDiem, tienTichMotDiem, currentCart
                );
                dialog.updateDiscountUI(result);
            };

            dialog.addPointsInputListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateDiscountState.run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateDiscountState.run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateDiscountState.run(); }
            });
            dialog.setOnStateChanged(updateDiscountState);
            
            updateDiscountState.run();

            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                int selectedCustomerId = dialog.getCustomerId(); // -1 nếu là khách vãng lai
                int pointsUsed = dialog.getPointsUsed();
                long discountAmt = dialog.getDiscountAmount();
                long actualFinalTotal = finalTotal - discountAmt;
                if (actualFinalTotal < 0) actualFinalTotal = 0;

                int totalPointsToDeduct = pointsUsed;

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
                    // Tự động in tem pha chế và tem dán ly
                    invoiceService.printPreparationStamp(newOrderId, false);
                    invoiceService.printStickerStamp(newOrderId, false);

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
                    if (mainFrame.getOrderController() != null) mainFrame.getOrderController().loadOrderList(); 
                } else {
                    JOptionPane.showMessageDialog(posPanel, "Lỗi khi tạo đơn hàng. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // các loại giá
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
                if (matchCategory && matchName && p.hasAnyRecipe()) filteredList.add(p);
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
                List<VariantModel> allVariants = variantService.getVariantsByProductId(p.getProductID());
                List<VariantModel> validVariants = new ArrayList<>();
                for (VariantModel v : allVariants) {
                    if (v.isHasRecipe()) validVariants.add(v);
                }
                
                List<ToppingModel> toppings = toppingService.getToppingsByProductID(p.getProductID());
                
                boolean isTakeaway = posPanel.isTakeaway();
                boolean isHoliday = posPanel.isHoliday();

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(posPanel);
                
                OrderOptionDialog dialog = new OrderOptionDialog(parentFrame, p, validVariants, toppings, isTakeaway, isHoliday, diemDoiMotLy);

                dialog.setInventoryValidator((newQty, variant, selToppings) -> {
                    List<CartItemModel> testCart = new ArrayList<>();
                    for(CartItemModel item : currentCart) {
                        CartItemModel cloneItem = new CartItemModel(item.getProduct(), item.getSelectedVariant(), item.getSelectedToppings(), item.getQuantity(), item.getNote());
                        testCart.add(cloneItem);
                    }
                    
                    boolean merged = false;
                    for(CartItemModel testItem : testCart) {
                        if (testItem.isSameItem(p, variant, selToppings, "")) { 
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
                    VariantModel selectedSize = dialog.getSelectedVariant();
                    List<ToppingModel> selectedToppings = dialog.getSelectedToppings();
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
        
        if (currentCart.isEmpty()) {
            globalPointsUsed = 0;
            globalDiscountAmount = 0;
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

    }
}