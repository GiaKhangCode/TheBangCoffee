package com.is216.thebangcf;

import Controller.AccountController;
import Controller.ProductController;
import Controller.StockPanelController;
import View.MainFrame;
import java.sql.SQLException;
import javax.swing.SwingUtilities;


public class TheBangCF {
    
    public static void main(String[] args) {
        
        // Đưa việc khởi tạo giao diện vào luồng an toàn của Swing (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. XÂY NGÔI NHÀ CHUNG: Khởi tạo MainFrame duy nhất
                    MainFrame sharedMainFrame = new MainFrame();
                    
                    // 2. GIAO NHIỆM VỤ: Khởi tạo các Controller và truyền MainFrame vào
                    
                    // AccountController nhận MainFrame, tự động mở màn hình Login lên
                    new AccountController(sharedMainFrame);
                    
                } catch (SQLException ex) {
                    System.err.println("Lỗi kết nối cơ sở dữ liệu khi khởi động ứng dụng!");
                    ex.printStackTrace();
                }
            }
        });
        
    }
}