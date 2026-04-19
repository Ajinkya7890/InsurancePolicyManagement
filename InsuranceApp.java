import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.io.*;
import java.nio.file.*;

/**
 * Insurance Policy Management System
 * Run: javac InsuranceApp.java && java InsuranceApp
 */
public class InsuranceApp {

    // ── Color palette ──────────────────────────────────────────────────
    static final Color BG        = new Color(0xF0F4FF);
    static final Color SIDEBAR   = new Color(0x1A2347);
    static final Color ACCENT    = new Color(0x3D6EF5);
    static final Color ACCENT2   = new Color(0x22D3A5);
    static final Color CARD      = Color.WHITE;
    static final Color TEXT_DARK = new Color(0x1A2347);
    static final Color TEXT_MID  = new Color(0x5C6B8A);
    static final Color TEXT_LITE = new Color(0xA0AABF);
    static final Color DANGER    = new Color(0xF05252);
    static final Color WARNING   = new Color(0xF5A623);
    static final Color SUCCESS   = new Color(0x22D3A5);

    static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  22);
    static final Font FONT_HEAD   = new Font("SansSerif", Font.BOLD,  15);
    static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
    static final Font FONT_NUM    = new Font("Monospaced", Font.BOLD, 18);

    // ── Data model ─────────────────────────────────────────────────────
    static List<Policy> policies = new ArrayList<>();
    static int policyCounter = 1000;

    static final String[] POLICY_TYPES  = {"Life", "Health", "Vehicle", "Home", "Travel"};
    static final String[] STATUS_LABELS = {"Active", "Expired", "Pending"};

    // ── Main panels (set from outside) ─────────────────────────────────
    static JPanel contentArea;
    static JLabel statusBar;
    static Map<String, JButton> navButtons = new LinkedHashMap<>();

    public static void main(String[] args) {
        loadPolicies();  // Load from file
        if (policies.isEmpty()) seedDemoData();  // Seed demo data only if no file exists
        SwingUtilities.invokeLater(InsuranceApp::buildUI);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UI CONSTRUCTION
    // ═══════════════════════════════════════════════════════════════════
    static void buildUI() {
        JFrame frame = new JFrame("InsureEase — Policy Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1150, 720);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(0, 0));

        // Sidebar
        JPanel sidebar = buildSidebar(frame);
        frame.add(sidebar, BorderLayout.WEST);

        // Main content
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BG);
        frame.add(contentArea, BorderLayout.CENTER);

        // Status bar
        statusBar = new JLabel("  Ready");
        statusBar.setFont(FONT_SMALL);
        statusBar.setForeground(TEXT_LITE);
        statusBar.setBorder(new EmptyBorder(4, 12, 4, 12));
        statusBar.setBackground(new Color(0xE8EDFF));
        statusBar.setOpaque(true);
        frame.add(statusBar, BorderLayout.SOUTH);

        // Seed demo data
        seedDemoData();

        // Show dashboard by default
        showPanel("Dashboard");

        frame.setVisible(true);
        animateFadeIn(frame);
    }

    static JPanel buildSidebar(JFrame frame) {
        JPanel sb = new JPanel();
        sb.setBackground(SIDEBAR);
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(210, 0));
        sb.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo
        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(new Color(0x131C3A));
        logo.setBorder(new EmptyBorder(22, 20, 22, 20));
        JLabel logoIcon = new JLabel("⬡");
        logoIcon.setFont(new Font("SansSerif", Font.BOLD, 26));
        logoIcon.setForeground(ACCENT2);
        JLabel logoText = new JLabel("InsureEase");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 17));
        logoText.setForeground(Color.WHITE);
        JPanel logoInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoInner.setOpaque(false);
        logoInner.add(logoIcon);
        logoInner.add(logoText);
        logo.add(logoInner, BorderLayout.CENTER);
        sb.add(logo);

        sb.add(Box.createVerticalStrut(12));

        // Nav items
        String[][] navItems = {
            {"Dashboard", "⊞"},
            {"Policies", "☰"},
            {"Add Policy", "+"},
            {"Calculator", "◈"},
            {"Reports", "▤"}
        };
        for (String[] item : navItems) {
            JButton btn = buildNavButton(item[1] + "  " + item[0], item[0]);
            navButtons.put(item[0], btn);
            sb.add(btn);
            sb.add(Box.createVerticalStrut(4));
        }

        sb.add(Box.createVerticalGlue());

        // Footer
        JLabel ver = new JLabel("  v1.0  •  InsureEase");
        ver.setFont(FONT_SMALL);
        ver.setForeground(TEXT_MID);
        ver.setBorder(new EmptyBorder(12, 16, 16, 0));
        sb.add(ver);

        return sb;
    }

    static JButton buildNavButton(String label, String target) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = label.equals(getClientProperty("active"));
                if (active || getModel().isRollover()) {
                    Color c = active ? ACCENT : new Color(0x2A3560);
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    if (active) {
                        g2.setColor(ACCENT2);
                        g2.fillRoundRect(getWidth()-4, 6, 4, getHeight()-12, 3, 3);
                    }
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(new Color(0xB8C4E0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(210, 44));
        btn.setPreferredSize(new Dimension(210, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 16, 0, 0));
        btn.addActionListener(e -> showPanel(target));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); btn.repaint(); }
            public void mouseExited(MouseEvent e) {
                if (!"active".equals(btn.getClientProperty("active"))) btn.setForeground(new Color(0xB8C4E0));
                btn.repaint();
            }
        });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PANEL ROUTER
    // ═══════════════════════════════════════════════════════════════════
    static void showPanel(String name) {
        navButtons.values().forEach(b -> { b.putClientProperty("active", null); b.setForeground(new Color(0xB8C4E0)); b.repaint(); });
        JButton active = navButtons.get(name);
        if (active != null) { active.putClientProperty("active", active.getText()); active.setForeground(Color.WHITE); active.repaint(); }

        JPanel panel = switch (name) {
            case "Dashboard"  -> buildDashboard();
            case "Policies"   -> buildPoliciesPanel();
            case "Add Policy" -> buildAddPolicyPanel(null);
            case "Calculator" -> buildCalculatorPanel();
            case "Reports"    -> buildReportsPanel();
            default           -> buildDashboard();
        };
        fadeSwitch(contentArea, panel);
        setStatus("Viewing: " + name);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
    static JPanel buildDashboard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header
        JLabel title = new JLabel("Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_DARK);
        JLabel sub = new JLabel("Welcome back! Here's your policy overview.");
        sub.setFont(FONT_BODY);
        sub.setForeground(TEXT_MID);
        JPanel hdr = new JPanel();
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setOpaque(false);
        hdr.add(title);
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(sub);
        root.add(hdr, BorderLayout.NORTH);

        // Stats row
        long active  = policies.stream().filter(p -> "Active".equals(p.status)).count();
        long expired = policies.stream().filter(p -> "Expired".equals(p.status)).count();
        long pending = policies.stream().filter(p -> "Pending".equals(p.status)).count();
        double totalPrem = policies.stream().filter(p -> "Active".equals(p.status)).mapToDouble(p -> p.premium).sum();

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(20, 0, 20, 0));
        stats.add(statCard("Total Policies", String.valueOf(policies.size()), ACCENT, "☰"));
        stats.add(statCard("Active", String.valueOf(active), SUCCESS, "✓"));
        stats.add(statCard("Expired", String.valueOf(expired), DANGER, "✕"));
        stats.add(statCard("Annual Premium", "₹" + formatNum(totalPrem), WARNING, "◈"));
        root.add(stats, BorderLayout.CENTER);

        // Bottom: recent policies table
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel recTitle = new JLabel("Recent Policies");
        recTitle.setFont(FONT_HEAD);
        recTitle.setForeground(TEXT_DARK);
        recTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        bottom.add(recTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Holder", "Type", "Premium", "Status"};
        Object[][] rows = policies.stream()
            .sorted(Comparator.comparingInt((Policy p) -> p.id).reversed())
            .limit(6)
            .map(p -> new Object[]{
                "POL-"+p.id, p.holderName, p.type,
                "₹"+formatNum(p.premium), p.status})
            .toArray(Object[][]::new);

        JTable tbl = styledTable(cols, rows);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xDDE3F5), 1));
        sp.getViewport().setBackground(CARD);
        bottom.add(sp, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    static JPanel statCard(String label, String value, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accent.brighter());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDE3F5), 1, true),
            new EmptyBorder(18, 20, 18, 20)));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 22));
        ico.setForeground(accent);

        JLabel val = new JLabel(value);
        val.setFont(FONT_NUM);
        val.setForeground(TEXT_DARK);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MID);

        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.setOpaque(false);
        txt.add(val);
        txt.add(Box.createVerticalStrut(2));
        txt.add(lbl);

        card.add(ico, BorderLayout.EAST);
        card.add(txt, BorderLayout.CENTER);

        // Hover lift effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent, 2, true), new EmptyBorder(18, 20, 18, 20))); }
            public void mouseExited(MouseEvent e)  { card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xDDE3F5), 1, true), new EmptyBorder(18, 20, 18, 20))); }
        });
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  POLICIES LIST
    // ═══════════════════════════════════════════════════════════════════
    static void rebuildPoliciesPanel() {
        fadeSwitch(contentArea, buildPoliciesPanel());
    }

    static JPanel buildPoliciesPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header row
        JLabel title = new JLabel("All Policies");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_DARK);

        JTextField search = roundField("🔍  Search by name or ID...", 220);
        JComboBox<String> filter = styledCombo(new String[]{"All Types", "Life", "Health", "Vehicle", "Home", "Travel"});
        JButton addBtn = accentButton("+ Add Policy", ACCENT);
        addBtn.addActionListener(e -> showPanel("Add Policy"));

        JPanel hdrRow = new JPanel(new BorderLayout(10, 0));
        hdrRow.setOpaque(false);
        hdrRow.add(title, BorderLayout.WEST);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        controls.add(search);
        controls.add(filter);
        controls.add(addBtn);
        hdrRow.add(controls, BorderLayout.EAST);
        root.add(hdrRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"Policy ID", "Holder Name", "Type", "Age", "Sum Assured", "Annual Premium", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        populateModel(model, policies);

        JTable tbl = styledTable(model);
        // Actions column
        tbl.getColumn("Actions").setCellRenderer(new ActionCellRenderer());
        tbl.getColumn("Actions").setCellEditor(new ActionCellEditor(model, tbl));
        tbl.getColumn("Actions").setPreferredWidth(130);
        tbl.setRowHeight(40);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xDDE3F5)));
        sp.getViewport().setBackground(CARD);
        root.add(sp, BorderLayout.CENTER);

        // Search/filter listeners
        DocumentListenerHelper dlh = () -> {
            String q   = search.getText().toLowerCase().trim().replace("🔍  search by name or id...", "");
            String typ = (String) filter.getSelectedItem();
            List<Policy> filtered = policies.stream()
                .filter(p -> (q.isEmpty() || p.holderName.toLowerCase().contains(q) || String.valueOf(p.id).contains(q)))
                .filter(p -> ("All Types".equals(typ) || p.type.equals(typ)))
                .toList();
            model.setRowCount(0);
            populateModel(model, filtered);
        };
        search.getDocument().addDocumentListener(dlh);
        filter.addActionListener(e -> dlh.update());

        return root;
    }

    static void populateModel(DefaultTableModel model, List<Policy> list) {
        for (Policy p : list) {
            model.addRow(new Object[]{
                "POL-" + p.id, p.holderName, p.type, p.age,
                "₹" + formatNum(p.sumAssured),
                "₹" + formatNum(p.premium),
                p.status, "Edit | Delete"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ADD / EDIT POLICY FORM
    // ═══════════════════════════════════════════════════════════════════
    static JPanel buildAddPolicyPanel(Policy editing) {
        boolean isEdit = editing != null;
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel(isEdit ? "Edit Policy" : "Add New Policy");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_DARK);
        JLabel sub = new JLabel(isEdit ? "Modify the policy details below." : "Fill in the details to create a new policy.");
        sub.setFont(FONT_BODY);
        sub.setForeground(TEXT_MID);
        JPanel hdr = new JPanel();
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setOpaque(false);
        hdr.add(title);
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(sub);
        root.add(hdr, BorderLayout.NORTH);

        // Card form
        JPanel card = roundCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JTextField fName    = roundField("Full name", 0);
        JTextField fAge     = roundField("e.g. 35", 0);
        JComboBox<String> fType    = styledCombo(POLICY_TYPES);
        JTextField fAssured = roundField("e.g. 1000000", 0);
        JTextField fPhone   = roundField("10-digit number", 0);
        JTextField fEmail   = roundField("email@example.com", 0);
        JComboBox<String> fStatus = styledCombo(STATUS_LABELS);
        JTextField fDuration = roundField("Years (1-30)", 0);

        // Premium display
        JLabel premLabel = new JLabel("₹0.00");
        premLabel.setFont(FONT_NUM);
        premLabel.setForeground(ACCENT);
        premLabel.setBorder(new EmptyBorder(6, 12, 6, 12));

        if (isEdit) {
            fName.setText(editing.holderName); fAge.setText(String.valueOf(editing.age));
            fType.setSelectedItem(editing.type); fAssured.setText(String.valueOf((long)editing.sumAssured));
            fPhone.setText(editing.phone); fEmail.setText(editing.email);
            fStatus.setSelectedItem(editing.status); fDuration.setText(String.valueOf(editing.durationYears));
            premLabel.setText("₹" + formatNum(editing.premium));
        }

        // Auto-calc premium on change
        ActionListener calcListener = e -> {
            try {
                int age = Integer.parseInt(fAge.getText().trim());
                double sa = Double.parseDouble(fAssured.getText().trim());
                int dur  = Integer.parseInt(fDuration.getText().trim());
                String type = (String) fType.getSelectedItem();
                double prem = calcPremium(type, age, sa, dur);
                premLabel.setText("₹" + formatNum(prem));
            } catch (Exception ex) { premLabel.setText("—"); }
        };
        DocumentListenerHelper docListener = () -> {
            try {
                int age = Integer.parseInt(fAge.getText().trim());
                double sa = Double.parseDouble(fAssured.getText().trim());
                int dur  = Integer.parseInt(fDuration.getText().trim());
                String type = (String) fType.getSelectedItem();
                double prem = calcPremium(type, age, sa, dur);
                premLabel.setText("₹" + formatNum(prem));
            } catch (Exception ex) { premLabel.setText("—"); }
        };
        fAge.getDocument().addDocumentListener(docListener);
        fAssured.getDocument().addDocumentListener(docListener);
        fDuration.getDocument().addDocumentListener(docListener);
        fType.addActionListener(calcListener);

        // Layout rows
        Object[][] rows = {
            {"Policyholder Name *", fName,    "Age *",          fAge},
            {"Policy Type *",       fType,    "Duration (Yrs)*",fDuration},
            {"Sum Assured (₹) *",   fAssured, "Status",         fStatus},
            {"Phone",               fPhone,   "Email",          fEmail},
        };
        int row = 0;
        for (Object[] r : rows) {
            gbc.gridy = row; gbc.weightx = 0.15; gbc.gridx = 0; card.add(formLabel((String)r[0]), gbc);
            gbc.weightx = 0.35; gbc.gridx = 1; card.add((Component)r[1], gbc);
            gbc.weightx = 0.15; gbc.gridx = 2; card.add(formLabel((String)r[2]), gbc);
            gbc.weightx = 0.35; gbc.gridx = 3; card.add((Component)r[3], gbc);
            row++;
        }
        // Premium row
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.15; card.add(formLabel("Calculated Premium"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35; card.add(premLabel, gbc);

        // Buttons
        JButton save   = accentButton(isEdit ? "Save Changes" : "Create Policy", ACCENT);
        JButton cancel = ghostButton("Cancel");

        save.addActionListener(e -> {
            if (fName.getText().isBlank() || fAge.getText().isBlank() ||
                fAssured.getText().isBlank() || fDuration.getText().isBlank()) {
                showToast("Please fill all required fields.", DANGER); return;
            }
            try {
                String name = fName.getText().trim();
                int age     = Integer.parseInt(fAge.getText().trim());
                String type = (String) fType.getSelectedItem();
                double sa   = Double.parseDouble(fAssured.getText().trim());
                int dur     = Integer.parseInt(fDuration.getText().trim());
                String stat = (String) fStatus.getSelectedItem();
                String phn  = fPhone.getText().trim();
                String eml  = fEmail.getText().trim();
                double prem = calcPremium(type, age, sa, dur);

                if (isEdit) {
                    editing.holderName = name; editing.age = age; editing.type = type;
                    editing.sumAssured = sa;    editing.durationYears = dur;
                    editing.status = stat;      editing.phone = phn;
                    editing.email = eml;        editing.premium = prem;
                    savePolicies();
                    showToast("Policy updated successfully!", SUCCESS);
                } else {
                    Policy p = new Policy(++policyCounter, name, type, age, sa, prem, stat, phn, eml, dur);
                    policies.add(p);
                    savePolicies();
                    showToast("Policy created! ID: POL-" + p.id, SUCCESS);
                }
                showPanel("Policies");
            } catch (NumberFormatException ex) {
                showToast("Invalid number in Age, Sum Assured, or Duration.", DANGER);
            }
        });
        cancel.addActionListener(e -> showPanel("Policies"));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel); btnRow.add(save);

        gbc.gridy = ++row; gbc.gridx = 0; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.EAST; card.add(btnRow, gbc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrap.add(card, BorderLayout.NORTH);
        root.add(wrap, BorderLayout.CENTER);
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CALCULATOR
    // ═══════════════════════════════════════════════════════════════════
    static JPanel buildCalculatorPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Premium Calculator");
        title.setFont(FONT_TITLE); title.setForeground(TEXT_DARK);
        JLabel sub = new JLabel("Estimate your insurance premium instantly.");
        sub.setFont(FONT_BODY); sub.setForeground(TEXT_MID);
        JPanel hdr = new JPanel();
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setOpaque(false);
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        root.add(hdr, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 22, 0));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(22, 0, 0, 0));

        // Input card
        JPanel inputCard = roundCard();
        inputCard.setLayout(new BoxLayout(inputCard, BoxLayout.Y_AXIS));
        inputCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        addLabel(inputCard, "Policy Type");
        JComboBox<String> cType = styledCombo(POLICY_TYPES);
        cType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        inputCard.add(cType); inputCard.add(Box.createVerticalStrut(14));

        addLabel(inputCard, "Applicant Age");
        JSlider ageSlider = new JSlider(18, 80, 35);
        styleSlider(ageSlider, ACCENT);
        JLabel ageVal = new JLabel("35 years");
        ageVal.setFont(FONT_BODY); ageVal.setForeground(ACCENT);
        ageSlider.addChangeListener(e -> ageVal.setText(ageSlider.getValue() + " years"));
        inputCard.add(ageSlider); inputCard.add(ageVal); inputCard.add(Box.createVerticalStrut(14));

        addLabel(inputCard, "Sum Assured (₹)");
        JSlider saSlider = new JSlider(100000, 10000000, 1000000);
        saSlider.setMajorTickSpacing(2000000);
        styleSlider(saSlider, ACCENT2);
        JLabel saVal = new JLabel("₹10,00,000");
        saVal.setFont(FONT_BODY); saVal.setForeground(ACCENT2);
        saSlider.addChangeListener(e -> saVal.setText("₹" + formatNum(saSlider.getValue())));
        inputCard.add(saSlider); inputCard.add(saVal); inputCard.add(Box.createVerticalStrut(14));

        addLabel(inputCard, "Policy Duration (Years)");
        JSlider durSlider = new JSlider(1, 30, 10);
        styleSlider(durSlider, WARNING);
        JLabel durVal = new JLabel("10 years");
        durVal.setFont(FONT_BODY); durVal.setForeground(WARNING);
        durSlider.addChangeListener(e -> durVal.setText(durSlider.getValue() + " years"));
        inputCard.add(durSlider); inputCard.add(durVal); inputCard.add(Box.createVerticalStrut(20));

        JButton calcBtn = accentButton("Calculate Premium", ACCENT);
        calcBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCard.add(calcBtn);

        center.add(inputCard);

        // Result card
        JPanel resultCard = roundCard();
        resultCard.setLayout(new BorderLayout());
        resultCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel resultTitle = new JLabel("Premium Breakdown");
        resultTitle.setFont(FONT_HEAD); resultTitle.setForeground(TEXT_DARK);

        JPanel breakdown = new JPanel();
        breakdown.setLayout(new BoxLayout(breakdown, BoxLayout.Y_AXIS));
        breakdown.setOpaque(false);

        JLabel[] lineLabels = new JLabel[6];
        String[] lineNames  = {"Base Premium", "Age Loading", "Type Factor", "Duration Discount", "GST (18%)", "Total Annual Premium"};
        for (int i = 0; i < lineLabels.length; i++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(8, 0, 8, 0));
            JLabel nm = new JLabel(lineNames[i]);
            nm.setFont(i == 5 ? FONT_HEAD : FONT_BODY);
            nm.setForeground(i == 5 ? TEXT_DARK : TEXT_MID);
            lineLabels[i] = new JLabel("₹0");
            lineLabels[i].setFont(i == 5 ? FONT_NUM : FONT_BODY);
            lineLabels[i].setForeground(i == 5 ? ACCENT : TEXT_DARK);
            lineLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(nm, BorderLayout.WEST); row.add(lineLabels[i], BorderLayout.EAST);
            if (i == 4) row.add(new JSeparator(), BorderLayout.SOUTH);
            breakdown.add(row);
        }

        resultCard.add(resultTitle, BorderLayout.NORTH);
        resultCard.add(breakdown, BorderLayout.CENTER);

        // Calculate button action
        calcBtn.addActionListener(e -> {
            String type = (String) cType.getSelectedItem();
            int age  = ageSlider.getValue();
            double sa = saSlider.getValue();
            int dur   = durSlider.getValue();

            double base   = sa * 0.005;
            double ageLd  = base * (age > 45 ? 0.3 : age > 35 ? 0.15 : 0.05);
            double typeFc = base * typeMultiplier(type);
            double durDis = base * (dur > 20 ? -0.15 : dur > 10 ? -0.08 : 0);
            double subtot = base + ageLd + typeFc + durDis;
            double gst    = subtot * 0.18;
            double total  = subtot + gst;

            lineLabels[0].setText("₹" + formatNum(base));
            lineLabels[1].setText("₹" + formatNum(ageLd));
            lineLabels[2].setText("₹" + formatNum(typeFc));
            lineLabels[3].setText((durDis < 0 ? "-₹" : "₹") + formatNum(Math.abs(durDis)));
            lineLabels[4].setText("₹" + formatNum(gst));
            lineLabels[5].setText("₹" + formatNum(total));

            animateCountUp(lineLabels[5], total);
        });

        center.add(resultCard);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    static double typeMultiplier(String type) {
        return switch (type) {
            case "Life"    -> 0.10;
            case "Health"  -> 0.20;
            case "Vehicle" -> 0.15;
            case "Home"    -> 0.08;
            case "Travel"  -> 0.05;
            default        -> 0.10;
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    //  REPORTS
    // ═══════════════════════════════════════════════════════════════════
    static JPanel buildReportsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(FONT_TITLE); title.setForeground(TEXT_DARK);
        root.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        // By-type pie chart
        grid.add(buildBarChart());
        // Status distribution
        grid.add(buildStatusCard());
        // Top policies by premium
        grid.add(buildTopPoliciesCard());
        // Summary card
        grid.add(buildSummaryCard());

        root.add(grid, BorderLayout.CENTER);
        return root;
    }

    static JPanel buildBarChart() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel t = new JLabel("Policies by Type"); t.setFont(FONT_HEAD); t.setForeground(TEXT_DARK);
        card.add(t, BorderLayout.NORTH);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (String tp : POLICY_TYPES) counts.put(tp, policies.stream().filter(p -> p.type.equals(tp)).count());
        long max = counts.values().stream().mapToLong(v -> v).max().orElse(1);
        Color[] cols = {ACCENT, ACCENT2, WARNING, DANGER, new Color(0xA855F7)};

        JPanel bars = new JPanel(new GridLayout(POLICY_TYPES.length, 1, 0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int bh = (getHeight() - (POLICY_TYPES.length-1)*6) / POLICY_TYPES.length;
                int ci = 0;
                for (Map.Entry<String,Long> en : counts.entrySet()) {
                    int y = ci * (bh + 6);
                    int w = (int)((double)en.getValue()/max * (getWidth() - 80));
                    g2.setColor(cols[ci % cols.length]);
                    g2.fillRoundRect(60, y+4, Math.max(w, 4), bh-8, 6, 6);
                    g2.setColor(TEXT_MID);
                    g2.setFont(FONT_SMALL);
                    g2.drawString(en.getKey(), 0, y + bh/2 + 4);
                    g2.setColor(TEXT_DARK);
                    g2.drawString(String.valueOf(en.getValue()), 66+w, y + bh/2 + 4);
                    ci++;
                }
                g2.dispose();
            }
        };
        bars.setOpaque(false);
        bars.setBorder(new EmptyBorder(12, 0, 0, 0));
        card.add(bars, BorderLayout.CENTER);
        return card;
    }

    static JPanel buildStatusCard() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel t = new JLabel("Status Distribution"); t.setFont(FONT_HEAD); t.setForeground(TEXT_DARK);
        card.add(t, BorderLayout.NORTH);

        long ac = policies.stream().filter(p->"Active".equals(p.status)).count();
        long ex = policies.stream().filter(p->"Expired".equals(p.status)).count();
        long pe = policies.stream().filter(p->"Pending".equals(p.status)).count();
        long total = policies.size();

        JPanel donut = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth()-sz)/2, y = (getHeight()-sz)/2;
                double[] vals = {ac, ex, pe};
                Color[] cs    = {SUCCESS, DANGER, WARNING};
                double start  = -90;
                for (int i = 0; i < 3; i++) {
                    double arc = total > 0 ? 360.0 * vals[i] / total : 120;
                    g2.setColor(cs[i]);
                    g2.fill(new Arc2D.Double(x, y, sz, sz, start, arc, Arc2D.PIE));
                    start += arc;
                }
                g2.setColor(getBackground().equals(Color.WHITE) ? Color.WHITE : CARD);
                g2.setColor(CARD); int hole = sz/3;
                g2.fillOval(x+sz/2-hole/2, y+sz/2-hole/2, hole, hole);
                g2.setColor(TEXT_DARK); g2.setFont(FONT_HEAD);
                String lbl = String.valueOf(total);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, x+sz/2-fm.stringWidth(lbl)/2, y+sz/2+6);
                g2.dispose();
            }
        };
        donut.setOpaque(false);
        donut.setPreferredSize(new Dimension(120, 120));
        card.add(donut, BorderLayout.CENTER);

        JPanel legend = new JPanel(new GridLayout(1,3));
        legend.setOpaque(false);
        String[] ls = {"Active","Expired","Pending"};
        long[] lv = {ac, ex, pe};
        Color[] lc = {SUCCESS, DANGER, WARNING};
        for (int i=0;i<3;i++) {
            JPanel leg = new JPanel(); leg.setOpaque(false);
            leg.setLayout(new BoxLayout(leg, BoxLayout.Y_AXIS));
            JLabel dot = new JLabel("●"); dot.setForeground(lc[i]); dot.setFont(new Font("SansSerif",Font.PLAIN,18));
            JLabel ln  = new JLabel(ls[i]); ln.setFont(FONT_SMALL); ln.setForeground(TEXT_MID);
            JLabel lnv = new JLabel(String.valueOf(lv[i])); lnv.setFont(FONT_HEAD); lnv.setForeground(TEXT_DARK);
            leg.add(dot); leg.add(lnv); leg.add(ln);
            legend.add(leg);
        }
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    static JPanel buildTopPoliciesCard() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel t = new JLabel("Top 5 by Premium"); t.setFont(FONT_HEAD); t.setForeground(TEXT_DARK);
        card.add(t, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(10, 0, 0, 0));
        policies.stream().sorted(Comparator.comparingDouble((Policy p)->p.premium).reversed()).limit(5).forEach(p -> {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(4, 0, 4, 0));
            JLabel nm = new JLabel(p.holderName + "  "); nm.setFont(FONT_BODY); nm.setForeground(TEXT_DARK);
            JLabel pr = new JLabel("₹" + formatNum(p.premium)); pr.setFont(FONT_BODY); pr.setForeground(ACCENT);
            row.add(nm, BorderLayout.WEST); row.add(pr, BorderLayout.EAST);
            list.add(row);
            JSeparator sep = new JSeparator(); sep.setForeground(new Color(0xEEF0F8)); list.add(sep);
        });
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    static JPanel buildSummaryCard() {
        JPanel card = roundCard();
        card.setLayout(new GridLayout(4, 1, 0, 0));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        double totalPrem = policies.stream().filter(p->"Active".equals(p.status)).mapToDouble(v->v.premium).sum();
        double avgPrem   = policies.isEmpty() ? 0 : policies.stream().mapToDouble(v->v.premium).average().orElse(0);
        double totalSA   = policies.stream().filter(p->"Active".equals(p.status)).mapToDouble(v->v.sumAssured).sum();

        summaryRow(card, "Total Active Premium", "₹" + formatNum(totalPrem), ACCENT);
        summaryRow(card, "Avg Policy Premium",   "₹" + formatNum(avgPrem),   ACCENT2);
        summaryRow(card, "Total Sum Assured",     "₹" + formatNum(totalSA),   WARNING);
        summaryRow(card, "Total Policies",        String.valueOf(policies.size()), TEXT_DARK);
        return card;
    }

    static void summaryRow(JPanel p, String label, String value, Color c) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false); row.setBorder(new EmptyBorder(6,0,6,0));
        JLabel lbl = new JLabel(label); lbl.setFont(FONT_BODY); lbl.setForeground(TEXT_MID);
        JLabel val = new JLabel(value); val.setFont(FONT_HEAD); val.setForeground(c);
        row.add(lbl, BorderLayout.WEST); row.add(val, BorderLayout.EAST);
        p.add(row);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PREMIUM CALCULATION LOGIC
    // ═══════════════════════════════════════════════════════════════════
    static double calcPremium(String type, int age, double sa, int durationYears) {
        double base     = sa * 0.005;
        double ageFactor = age > 55 ? 1.40 : age > 45 ? 1.25 : age > 35 ? 1.10 : 1.0;
        double typeMult  = 1.0 + typeMultiplier(type);
        double durDisc   = durationYears > 20 ? 0.85 : durationYears > 10 ? 0.92 : 1.0;
        double subtotal  = base * ageFactor * typeMult * durDisc;
        return subtotal * 1.18; // GST
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HELPER UI COMPONENTS
    // ═══════════════════════════════════════════════════════════════════
    static JPanel roundCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0xDDE3F5));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
    }

    static JTextField roundField(String placeholder, int w) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF4F6FF));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(isFocusOwner() ? ACCENT : new Color(0xCDD5EE));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(6, 10, 6, 10));
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_DARK);
        if (w > 0) tf.setPreferredSize(new Dimension(w, 34));
        // Placeholder
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(TEXT_DARK); } tf.repaint(); }
            public void focusLost(FocusEvent e)   { if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(TEXT_LITE); } tf.repaint(); }
        });
        tf.setText(placeholder); tf.setForeground(TEXT_LITE);
        return tf;
    }

    static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(new Color(0xF4F6FF));
        cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createLineBorder(new Color(0xCDD5EE)));
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }

    static JButton accentButton(String text, Color c) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? c.darker() : getModel().isRollover() ? c.brighter() : c;
                g2.setColor(base);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_MID);
        btn.setBackground(new Color(0xEEF0F8));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCDD5EE), 1, true),
            new EmptyBorder(8, 16, 8, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXT_MID);
        return l;
    }

    static JTable styledTable(String[] cols, Object[][] data) {
        JTable tbl = new JTable(data, cols);
        styleTableComponent(tbl);
        return tbl;
    }

    static JTable styledTable(DefaultTableModel m) {
        JTable tbl = new JTable(m);
        styleTableComponent(tbl);
        return tbl;
    }

    static void styleTableComponent(JTable tbl) {
        tbl.setFont(FONT_BODY);
        tbl.setForeground(TEXT_DARK);
        tbl.setBackground(CARD);
        tbl.setRowHeight(36);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionBackground(new Color(0xEEF2FF));
        tbl.setSelectionForeground(TEXT_DARK);
        tbl.setFillsViewportHeight(true);
        JTableHeader hdr = tbl.getTableHeader();
        hdr.setFont(new Font("SansSerif", Font.BOLD, 12));
        hdr.setBackground(new Color(0xF0F4FF));
        hdr.setForeground(TEXT_MID);
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,2,0,new Color(0xDDE3F5)));
        // Row striping
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) comp.setBackground(r%2==0 ? CARD : new Color(0xF8F9FF));
                setBorder(new EmptyBorder(0,12,0,12));
                // Status coloring
                if (v instanceof String s) {
                    if (s.equals("Active"))  { setForeground(SUCCESS); setFont(new Font("SansSerif",Font.BOLD,12)); }
                    else if (s.equals("Expired")) { setForeground(DANGER); setFont(new Font("SansSerif",Font.BOLD,12)); }
                    else if (s.equals("Pending")) { setForeground(WARNING); setFont(new Font("SansSerif",Font.BOLD,12)); }
                    else { setForeground(TEXT_DARK); setFont(FONT_BODY); }
                }
                return comp;
            }
        });
    }

    static void styleSlider(JSlider sl, Color c) {
        sl.setOpaque(false);
        sl.setForeground(c);
        sl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    static void addLabel(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXT_MID);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ANIMATIONS & TRANSITIONS
    // ═══════════════════════════════════════════════════════════════════
    static void fadeSwitch(JPanel container, JPanel newPanel) {
        // Simple swap with repaint; a real fade needs a timer-based alpha approach
        container.removeAll();
        newPanel.setVisible(false);
        container.add(newPanel, BorderLayout.CENTER);
        container.revalidate();
        Timer timer = new Timer();
        final int[] step = {0};
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (step[0] >= 5) { newPanel.setVisible(true); timer.cancel(); return; }
                step[0]++;
            }
        }, 0, 20);
        newPanel.setVisible(true);
        container.repaint();
    }

    static void animateFadeIn(JFrame frame) {
        try {
            frame.setOpacity(0.0f);
            Timer t = new Timer();
            final float[] op = {0f};
            t.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    op[0] += 0.06f;
                    if (op[0] >= 1f) { frame.setOpacity(1f); t.cancel(); return; }
                    frame.setOpacity(op[0]);
                }
            }, 0, 16);
        } catch (Exception ignored) {}
    }

    static void animateCountUp(JLabel label, double target) {
        Timer t = new Timer();
        final double[] cur = {0};
        final int steps = 25;
        final int[] i = {0};
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                cur[0] = target * (double)i[0] / steps;
                SwingUtilities.invokeLater(() -> label.setText("₹" + formatNum(cur[0])));
                if (++i[0] > steps) { SwingUtilities.invokeLater(() -> label.setText("₹" + formatNum(target))); t.cancel(); }
            }
        }, 0, 30);
    }

    static void showToast(String msg, Color c) {
        JWindow toast = new JWindow();
        JLabel lbl = new JLabel("  " + msg + "  ");
        lbl.setFont(FONT_BODY);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(c);
        lbl.setBorder(new EmptyBorder(10, 16, 10, 16));
        toast.add(lbl);
        toast.pack();
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(scr.width/2 - toast.getWidth()/2, scr.height - 120);
        toast.setVisible(true);
        new Timer().schedule(new TimerTask() { public void run() { toast.dispose(); } }, 2500);
    }

    static void setStatus(String msg) {
        if (statusBar != null) statusBar.setText("  " + msg);
    }

    static String formatNum(double v) {
        return new DecimalFormat("#,##0.00").format(v);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ACTION CELL RENDERER / EDITOR
    // ═══════════════════════════════════════════════════════════════════
    static class ActionCellRenderer implements TableCellRenderer {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        JButton edit = new JButton("Edit"), del = new JButton("Delete");
        ActionCellRenderer() {
            p.setOpaque(true);
            edit.setFont(FONT_SMALL); edit.setForeground(ACCENT); edit.setBackground(new Color(0xEEF2FF));
            edit.setBorder(new EmptyBorder(3,8,3,8)); edit.setFocusPainted(false);
            del.setFont(FONT_SMALL); del.setForeground(DANGER); del.setBackground(new Color(0xFEE2E2));
            del.setBorder(new EmptyBorder(3,8,3,8)); del.setFocusPainted(false);
            p.add(edit); p.add(del);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            p.setBackground(r%2==0 ? CARD : new Color(0xF8F9FF));
            return p;
        }
    }

    static class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        JButton edit = new JButton("Edit"), del = new JButton("Delete");
        int currentRow;
        DefaultTableModel model;
        JTable table;

        ActionCellEditor(DefaultTableModel m, JTable t) {
            this.model = m; this.table = t;
            p.setOpaque(true); p.setBackground(new Color(0xEEF2FF));
            edit.setFont(FONT_SMALL); edit.setForeground(ACCENT); edit.setBackground(new Color(0xEEF2FF));
            edit.setBorder(new EmptyBorder(3,8,3,8)); edit.setFocusPainted(false);
            del.setFont(FONT_SMALL); del.setForeground(DANGER); del.setBackground(new Color(0xFEE2E2));
            del.setBorder(new EmptyBorder(3,8,3,8)); del.setFocusPainted(false);
            p.add(edit); p.add(del);

            edit.addActionListener(e -> {
                fireEditingStopped();
                String idStr = model.getValueAt(currentRow, 0).toString().replace("POL-","");
                int id = Integer.parseInt(idStr);
                Policy pol = policies.stream().filter(x -> x.id == id).findFirst().orElse(null);
                if (pol != null) {
                    JPanel editPanel = buildAddPolicyPanel(pol);
                    fadeSwitch(contentArea, editPanel);
                    navButtons.values().forEach(b -> { b.putClientProperty("active", null); b.repaint(); });
                }
            });

            del.addActionListener(e -> {
                fireEditingStopped();
                String idStr = model.getValueAt(currentRow, 0).toString().replace("POL-","");
                int id = Integer.parseInt(idStr);
                int confirm = JOptionPane.showConfirmDialog(null,
                    "Delete policy POL-" + id + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    policies.removeIf(x -> x.id == id);
                    model.removeRow(currentRow);
                    savePolicies();
                    showToast("Policy deleted.", DANGER);
                }
            });
        }

        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            currentRow = r;
            return p;
        }
        public Object getCellEditorValue() { return ""; }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DATA MODEL
    // ═══════════════════════════════════════════════════════════════════
    static class Policy {
        int id, age, durationYears;
        String holderName, type, status, phone, email;
        double sumAssured, premium;
        Policy(int id, String n, String t, int a, double sa, double p, String st, String ph, String em, int dur) {
            this.id=id; holderName=n; type=t; age=a; sumAssured=sa; premium=p; status=st; phone=ph; email=em; durationYears=dur;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SEED DATA
    // ═══════════════════════════════════════════════════════════════════
    static void seedDemoData() {
        Object[][] d = {
            {"Rahul Sharma",  "Life",    38, 2000000, "Active",  "9876543210", "rahul@email.com",   20},
            {"Priya Verma",   "Health",  29, 500000,  "Active",  "9812345678", "priya@email.com",   5},
            {"Amit Patel",    "Vehicle", 45, 800000,  "Expired", "9898989898", "amit@email.com",    3},
            {"Sunita Rao",    "Home",    52, 3000000, "Active",  "9765432100", "sunita@email.com",  15},
            {"Kiran Desai",   "Travel",  35, 100000,  "Pending", "9700012345", "kiran@email.com",   1},
            {"Mohan Gupta",   "Life",    60, 5000000, "Active",  "9654321098", "mohan@email.com",   10},
            {"Neha Joshi",    "Health",  27, 300000,  "Active",  "9543210987", "neha@email.com",    5},
            {"Rajesh Kumar",  "Vehicle", 40, 600000,  "Expired", "9432109876", "rajesh@email.com",  2},
        };
        for (Object[] r : d) {
            int dur  = (int) r[7];
            double sa = ((Number)r[3]).doubleValue();
            int age  = (int) r[2];
            String type = (String) r[1];
            double prem = calcPremium(type, age, sa, dur);
            policies.add(new Policy(++policyCounter, (String)r[0], type, age, sa, prem, (String)r[4], (String)r[5], (String)r[6], dur));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PERSISTENCE (SAVE/LOAD)
    // ═══════════════════════════════════════════════════════════════════
    static final String POLICIES_FILE = "policies.csv";

    static void savePolicies() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(POLICIES_FILE))) {
            writer.println("id,holderName,type,age,sumAssured,premium,status,phone,email,durationYears");
            for (Policy p : policies) {
                writer.printf("%d,%s,%s,%d,%.2f,%.2f,%s,%s,%s,%d%n",
                    p.id, p.holderName, p.type, p.age, p.sumAssured, p.premium,
                    p.status, p.phone, p.email, p.durationYears);
            }
            if (policyCounter < 1000) policyCounter = 1000;  // Reset counter if needed
        } catch (IOException e) {
            System.err.println("Error saving policies: " + e.getMessage());
        }
    }

    static void loadPolicies() {
        File file = new File(POLICIES_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();  // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 10) continue;
                try {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String type = parts[2];
                    int age = Integer.parseInt(parts[3]);
                    double sumAssured = Double.parseDouble(parts[4]);
                    double premium = Double.parseDouble(parts[5]);
                    String status = parts[6];
                    String phone = parts[7];
                    String email = parts[8];
                    int durationYears = Integer.parseInt(parts[9]);
                    
                    policies.add(new Policy(id, name, type, age, sumAssured, premium, status, phone, email, durationYears));
                    if (id >= policyCounter) policyCounter = id + 1;
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing policy line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading policies: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  FUNCTIONAL INTERFACE HELPER
    // ═══════════════════════════════════════════════════════════════════
    @FunctionalInterface
    interface DocumentListenerHelper extends javax.swing.event.DocumentListener {
        void update();
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }
}
