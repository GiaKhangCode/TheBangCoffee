package Controller;

import Model.OrderDetailModel;
import Model.OrderModel;
import Service.CustomerService;
import Service.InvoiceService;
import Service.OrderService;
import Service.RoleService;
import Model.SessionManager;
import Service.PayOSService;
import Service.PayOSService.PaymentResult;
import View.MainFrame;
import View.OrderPanel;
import java.awt.image.BufferedImage;

import javax.swing.*;
import java.util.List;

public class OrderController {

    private MainFrame mainFrame;
    private OrderPanel orderPanel;
    
    private OrderService orderService;
    private CustomerService customerService;
    private InvoiceService invoiceService;
    private RoleService roleService;
    
    private int currentSelectedOrderId = -1;
    private boolean hasPrintOrder = true;

    public OrderController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.orderPanel = mainFrame.getOrderPanel(); 
        
        this.orderService = new OrderService();
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
        
        this.mainFrame.setOrderController(this);
        
        initView();
        initOrderPanelListeners(); 
    }

    private void initView() {
        loadOrderList(); 
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
                OrderModel order = orderService.getOrderById(currentSelectedOrderId);
                BufferedImage qrImage = null;
                long orderCode = -1L;
                PayOSService payOSService = new PayOSService();

                if (order != null && "Chưa thanh toán".equals(order.getPaymentStatus()) && order.getFinalTotal() > 0) {
                    int amount = (int) order.getFinalTotal();
                    PaymentResult paymentData = payOSService.createPaymentLink(currentSelectedOrderId, amount, "Thanh toan don " + currentSelectedOrderId);
                    
                    if (paymentData != null) {
                        qrImage = payOSService.generateQRCodeImage(paymentData.qrCode);
                        orderCode = paymentData.orderCode;
                    }
                }
                boolean isPaid = order != null && "Đã thanh toán".equals(order.getPaymentStatus());
                invoiceService.printInvoice(currentSelectedOrderId, false, qrImage, isPaid); 

                if (orderCode > 0) {
                    final long finalOrderCode = orderCode;
                    final int finalOrderId = currentSelectedOrderId;
                    javax.swing.Timer timer = new javax.swing.Timer(3000, null);
                    timer.addActionListener(new java.awt.event.ActionListener() {
                        int count = 0;
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            count++;
                            String status = payOSService.getPaymentStatus(finalOrderCode);
                            if ("PAID".equals(status)) {
                                ((javax.swing.Timer)evt.getSource()).stop();
                                boolean isSuccess = orderService.updatePaymentStatus(finalOrderId, "Đã thanh toán", "Chuyển khoản");
                                if (isSuccess) {
                                    checkAndRewardPoints(finalOrderId, null, "Đã thanh toán");
                                    loadOrderList(); 
                                    loadOrderDetails(finalOrderId); 
                                    JOptionPane.showMessageDialog(orderPanel, "Khách hàng đã chuyển khoản thành công đơn #" + finalOrderId + "!");
                                }
                            } else if (count > 200) { // Timeout sau 10 phút (200 * 3s)
                                ((javax.swing.Timer)evt.getSource()).stop();
                            }
                        }
                    });
                    timer.start();
                }
            }
        });
    }

    public void loadOrderList() {
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
        
        int confirm = JOptionPane.showConfirmDialog(orderPanel, "Xác nhận thu tiền mặt cho đơn hàng này?", "Xác nhận thu tiền mặt", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String phuongThucThanhToan = "Tiền mặt";
            
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
