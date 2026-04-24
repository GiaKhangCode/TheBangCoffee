package View;

import java.awt.*;

public class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = (target.getParent() != null) ? target.getParent().getWidth() : 400;
            if (targetWidth == 0) targetWidth = 400;

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            for (int i = 0; i < target.getComponentCount(); i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    rowWidth += d.width + hgap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight + insets.top + insets.bottom + vgap * 2;
            return dim;
        }
    }
}