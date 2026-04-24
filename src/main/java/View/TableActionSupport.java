package View;

import javax.swing.*;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Dùng chung cho mọi JTable cần cột nút "Sửa / Xóa".
 *
 * Cách dùng:
 *   TableColumn col = table.getColumnModel().getColumn(n);
 *   col.setCellRenderer(TableActionSupport.renderer());
 *   col.setCellEditor(TableActionSupport.editor((row, action) -> {
 *       if (action == TableActionSupport.Action.EDIT)   handleEdit(row);
 *       if (action == TableActionSupport.Action.DELETE) handleDelete(row);
 *   }));
 */
public class TableActionSupport {

    // ── Enum hành động ───────────────────────────────────────────────────
    public enum Action { EDIT, DELETE }

    // ── Interface gọn (functional interface) ────────────────────────────
    @FunctionalInterface
    public interface ActionListener {
        void onAction(int row, Action action);
    }

    // ── Listener dạng 2 method (tương thích anonymous class cũ) ─────────
    public interface SplitActionListener {
        void onEdit(int row);
        void onDelete(int row);
    }

    // ── Panel chứa 2 nút ─────────────────────────────────────────────────
    static class ActionPanel extends JPanel {
        final JButton btnEdit   = new JButton("Sửa");
        final JButton btnDelete = new JButton("Xóa");

        ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
            setOpaque(true);
            styleBtn(btnEdit,   new Color(0, 122, 255));
            styleBtn(btnDelete, new Color(255, 59, 48));
            add(btnEdit);
            add(btnDelete);
        }

        private void styleBtn(JButton btn, Color color) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(color);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(color, 1));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(60, 30));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    // ── Renderer ─────────────────────────────────────────────────────────
    private static class ActionRenderer implements TableCellRenderer {
        private final ActionPanel panel = new ActionPanel();

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }
    }

    // ── Editor ───────────────────────────────────────────────────────────
    private static class ActionEditor extends DefaultCellEditor {
        private final ActionPanel panel;
        private int currentRow;

        ActionEditor(SplitActionListener listener) {
            super(new JCheckBox());
            this.panel = new ActionPanel();
            panel.btnEdit.addActionListener(e -> {
                stopCellEditing();
                listener.onEdit(currentRow);
            });
            panel.btnDelete.addActionListener(e -> {
                stopCellEditing();
                listener.onDelete(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return ""; }
    }

    // ── Static factory (public API) ───────────────────────────────────────

    /** Renderer dùng chung — không cần tham số. */
    public static TableCellRenderer renderer() {
        return new ActionRenderer();
    }

    /**
     * Editor với SplitActionListener (2 method onEdit / onDelete).
     * Dùng khi muốn anonymous class rõ ràng.
     */
    public static DefaultCellEditor editor(SplitActionListener listener) {
        return new ActionEditor(listener);
    }

    /**
     * Editor với lambda gọn.
     * Ví dụ: editor((row, action) -> { if (action == Action.EDIT) ... })
     */
    public static DefaultCellEditor editor(ActionListener listener) {
        return editor(new SplitActionListener() {
            @Override public void onEdit(int row)   { listener.onAction(row, Action.EDIT);   }
            @Override public void onDelete(int row) { listener.onAction(row, Action.DELETE); }
        });
    }
}