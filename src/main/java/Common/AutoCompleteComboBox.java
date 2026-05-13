package Common;

import javax.swing.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * AutoCompleteComboBox - Generic Version
 * An toàn, chống lỗi mất Focus và ClassCastException.
 */
public class AutoCompleteComboBox<E> extends JComboBox<E> {

    private List<E> originalItems;

    public AutoCompleteComboBox() {
        super();
        this.originalItems = new ArrayList<>();
        initAutoComplete();
    }

    // Hàm an toàn để nạp dữ liệu từ Database
    public void setData(List<E> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        updateModel(originalItems);
        this.setSelectedItem(null); // Reset chọn lựa khi data thay đổi
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
                        boolean isExactMatch = false;
                        
                        for (int i = 0; i < getItemCount(); i++) {
                            // Dùng toString() để so sánh chung cho cả String và Object
                            if (getItemAt(i).toString().equals(typedText)) {
                                isExactMatch = true;
                                setSelectedItem(getItemAt(i));
                                break;
                            }
                        }
                        
                        if (!isExactMatch) {
                            E top1Match = getItemAt(0);
                            textField.setText(top1Match.toString());
                            setSelectedItem(top1Match);
                        }
                        hidePopup();
                    }
                    return;
                }

                // Bỏ qua phím điều hướng
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                    keyCode == KeyEvent.VK_ESCAPE ||
                    keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    String input = textField.getText();
                    List<E> filteredList = new ArrayList<>();

                    for (E item : originalItems) {
                        // Tìm kiếm trên chuỗi toString()
                        if (isFuzzyMatch(input, item.toString())) {
                            filteredList.add(item);
                        }
                    }

                    updateModel(filteredList);
                    textField.setText(input); // Giữ nguyên chữ đang gõ

                    if (filteredList.isEmpty() || input.isEmpty()) {
                        hidePopup();
                    } else {
                        showPopup();
                    }
                });
            }
        });
    }

    // [ĐÃ SỬA] Dùng vòng lặp addElement để tránh lỗi ép kiểu Array của Java
    private void updateModel(List<E> filteredList) {
        DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>();
        for (E item : filteredList) {
            model.addElement(item);
        }
        this.setModel(model);
    }

    // =======================================================
    // CÁC HÀM LOGIC TÌM KIẾM (GIỮ NGUYÊN CỦA BẠN)
    // =======================================================
    private boolean isFuzzyMatch(String input, String target) {
        if (input.trim().isEmpty()) return true;

        String normInput = removeAccents(input.toLowerCase().trim());
        String normTarget = removeAccents(target.toLowerCase());

        if (normTarget.contains(normInput)) return true;

        String targetAcronym = getAcronym(normTarget);
        if (targetAcronym.contains(normInput)) return true;

        int maxTyposAllowed = normInput.length() <= 4 ? 1 : 2;
        for (String word : normTarget.split(" ")) {
            if (levenshteinDistance(normInput, word) <= 1) return true;
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
            if (!word.isEmpty()) acronym.append(word.charAt(0));
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