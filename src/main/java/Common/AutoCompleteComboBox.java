/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

/**
 *
 * @author SONY
 */
import javax.swing.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AutoCompleteComboBox extends JComboBox<String> {

    // Lưu trữ danh sách dữ liệu gốc để không bị mất khi filter
    private List<String> originalItems;

    public AutoCompleteComboBox() {
        super();
        this.originalItems = new ArrayList<>(); // Khởi tạo list rỗng để chặn NullPointerException
        initAutoComplete();
    }
    
    // Khởi tạo với List
    public AutoCompleteComboBox(List<String> items) {
        super(items.toArray(new String[0]));
        this.originalItems = new ArrayList<>(items);
        initAutoComplete();
    }

    // Khởi tạo với Array
    public AutoCompleteComboBox(String[] items) {
        super(items);
        this.originalItems = new ArrayList<>(Arrays.asList(items));
        initAutoComplete();
    }

    // Hàm để bạn cập nhật lại dữ liệu từ Database nếu cần
    public void setData(List<String> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        updateModel(originalItems);
    }

    private void initAutoComplete() {
        this.setEditable(true);
        JTextField textField = (JTextField) this.getEditor().getEditorComponent();

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ENTER) {
                    if (getItemCount() > 0) {
                        String typedText = textField.getText();
                        
                        // Kiểm tra xem text hiện tại có khớp chính xác với item nào trong danh sách không
                        // (Trường hợp người dùng dùng phím mũi tên lên/xuống để chọn)
                        boolean isExactMatch = false;
                        for (int i = 0; i < getItemCount(); i++) {
                            if (getItemAt(i).equals(typedText)) {
                                isExactMatch = true;
                                setSelectedItem(getItemAt(i));
                                break;
                            }
                        }
                        
                        // Nếu gõ viết tắt (ví dụ "tdcs") rồi ấn Enter luôn mà chưa dùng mũi tên
                        // -> Tự động điền phần tử đầu tiên (top 1 match)
                        if (!isExactMatch) {
                            String top1Match = getItemAt(0);
                            textField.setText(top1Match);
                            setSelectedItem(top1Match);
                        }
                        
                        hidePopup(); // Đóng danh sách gợi ý lại
                    }
                    return;
                }

                // Bỏ qua các phím điều hướng để không làm mất focus
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                    keyCode == KeyEvent.VK_ESCAPE ||
                    keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    String input = textField.getText();
                    List<String> filteredList = new ArrayList<>();

                    for (String item : originalItems) {
                        if (isFuzzyMatch(input, item)) {
                            filteredList.add(item);
                        }
                    }

                    updateModel(filteredList);
                    textField.setText(input); // Trả lại text người dùng đang gõ

                    if (filteredList.isEmpty() || input.isEmpty()) {
                        hidePopup();
                    } else {
                        showPopup();
                    }
                });
            }
        });
    }

    private void updateModel(List<String> filteredList) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(filteredList.toArray(new String[0]));
        this.setModel(model);
    }

    // --- CÁC HÀM LOGIC TÌM KIẾM ---

    private boolean isFuzzyMatch(String input, String target) {
        if (input.trim().isEmpty()) return true;

        String normInput = removeAccents(input.toLowerCase().trim());
        String normTarget = removeAccents(target.toLowerCase());

        // 1. Tìm chứa chuỗi (Ví dụ: "cam" -> "Trà đào cam sả")
        if (normTarget.contains(normInput)) return true;

        // 2. Tìm theo viết tắt (Ví dụ: "tdcs" -> "Trà đào cam sả")
        String targetAcronym = getAcronym(normTarget);
        if (targetAcronym.contains(normInput)) return true;

        // 3. Tìm sai chính tả (Levenshtein)
        int maxTyposAllowed = normInput.length() <= 4 ? 1 : 2;
        for (String word : normTarget.split(" ")) {
            if (levenshteinDistance(normInput, word) <= 1) {
                return true;
            }
        }

        return levenshteinDistance(normInput, normTarget) <= maxTyposAllowed;
    }

    private String removeAccents(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }

    private String getAcronym(String text) {
        StringBuilder acronym = new StringBuilder();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                acronym.append(word.charAt(0));
            }
        }
        return acronym.toString();
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}
