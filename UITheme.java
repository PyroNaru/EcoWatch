import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Production-grade design system and UI toolkit for EcoWatch.
 * Provides custom brand icons, color palettes, responsive typography,
 * interactive button states, badge renderers, and window initialization.
 */
public class UITheme {

    // --- Color Palette ---
    public static final Color PRIMARY = new Color(22, 101, 52);          // Deep Forest Green (#166534)
    public static final Color PRIMARY_HOVER = new Color(20, 83, 45);
    public static final Color PRIMARY_LIGHT = new Color(240, 253, 244);  // Soft mint (#F0FDF4)
    public static final Color ACCENT_TEAL = new Color(13, 148, 136);

    public static final Color BG_APP = new Color(248, 250, 252);         // Slate-50 (#F8FAFC)
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);          // Slate-200 (#E2E8F0)
    public static final Color BORDER_FOCUS = new Color(34, 197, 94);

    public static final Color TEXT_MAIN = new Color(15, 23, 42);          // Slate-900 (#0F172A)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);      // Slate-500 (#64748B)
    public static final Color TEXT_LIGHT = Color.WHITE;

    public static final Color DANGER = new Color(220, 38, 38);            // Red-600
    public static final Color DANGER_HOVER = new Color(185, 28, 28);
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);

    // --- Status Badge Colors ---
    public static final Color STATUS_PENDING_BG = new Color(254, 243, 199);   // Amber-100
    public static final Color STATUS_PENDING_TXT = new Color(180, 83, 9);     // Amber-700
    public static final Color STATUS_REVIEW_BG = new Color(224, 242, 254);    // Sky-100
    public static final Color STATUS_REVIEW_TXT = new Color(3, 105, 161);     // Sky-700
    public static final Color STATUS_RESOLVED_BG = new Color(220, 252, 231);  // Green-100
    public static final Color STATUS_RESOLVED_TXT = new Color(21, 128, 61);   // Green-700

    // --- Severity Badge Colors ---
    public static final Color SEV_LOW_BG = new Color(241, 245, 249);          // Slate-100
    public static final Color SEV_LOW_TXT = new Color(71, 85, 105);          // Slate-600
    public static final Color SEV_MED_BG = new Color(254, 243, 199);          // Amber-100
    public static final Color SEV_MED_TXT = new Color(217, 119, 6);          // Amber-600
    public static final Color SEV_HIGH_BG = new Color(254, 226, 226);         // Red-100
    public static final Color SEV_HIGH_TXT = new Color(220, 38, 38);          // Red-600

    // --- Typography ---
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.BOLD, 11);

    private static Image cachedIcon = null;

    /**
     * Generates a high-resolution, vector-quality application icon
     * representing an environmental seedling/shield in forest green.
     */
    public static synchronized Image getAppIcon() {
        ImageIcon icon = new ImageIcon("assets/FramedLogo.png");
        return icon.getImage();
    }

    /**
     * Standardizes a Window (Frame or Dialog) with the brand icon,
     * minimum dimensions, and Escape-key dismiss bindings for dialogs.
     */
    public static void setupWindow(Window window, int minW, int minH) {
    window.setIconImage(getAppIcon());
    window.setMinimumSize(new Dimension(minW, minH));
    if (window instanceof JDialog) {
        JRootPane root = ((JDialog) window).getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE_DISMISS");
        root.getActionMap().put("ESCAPE_DISMISS", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { window.dispose(); }
        });
    }
}

    /** Creates a primary button with rounded styling and hover states. */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY, PRIMARY_HOVER, TEXT_LIGHT, null);
    }

    /** Creates a secondary button with light slate background. */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, new Color(241, 245, 249), new Color(226, 232, 240), TEXT_MAIN, null);
    }

    /** Creates a danger / delete button with soft red styling. */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_LIGHT, new Color(254, 202, 202), DANGER, null);
    }

    public static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg, String tooltip) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color curBg = getModel().isPressed() ? hoverBg.darker() :
                              getModel().isRollover() ? hoverBg : bg;
                g2.setColor(curBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (tooltip != null) btn.setToolTipText(tooltip);
        return btn;
    }

    /** Styles text inputs with consistent padding and modern slate borders. */
    public static void styleInput(JComponent comp) {
        comp.setFont(FONT_BODY);
        if (comp instanceof JTextField) {
            comp.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }
    }

    /** Table cell renderer for Status & Severity badges. */
    public static class BadgeCellRenderer extends DefaultTableCellRenderer {
        private final boolean isStatus;

        public BadgeCellRenderer(boolean isStatus) {
            this.isStatus = isStatus;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(FONT_BADGE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String val = value == null ? "" : value.toString().trim();
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
            label.setText(val);
            label.setFont(FONT_BADGE);
            label.setOpaque(false);

            Color bg;
            Color fg;

            if (isStatus) {
                switch (val.toUpperCase()) {
                    case "PENDING":
                        bg = STATUS_PENDING_BG; fg = STATUS_PENDING_TXT; break;
                    case "UNDER REVIEW":
                        bg = STATUS_REVIEW_BG; fg = STATUS_REVIEW_TXT; break;
                    case "RESOLVED":
                        bg = STATUS_RESOLVED_BG; fg = STATUS_RESOLVED_TXT; break;
                    default:
                        bg = Color.LIGHT_GRAY; fg = Color.BLACK;
                }
            } else { // Severity
                switch (val.toUpperCase()) {
                    case "HIGH":
                        bg = SEV_HIGH_BG; fg = SEV_HIGH_TXT; break;
                    case "MEDIUM":
                        bg = SEV_MED_BG; fg = SEV_MED_TXT; break;
                    case "LOW":
                        bg = SEV_LOW_BG; fg = SEV_LOW_TXT; break;
                    default:
                        bg = Color.LIGHT_GRAY; fg = Color.BLACK;
                }
            }

            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isSelected) {
                        g2.setColor(new Color(220, 252, 231));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    } else if (row % 2 == 1) {
                        g2.setColor(new Color(248, 250, 252));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }

                    int badgeWidth = Math.min(getWidth() - 12, 100);
                    int badgeHeight = 22;
                    int x = (getWidth() - badgeWidth) / 2;
                    int y = (getHeight() - badgeHeight) / 2;

                    // Fill pill
                    g2.setColor(bg);
                    g2.fillRoundRect(x, y, badgeWidth, badgeHeight, 10, 10);

                    // Subtle pill border
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60));
                    g2.drawRoundRect(x, y, badgeWidth, badgeHeight, 10, 10);

                    // Badge text
                    g2.setColor(fg);
                    g2.setFont(FONT_BADGE);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (badgeWidth - fm.stringWidth(val)) / 2;
                    int ty = y + ((badgeHeight - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(val, tx, ty);
                    g2.dispose();
                }
            };
        }
    }
}
