/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package View;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author FAKK
 */
public class OtpDialog extends JDialog {

    private JTextField txtOtp = new JTextField(10);
    private JButton btnConfirm = new JButton("Xác nhận");
    private boolean verified = false;

    public OtpDialog(JFrame parent) {
        super(parent, "Xác minh OTP", true);

        setLayout(new FlowLayout());
        add(new JLabel("Nhập OTP:"));
        add(txtOtp);
        add(btnConfirm);

        btnConfirm.addActionListener(e -> {
            verified = true;
            dispose();
        });

        setSize(250, 120);
        setLocationRelativeTo(parent);
    }

    public String getOtp() {
        return txtOtp.getText();
    }

    public boolean isVerified() {
        return verified;
    }
}
