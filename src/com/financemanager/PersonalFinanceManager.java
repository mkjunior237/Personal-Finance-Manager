package com.financemanager;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.security.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PersonalFinanceManager extends JFrame {
    // Modern Color Palette
    private static final Color PRIMARY_COLOR = new Color(30, 136, 229);
    private static final Color PRIMARY_LIGHT = new Color(100, 181, 246);
    private static final Color PRIMARY_DARK = new Color(13, 71, 161);
    private static final Color SECONDARY_COLOR = new Color(255, 152, 0);
    private static final Color SUCCESS_COLOR = new Color(67, 160, 71);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(229, 57, 53);
    private static final Color BACKGROUND = new Color(250, 250, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    private static final Color DIVIDER_COLOR = new Color(224, 224, 224);

    // UI Components
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel currentPageLabel;
    private DatabaseManager dbManager;
    private AuthenticationManager authManager;
    private User currentUser;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PersonalFinanceManager app = new PersonalFinanceManager();
            app.setVisible(true);
        });
    }

    public PersonalFinanceManager() {
        initializeDatabase();
        initializeUI();
        showLoginScreen();
    }

    private void initializeDatabase() {
        dbManager = new DatabaseManager();
        authManager = new AuthenticationManager(dbManager);
    }

    private void initializeUI() {
        setTitle("Personal Finance Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        setContentPane(mainPanel);
    }

    private Image createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 64, 64, PRIMARY_DARK);
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, 64, 64, 16, 16);

        // Draw dollar sign
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2d.getFontMetrics();
        String dollar = "$";
        int x = (64 - fm.stringWidth(dollar)) / 2;
        int y = ((64 - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(dollar, x, y);

        g2d.dispose();
        return image;
    }

    private void showLoginScreen() {
        LoginPanel loginPanel = new LoginPanel();
        animateTransition(() -> {
            mainPanel.removeAll();
            mainPanel.add(loginPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
        });
    }

    private void showMainApplication() {
        mainPanel.removeAll();

        // Create header
        createHeader();

        // Create sidebar
        createSidebar();

        // Create content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add panels
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createTransactionsPanel(), "Transactions");
        contentPanel.add(createBudgetPanel(), "Budget");
        contentPanel.add(createAnalyticsPanel(), "Analytics");
        contentPanel.add(createGoalsPanel(), "Goals");

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Show dashboard by default
        cardLayout.show(contentPanel, "Dashboard");
        currentPageLabel.setText("Dashboard");

        mainPanel.revalidate();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DIVIDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)
        ));
        headerPanel.setPreferredSize(new Dimension(0, 60));

        // Current page title
        currentPageLabel = new JLabel("Dashboard");
        currentPageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        currentPageLabel.setForeground(TEXT_PRIMARY);

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setBackground(CARD_BACKGROUND);

        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(TEXT_PRIMARY);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, DANGER_COLOR, false);
        logoutBtn.addActionListener(e -> logout());

        userPanel.add(userLabel);
        userPanel.add(logoutBtn);

        headerPanel.add(currentPageLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createSidebar() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(CARD_BACKGROUND);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, DIVIDER_COLOR),
                new EmptyBorder(20, 0, 20, 0)
        ));

        // App logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoPanel.setBackground(CARD_BACKGROUND);

        JLabel iconLabel = new JLabel(new ImageIcon(createAppIcon().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        JLabel titleLabel = new JLabel("Finance Manager");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);

        logoPanel.add(iconLabel);
        logoPanel.add(titleLabel);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Navigation items
        String[] navItems = {"Dashboard", "Transactions", "Budget", "Analytics", "Goals"};
        String[] navIcons = {"📊", "💳", "💰", "📈", "🎯"};

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(CARD_BACKGROUND);

        for (int i = 0; i < navItems.length; i++) {
            navPanel.add(createNavButton(navItems[i], navIcons[i]));
            navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        sidebarPanel.add(logoPanel);
        sidebarPanel.add(navPanel);
        sidebarPanel.add(Box.createVerticalGlue());

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
    }

    private JButton createNavButton(String text, String icon) {
        JButton btn = new JButton("<html><div style='text-align:left;padding-left:10px'>" +
                icon + "  " + text + "</div></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(CARD_BACKGROUND);
        btn.setBorder(new EmptyBorder(12, 25, 12, 25));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 40));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(PRIMARY_COLOR);
                btn.setBackground(new Color(236, 239, 241));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getText().contains(">")) { // Not selected
                    btn.setForeground(TEXT_SECONDARY);
                    btn.setBackground(CARD_BACKGROUND);
                }
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, text);
            currentPageLabel.setText(text);
            highlightNavButton(btn);
        });

        return btn;
    }

    private void highlightNavButton(JButton activeBtn) {
        Component sidebarComponent = mainPanel.getComponent(1); // Get the sidebar component
        if (sidebarComponent instanceof Container) {
            Container sidebarContainer = (Container) sidebarComponent;
            Component navComponent = sidebarContainer.getComponent(1); // Get the navigation panel
            if (navComponent instanceof Container) {
                Container navContainer = (Container) navComponent;
                for (Component comp : navContainer.getComponents()) {
                    if (comp instanceof JButton) {
                        JButton btn = (JButton) comp;
                        if (btn == activeBtn) {
                            btn.setForeground(PRIMARY_COLOR);
                            btn.setBackground(new Color(236, 239, 241));
                            btn.setBorder(new CompoundBorder(
                                    new MatteBorder(0, 3, 0, 0, PRIMARY_COLOR),
                                    new EmptyBorder(12, 22, 12, 25)
                            ));
                        } else {
                            btn.setForeground(TEXT_SECONDARY);
                            btn.setBackground(CARD_BACKGROUND);
                            btn.setBorder(new EmptyBorder(12, 25, 12, 25));
                        }
                    }
                }
            }
        }
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        summaryPanel.setBackground(BACKGROUND);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        double totalIncome = dbManager.getTotalIncome(currentUser.getId());
        double totalExpenses = dbManager.getTotalExpenses(currentUser.getId());
        double balance = totalIncome - totalExpenses;
        int transactionCount = dbManager.getTransactionCount(currentUser.getId());

        summaryPanel.add(createSummaryCard("Total Income", totalIncome, "↑", SUCCESS_COLOR));
        summaryPanel.add(createSummaryCard("Total Expenses", totalExpenses, "↓", DANGER_COLOR));
        summaryPanel.add(createSummaryCard("Current Balance", balance,
                balance >= 0 ? "↗" : "↘", balance >= 0 ? SUCCESS_COLOR : DANGER_COLOR));
        summaryPanel.add(createSummaryCard("Transactions", transactionCount, "⇄", PRIMARY_COLOR));

        panel.add(summaryPanel, BorderLayout.NORTH);

        // Charts and recent transactions
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        contentPanel.setBackground(BACKGROUND);

        // Expense breakdown chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BACKGROUND);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Double> expensesByCategory = dbManager.getExpensesByCategory(currentUser.getId());
        expensesByCategory.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Expense Breakdown",
                dataset,
                true, true, false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint(0, PRIMARY_COLOR);
        plot.setSectionPaint(1, PRIMARY_LIGHT);
        plot.setSectionPaint(2, SECONDARY_COLOR);
        plot.setSectionPaint(3, SUCCESS_COLOR);
        plot.setSectionPaint(4, WARNING_COLOR);
        plot.setBackgroundPaint(CARD_BACKGROUND);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null);

        chart.setBackgroundPaint(CARD_BACKGROUND);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        ChartPanel chartComponent = new ChartPanel(chart);
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        // Recent transactions
        JPanel transactionsPanel = new JPanel(new BorderLayout());
        transactionsPanel.setBackground(CARD_BACKGROUND);
        transactionsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel transactionsLabel = new JLabel("Recent Transactions");
        transactionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        transactionsLabel.setForeground(TEXT_PRIMARY);
        transactionsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Date", "Description", "Amount"}, 0
        );

        List<Transaction> recentTransactions = dbManager.getRecentTransactions(currentUser.getId(), 5);
        for (Transaction t : recentTransactions) {
            model.addRow(new Object[]{
                    t.getDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                    t.getDescription(),
                    String.format("%.2f FCFA", t.getAmount())
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        JButton viewAllBtn = new JButton("View All Transactions");
        viewAllBtn.addActionListener(e -> {
            currentPageLabel.setText("Transactions");
            // Find the transactions nav button and highlight it
            Component sidebar = mainPanel.getComponent(1); // Sidebar panel
            if (sidebar instanceof Container) {
                Component navPanel = ((Container)sidebar).getComponent(1); // Navigation panel
                if (navPanel instanceof Container) {
                    Component firstNavButton = ((Container)navPanel).getComponent(0);
                    if (firstNavButton instanceof JButton) {
                        highlightNavButton((JButton)firstNavButton);
                    }
                }
            }
            cardLayout.show(contentPanel, "Transactions");
        });
        styleButton(viewAllBtn, PRIMARY_COLOR, true);

        transactionsPanel.add(transactionsLabel, BorderLayout.NORTH);
        transactionsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        transactionsPanel.add(viewAllBtn, BorderLayout.SOUTH);

        contentPanel.add(chartPanel);
        contentPanel.add(transactionsPanel);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryCard(String title, double value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        iconLabel.setHorizontalAlignment(JLabel.RIGHT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(String.format("%.2f FCFA", value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_BACKGROUND);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(iconLabel, BorderLayout.EAST);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        // Hover animation
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(color, 2),
                        new EmptyBorder(14, 14, 14, 14)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(DIVIDER_COLOR, 1),
                        new EmptyBorder(15, 15, 15, 15)
                ));
            }
        });

        return card;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        // Header with buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Transaction Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);

        JButton addBtn = new JButton("Add Transaction");
        addBtn.addActionListener(e -> showAddTransactionDialog());
        styleButton(addBtn, SUCCESS_COLOR, true);

        JButton filterBtn = new JButton("Filter");
        filterBtn.addActionListener(e -> showFilterDialog());
        styleButton(filterBtn, PRIMARY_COLOR, true);

        buttonPanel.add(filterBtn);
        buttonPanel.add(addBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // Transaction table
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Date", "Description", "Category", "Amount", "Type", "Actions"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // Add action buttons column
        table.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        table.getColumn("Actions").setCellEditor(new ActionButtonEditor(table));

        loadTransactions(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(BACKGROUND);
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                new LineBorder(DIVIDER_COLOR, 1)
        ));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    private void refreshAllData() {
        // Refresh transactions panel
        Component transactionsCard = contentPanel.getComponent(1); // Transactions panel is usually the 2nd card
        if (transactionsCard instanceof Container) {
            Component scrollPane = ((Container)transactionsCard).getComponent(1); // ScrollPane is usually the 2nd component
            if (scrollPane instanceof JScrollPane) {
                JTable transactionsTable = (JTable)((JScrollPane)scrollPane).getViewport().getView();
                loadTransactions((DefaultTableModel)transactionsTable.getModel());
            }
        }

        // Refresh budgets panel
        Component budgetsCard = contentPanel.getComponent(2); // Budgets panel is usually the 3rd card
        if (budgetsCard instanceof Container) {
            Component scrollPane = ((Container)budgetsCard).getComponent(2); // ScrollPane is usually the 3rd component
            if (scrollPane instanceof JScrollPane) {
                JTable budgetsTable = (JTable)((JScrollPane)scrollPane).getViewport().getView();
                loadBudgets((DefaultTableModel)budgetsTable.getModel());
            }
        }
        Component analyticsCard = contentPanel.getComponent(3);
        if (analyticsCard instanceof JTabbedPane) {
            JTabbedPane analyticsTabbedPane = (JTabbedPane)analyticsCard;

            // Refresh expense analysis chart
            Component expenseTab = analyticsTabbedPane.getComponent(0);
            if (expenseTab instanceof ChartPanel) {
                ((ChartPanel)expenseTab).setChart(createExpenseChart());
            }

            // Refresh income vs expense chart
            Component comparisonTab = analyticsTabbedPane.getComponent(1);
            if (comparisonTab instanceof ChartPanel) {
                ((ChartPanel)comparisonTab).setChart(createComparisonChart());
            }

            // Refresh monthly trends chart
            Component trendsTab = analyticsTabbedPane.getComponent(2);
            if (trendsTab instanceof ChartPanel) {
                ((ChartPanel)trendsTab).setChart(createTrendsChart());
            }
        }


        // Refresh dashboard
        contentPanel.remove(0); // Remove old dashboard
        contentPanel.add(createDashboardPanel(), "Dashboard", 0); // Add new dashboard at position 0
        cardLayout.show(contentPanel, currentPageLabel.getText()); // Show current page
    }
    // Add these helper methods to your class:
    private JFreeChart createExpenseChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> expensesByCategory = dbManager.getExpensesByCategory(currentUser.getId());
        expensesByCategory.forEach((category, amount) ->
                dataset.addValue(amount, "Expenses", category));

        return ChartFactory.createBarChart(
                "Expenses by Category (FCFA)",
                "Category",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
    }

    private JFreeChart createComparisonChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(dbManager.getTotalIncome(currentUser.getId()), "Income", "Total");
        dataset.addValue(dbManager.getTotalExpenses(currentUser.getId()), "Expenses", "Total");

        return ChartFactory.createBarChart(
                "Income vs Expenses (FCFA)",
                "",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
    }

    private JFreeChart createTrendsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> monthlyExpenses = dbManager.getMonthlyExpenses(currentUser.getId());
        monthlyExpenses.forEach((month, amount) ->
                dataset.addValue(amount, "Expenses", month));

        return ChartFactory.createLineChart(
                "Monthly Spending Trends (FCFA)",
                "Month",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
    }

    private void loadTransactions(DefaultTableModel model) {
        model.setRowCount(0);
        List<Transaction> transactions = dbManager.getAllTransactions(currentUser.getId());

        for (Transaction t : transactions) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    t.getDescription(),
                    t.getCategory(),
                    String.format("%.2f FCFA", t.getAmount()),
                    t.getType(),
                    "Actions"
            });
        }
    }

    private void showAddTransactionDialog() {
        JDialog dialog = new JDialog(this, "Add Transaction", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Add New Transaction");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);

        // Form fields
        JTextField descField = createStyledTextField();
        JTextField amountField = createStyledTextField();
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
                "Food", "Transportation", "Entertainment", "Utilities", "Healthcare",
                "Shopping", "Education", "Travel", "Investment", "Other"
        });
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});

        styleComboBox(categoryCombo);
        styleComboBox(typeCombo);

        addFormField(dialog, gbc, 1, "Description:", descField);
        addFormField(dialog, gbc, 2, "Amount:", amountField);
        addFormField(dialog, gbc, 3, "Category:", categoryCombo);
        addFormField(dialog, gbc, 4, "Type:", typeCombo);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                String desc = descField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                String category = (String) categoryCombo.getSelectedItem();
                String type = (String) typeCombo.getSelectedItem();

                if (desc.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Description cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Transaction transaction = new Transaction(0, currentUser.getId(), LocalDateTime.now(),
                        desc, category, amount, type);
                if (dbManager.addTransaction(transaction)) {

                    // Get the transactions panel (assuming it's the second card in contentPanel)
                    Component transactionsCard = contentPanel.getComponent(1);
                    if (transactionsCard instanceof Container) {
                        Container transactionsContainer = (Container) transactionsCard;
                        // Get the scroll pane (assuming it's the second component in the transactions panel)
                        Component scrollPane = transactionsContainer.getComponent(1);
                        if (scrollPane instanceof JScrollPane) {
                            JScrollPane jScrollPane = (JScrollPane) scrollPane;
                            // Get the table from the viewport
                            JTable table = (JTable) jScrollPane.getViewport().getView();
                            // Refresh the table model
                            loadTransactions((DefaultTableModel) table.getModel());
                        }
                    }
                    refreshAllData();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(dialog,
                            "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to add transaction", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        styleButton(saveBtn, SUCCESS_COLOR, true);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        styleButton(cancelBtn, DANGER_COLOR, true);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JButton addBudgetBtn = new JButton("Add Budget");
        addBudgetBtn.addActionListener(e -> showAddBudgetDialog());
        styleButton(addBudgetBtn, SUCCESS_COLOR, true);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addBudgetBtn, BorderLayout.EAST);

        // Budget overview
        JPanel overviewPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        overviewPanel.setBackground(BACKGROUND);
        overviewPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Budget status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(CARD_BACKGROUND);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel statusLabel = new JLabel("Budget Status");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        double totalBudget = dbManager.getAllBudgets(currentUser.getId()).stream()
                .mapToDouble(Budget::getAmount).sum();
        double totalSpent = dbManager.getTotalExpenses(currentUser.getId());
        double remaining = totalBudget - totalSpent;
        double percentageUsed = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0;

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int)percentageUsed);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", percentageUsed));
        progressBar.setForeground(percentageUsed > 90 ? DANGER_COLOR :
                percentageUsed > 70 ? WARNING_COLOR : SUCCESS_COLOR);
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBackground(CARD_BACKGROUND);
        statsPanel.add(createStatItem("Total Budget", String.format("%.2f FCFA", totalBudget)));
        statsPanel.add(createStatItem("Total Spent", String.format("%.2f FCFA", totalSpent)));
        statsPanel.add(createStatItem("Remaining", String.format("%.2f FCFA", remaining)));

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(statsPanel, BorderLayout.SOUTH);

        // Budget by category chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BACKGROUND);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> budgetByCategory = new HashMap<>();
        dbManager.getAllBudgets(currentUser.getId()).forEach(b ->
                budgetByCategory.put(b.getCategory(), b.getAmount()));

        Map<String, Double> spentByCategory = dbManager.getExpensesByCategory(currentUser.getId());

        budgetByCategory.forEach((category, budget) -> {
            dataset.addValue(budget, "Budget", category);
            dataset.addValue(spentByCategory.getOrDefault(category, 0.0), "Spent", category);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                "Budget vs Actual",
                "Category",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, PRIMARY_COLOR);
        plot.getRenderer().setSeriesPaint(1, DANGER_COLOR);
        plot.setBackgroundPaint(CARD_BACKGROUND);
        plot.setRangeGridlinePaint(DIVIDER_COLOR);

        chart.setBackgroundPaint(CARD_BACKGROUND);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        ChartPanel chartComponent = new ChartPanel(chart);
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        overviewPanel.add(statusPanel);
        overviewPanel.add(chartPanel);

        // Budget table
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Category", "Budget", "Spent", "Remaining", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // Color status column
        table.getColumn("Status").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                String status = (String) value;
                if (status.equals("On Track")) {
                    c.setForeground(SUCCESS_COLOR);
                } else {
                    c.setForeground(DANGER_COLOR);
                }

                return c;
            }
        });

        loadBudgets(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(BACKGROUND);
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                new LineBorder(DIVIDER_COLOR, 1)
        ));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(overviewPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatItem(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelComponent.setForeground(TEXT_SECONDARY);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueComponent.setForeground(TEXT_PRIMARY);
        valueComponent.setHorizontalAlignment(JLabel.RIGHT);

        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(valueComponent, BorderLayout.EAST);

        return panel;
    }

    private void loadBudgets(DefaultTableModel model) {
        model.setRowCount(0);
        List<Budget> budgets = dbManager.getAllBudgets(currentUser.getId());

        for (Budget budget : budgets) {
            double spent = dbManager.getSpentInCategory(currentUser.getId(), budget.getCategory());
            double remaining = budget.getAmount() - spent;
            String status = remaining >= 0 ? "On Track" : "Over Budget";

            model.addRow(new Object[]{
                    budget.getCategory(),
                    String.format("%.2f FCFA", budget.getAmount()),
                    String.format("%.2f FCFA", spent),
                    String.format("%.2f FCFA", remaining),
                    status
            });
        }
    }

    private void showAddBudgetDialog() {
        JDialog dialog = new JDialog(this, "Add Budget", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Add New Budget");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);

        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
                "Food", "Transportation", "Entertainment", "Utilities", "Healthcare",
                "Shopping", "Education", "Travel", "Other"
        });
        JTextField amountField = createStyledTextField();

        styleComboBox(categoryCombo);

        addFormField(dialog, gbc, 1, "Category:", categoryCombo);
        addFormField(dialog, gbc, 2, "Amount:", amountField);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                String category = (String) categoryCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Budget amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Budget budget = new Budget(0, currentUser.getId(), category, amount);

                if (dbManager.addBudget(budget)) {
                    // Safely get the budget table
                    Component budgetCard = contentPanel.getComponent(2); // Budget panel is 3rd card
                    if (budgetCard instanceof Container) {
                        Container budgetContainer = (Container) budgetCard;
                        Component budgetScrollPane = budgetContainer.getComponent(2); // ScrollPane is 3rd component
                        if (budgetScrollPane instanceof JScrollPane) {
                            JTable budgetTable = (JTable)((JScrollPane)budgetScrollPane).getViewport().getView();
                            loadBudgets((DefaultTableModel) budgetTable.getModel());
                        }
                    }

                    dialog.dispose();
                    JOptionPane.showMessageDialog(dialog,
                            "Budget added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to add budget", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        styleButton(saveBtn, SUCCESS_COLOR, true);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        styleButton(cancelBtn, DANGER_COLOR, true);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        JLabel titleLabel = new JLabel("Financial Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Analytics tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BACKGROUND);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Expense analysis tab
        JPanel expensePanel = new JPanel(new BorderLayout());
        expensePanel.setBackground(BACKGROUND);

        DefaultCategoryDataset expenseDataset = new DefaultCategoryDataset();
        Map<String, Double> expensesByCategory = dbManager.getExpensesByCategory(currentUser.getId());
        expensesByCategory.forEach((category, amount) ->
                expenseDataset.addValue(amount, "Expenses", category));

        JFreeChart expenseChart = ChartFactory.createBarChart(
                "Expenses by Category",
                "Category",
                "Amount",
                expenseDataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot expensePlot = expenseChart.getCategoryPlot();
        expensePlot.getRenderer().setSeriesPaint(0, PRIMARY_COLOR);
        expensePlot.setBackgroundPaint(CARD_BACKGROUND);
        expensePlot.setRangeGridlinePaint(DIVIDER_COLOR);

        expenseChart.setBackgroundPaint(CARD_BACKGROUND);
        expenseChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        ChartPanel expenseChartPanel = new ChartPanel(expenseChart);
        expensePanel.add(expenseChartPanel, BorderLayout.CENTER);

        // Income vs Expense tab
        JPanel comparisonPanel = new JPanel(new BorderLayout());
        comparisonPanel.setBackground(BACKGROUND);

        DefaultCategoryDataset comparisonDataset = new DefaultCategoryDataset();
        comparisonDataset.addValue(dbManager.getTotalIncome(currentUser.getId()), "Income", "Total");
        comparisonDataset.addValue(dbManager.getTotalExpenses(currentUser.getId()), "Expenses", "Total");

        JFreeChart comparisonChart = ChartFactory.createBarChart(
                "Income vs Expenses",
                "",
                "Amount",
                comparisonDataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot comparisonPlot = comparisonChart.getCategoryPlot();
        comparisonPlot.getRenderer().setSeriesPaint(0, SUCCESS_COLOR);
        comparisonPlot.getRenderer().setSeriesPaint(1, DANGER_COLOR);
        comparisonPlot.setBackgroundPaint(CARD_BACKGROUND);
        comparisonPlot.setRangeGridlinePaint(DIVIDER_COLOR);

        comparisonChart.setBackgroundPaint(CARD_BACKGROUND);
        comparisonChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        ChartPanel comparisonChartPanel = new ChartPanel(comparisonChart);
        comparisonPanel.add(comparisonChartPanel, BorderLayout.CENTER);

        // Monthly trends tab
        JPanel trendsPanel = new JPanel(new BorderLayout());
        trendsPanel.setBackground(BACKGROUND);

        DefaultCategoryDataset trendsDataset = new DefaultCategoryDataset();
        Map<String, Double> monthlyExpenses = dbManager.getMonthlyExpenses(currentUser.getId());
        monthlyExpenses.forEach((month, amount) ->
                trendsDataset.addValue(amount, "Expenses", month));

        JFreeChart trendsChart = ChartFactory.createLineChart(
                "Monthly Spending Trends",
                "Month",
                "Amount",
                trendsDataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot trendsPlot = trendsChart.getCategoryPlot();
        trendsPlot.getRenderer().setSeriesPaint(0, PRIMARY_COLOR);
        trendsPlot.setBackgroundPaint(CARD_BACKGROUND);
        trendsPlot.setRangeGridlinePaint(DIVIDER_COLOR);

        trendsChart.setBackgroundPaint(CARD_BACKGROUND);
        trendsChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        ChartPanel trendsChartPanel = new ChartPanel(trendsChart);
        trendsPanel.add(trendsChartPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Expense Analysis", expensePanel);
        tabbedPane.addTab("Income vs Expenses", comparisonPanel);
        tabbedPane.addTab("Monthly Trends", trendsPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        JLabel titleLabel = new JLabel("Financial Goals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Placeholder content
        JLabel comingSoon = new JLabel("Goals feature coming soon!", JLabel.CENTER);
        comingSoon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comingSoon.setForeground(TEXT_SECONDARY);
        panel.add(comingSoon, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_BACKGROUND);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(CARD_BACKGROUND);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER_COLOR));
        header.setPreferredSize(new Dimension(0, 40));

        // Center align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(CARD_BACKGROUND);
        centerRenderer.setForeground(TEXT_PRIMARY);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(CARD_BACKGROUND);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(CARD_BACKGROUND);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setCaretColor(TEXT_PRIMARY);
        return field;
    }

    private void styleButton(JButton btn, Color bgColor, boolean withHover) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (withHover) {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(bgColor.brighter());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(bgColor);
                }
            });
        }
    }

    private void addFormField(Container container, GridBagConstraints gbc, int row, String label, JComponent field) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelComponent.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        container.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        container.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    private void animateTransition(Runnable action) {
        Timer timer = new Timer(20, null);
        timer.addActionListener(new ActionListener() {
            float opacity = 1.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f;
                if (opacity <= 0) {
                    timer.stop();
                    action.run();
                } else {
                    mainPanel.setBackground(new Color(
                            BACKGROUND.getRed(),
                            BACKGROUND.getGreen(),
                            BACKGROUND.getBlue(),
                            (int)(opacity * 255)
                    ));
                    mainPanel.repaint();
                }
            }
        });
        timer.start();
    }

    private void logout() {
        currentUser = null;
        animateTransition(this::showLoginScreen);
    }

    // Inner classes
    private class LoginPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginPanel() {
            setBackground(BACKGROUND);
            setLayout(new GridBagLayout());
            createLoginUI();
        }

        private void createLoginUI() {
            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(CARD_BACKGROUND);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 1),
                    new EmptyBorder(30, 30, 30, 30)
            ));
            card.setPreferredSize(new Dimension(400, 500));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 20, 15, 20);
            gbc.anchor = GridBagConstraints.CENTER;

            // App icon
            JLabel iconLabel = new JLabel(new ImageIcon(createAppIcon().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = 2;
            card.add(iconLabel, gbc);

            // Title
            JLabel titleLabel = new JLabel("Personal Finance Manager");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(PRIMARY_COLOR);
            gbc.gridy = 1;
            gbc.insets = new Insets(5, 20, 25, 20);
            card.add(titleLabel, gbc);

            // Reset insets
            gbc.insets = new Insets(10, 20, 10, 20);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = 1;

            // Username field
            gbc.gridy = 2;
            card.add(new JLabel("Username:"), gbc);
            usernameField = createStyledTextField();
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            card.add(usernameField, gbc);

            // Password field
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridy = 4;
            card.add(new JLabel("Password:"), gbc);
            passwordField = new JPasswordField(20);
            styleTextField(passwordField);
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            card.add(passwordField, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonPanel.setBackground(CARD_BACKGROUND);

            JButton loginBtn = new JButton("Login");
            loginBtn.addActionListener(this::handleLogin);
            styleButton(loginBtn, SUCCESS_COLOR, true);

            JButton signupBtn = new JButton("Sign Up");
            signupBtn.addActionListener(this::handleSignup);
            styleButton(signupBtn, PRIMARY_COLOR, true);

            buttonPanel.add(loginBtn);
            buttonPanel.add(signupBtn);

            gbc.gridy = 6;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(20, 20, 15, 20);
            card.add(buttonPanel, gbc);

            add(card);
        }

        private void handleLogin(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = authManager.authenticate(username, password);
            if (user != null) {
                currentUser = user;
                showMainApplication();
            } else {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        }

        private void handleSignup(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (authManager.createUser(username, password)) {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Account created successfully! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void styleTextField(JTextField field) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBackground(CARD_BACKGROUND);
            field.setForeground(TEXT_PRIMARY);
            field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(DIVIDER_COLOR, 1),
                    new EmptyBorder(8, 12, 8, 12)
            ));
        }
    }

    private static class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editBtn;
        private final JButton deleteBtn;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setBackground(CARD_BACKGROUND);

            editBtn = new JButton("Edit");
            deleteBtn = new JButton("Delete");

            editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            editBtn.setForeground(Color.WHITE);
            deleteBtn.setForeground(Color.WHITE);

            editBtn.setBackground(WARNING_COLOR);
            deleteBtn.setBackground(DANGER_COLOR);

            editBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
            deleteBtn.setBorder(new EmptyBorder(5, 10, 5, 10));

            editBtn.setFocusPainted(false);
            deleteBtn.setFocusPainted(false);

            add(editBtn);
            add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private int currentRow;
        private final JTable table;

        public ActionButtonEditor(JTable table) {
            super(new JCheckBox());
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setBackground(CARD_BACKGROUND);

            JButton editBtn = new JButton("Edit");
            JButton deleteBtn = new JButton("Delete");

            editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            editBtn.setForeground(Color.WHITE);
            deleteBtn.setForeground(Color.WHITE);

            editBtn.setBackground(WARNING_COLOR);
            deleteBtn.setBackground(DANGER_COLOR);

            editBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
            deleteBtn.setBorder(new EmptyBorder(5, 10, 5, 10));

            editBtn.setFocusPainted(false);
            deleteBtn.setFocusPainted(false);

            editBtn.addActionListener(e -> editTransaction());
            deleteBtn.addActionListener(e -> deleteTransaction());

            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        private void editTransaction() {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int transactionId = (Integer) model.getValueAt(currentRow, 0);

            Transaction transaction = dbManager.getTransactionById(transactionId);
            if (transaction == null) {
                JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                        "Transaction not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(PersonalFinanceManager.this, "Edit Transaction", true);
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(PersonalFinanceManager.this);
            dialog.setLayout(new GridBagLayout());
            dialog.getContentPane().setBackground(BACKGROUND);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            JLabel titleLabel = new JLabel("Edit Transaction");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(TEXT_PRIMARY);
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = 2;
            dialog.add(titleLabel, gbc);

            // Form fields
            JTextField descField = createStyledTextField();
            descField.setText(transaction.getDescription());

            JTextField amountField = createStyledTextField();
            amountField.setText(String.format("%.2f", transaction.getAmount()));

            JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
                    "Food", "Transportation", "Entertainment", "Utilities", "Healthcare",
                    "Shopping", "Education", "Travel", "Investment", "Other"
            });
            categoryCombo.setSelectedItem(transaction.getCategory());

            JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
            typeCombo.setSelectedItem(transaction.getType());

            styleComboBox(categoryCombo);
            styleComboBox(typeCombo);

            addFormField(dialog, gbc, 1, "Description:", descField);
            addFormField(dialog, gbc, 2, "Amount:", amountField);
            addFormField(dialog, gbc, 3, "Category:", categoryCombo);
            addFormField(dialog, gbc, 4, "Type:", typeCombo);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonPanel.setBackground(BACKGROUND);

            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                try {
                    String desc = descField.getText().trim();
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String category = (String) categoryCombo.getSelectedItem();
                    String type = (String) typeCombo.getSelectedItem();

                    if (desc.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog,
                                "Description cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Transaction updatedTransaction = new Transaction(
                            transaction.getId(),
                            transaction.getUserId(),
                            transaction.getDate(),
                            desc,
                            category,
                            amount,
                            type
                    );

                    if (dbManager.updateTransaction(updatedTransaction)) {
                        model.setValueAt(desc, currentRow, 2);
                        model.setValueAt(category, currentRow, 3);
                        model.setValueAt(String.format("%.2f FCFA", amount), currentRow, 4);
                        model.setValueAt(type, currentRow, 5);
                        refreshAllData();
                        dialog.dispose();
                        JOptionPane.showMessageDialog(dialog,
                                "Transaction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Failed to update transaction", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            styleButton(saveBtn, SUCCESS_COLOR, true);

            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(e -> dialog.dispose());
            styleButton(cancelBtn, DANGER_COLOR, true);

            buttonPanel.add(cancelBtn);
            buttonPanel.add(saveBtn);

            gbc.gridx = 0; gbc.gridy = 5;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;
            dialog.add(buttonPanel, gbc);

            dialog.setVisible(true);
            stopCellEditing();
        }

        private void deleteTransaction() {
            stopCellEditing();
            int result = JOptionPane.showConfirmDialog(PersonalFinanceManager.this,
                    "Are you sure you want to delete this transaction?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int transactionId = (Integer) model.getValueAt(currentRow, 0);

                if (dbManager.deleteTransaction(transactionId)) {
                    model.removeRow(currentRow);
                    refreshAllData();
                    JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                            "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(PersonalFinanceManager.this,
                            "Failed to delete transaction", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    private void showFilterDialog() {
        JDialog dialog = new JDialog(this, "Filter Transactions", true);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Filter Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);

        // Filter options
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
                "All", "Food", "Transportation", "Entertainment", "Utilities",
                "Healthcare", "Shopping", "Education", "Travel", "Investment", "Other"
        });

        styleComboBox(typeCombo);
        styleComboBox(categoryCombo);

        addFormField(dialog, gbc, 1, "Type:", typeCombo);
        addFormField(dialog, gbc, 2, "Category:", categoryCombo);

        // Date range
        JPanel datePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        datePanel.setBackground(BACKGROUND);

        JTextField startDateField = createStyledTextField();
        startDateField.setText("Start Date");
        JTextField endDateField = createStyledTextField();
        endDateField.setText("End Date");

        datePanel.add(startDateField);
        datePanel.add(endDateField);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(new JLabel("Date Range:"), gbc);
        gbc.gridy = 4;
        dialog.add(datePanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);

        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> {
            // Apply filter logic here
            dialog.dispose();
        });
        styleButton(applyBtn, PRIMARY_COLOR, true);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        styleButton(cancelBtn, DANGER_COLOR, true);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(applyBtn);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }
}

class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:finance_manager.db";
    private Connection connection;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String createTransactions = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "date TIMESTAMP NOT NULL, " +
                "description TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "type TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (id))";

        String createBudgets = "CREATE TABLE IF NOT EXISTS budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "category TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (id), " +
                "UNIQUE(user_id, category))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createTransactions);
            stmt.execute(createBudgets);
        }
    }

    public boolean createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (user_id, date, description, category, amount, type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getUserId());
            pstmt.setString(2, transaction.getDate().toString());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getCategory());
            pstmt.setDouble(5, transaction.getAmount());
            pstmt.setString(6, transaction.getType());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET description = ?, category = ?, amount = ?, type = ? " +
                "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getDescription());
            pstmt.setString(2, transaction.getCategory());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getType());
            pstmt.setInt(5, transaction.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Transaction getTransactionById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        LocalDateTime.parse(rs.getString("date")),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("type")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Transaction> getAllTransactions(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        LocalDateTime.parse(rs.getString("date")),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getRecentTransactions(int userId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        LocalDateTime.parse(rs.getString("date")),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public boolean deleteTransaction(int transactionId) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addBudget(Budget budget) {
        String sql = "INSERT OR REPLACE INTO budgets (user_id, category, amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, budget.getUserId());
            pstmt.setString(2, budget.getCategory());
            pstmt.setDouble(3, budget.getAmount());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Budget> getAllBudgets(int userId) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                budgets.add(new Budget(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("category"),
                        rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return budgets;
    }

    public double getTotalIncome(int userId) {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Income'";
        return getSumFromQuery(sql, userId);
    }

    public double getTotalExpenses(int userId) {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Expense'";
        return getSumFromQuery(sql, userId);
    }

    public int getTransactionCount(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getSpentInCategory(int userId, String category) {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND category = ? AND type = 'Expense'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Map<String, Double> getExpensesByCategory(int userId) {
        Map<String, Double> categoryExpenses = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(amount) as total FROM transactions " +
                "WHERE user_id = ? AND type = 'Expense' GROUP BY category";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categoryExpenses.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryExpenses;
    }
    public Map<String, Double> getMonthlyExpenses(int userId) {
        Map<String, Double> monthlyExpenses = new LinkedHashMap<>();
        String sql = "SELECT strftime('%Y-%m', date) as month, SUM(amount) as total " +
                "FROM transactions WHERE user_id = ? AND type = 'Expense' " +
                "GROUP BY month ORDER BY month";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                monthlyExpenses.put(rs.getString("month"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlyExpenses;
    }

    private double getSumFromQuery(String sql, int userId) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

class AuthenticationManager {
    private DatabaseManager dbManager;

    public AuthenticationManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public User authenticate(String username, String password) {
        User user = dbManager.getUser(username);
        if (user != null && verifyPassword(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public boolean createUser(String username, String password) {
        if (dbManager.getUser(username) != null) {
            return false; // Username already exists
        }
        String passwordHash = hashPassword(password);
        return dbManager.createUser(username, passwordHash);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        return hashPassword(password).equals(storedHash);
    }
}

class User {
    private int id;
    private String username;
    private String passwordHash;
    public User(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}

class Transaction {
    private int id;
    private int userId;
    private LocalDateTime date;
    private String description;
    private String category;
    private double amount;
    private String type;
    public Transaction(int id, int userId, LocalDateTime date, String description,
                       String category, double amount, String type) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }
}

class Budget {
    private int id;
    private int userId;
    private String category;
    private double amount;
    public Budget(int id, int userId, String category, double amount) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        PersonalFinanceManager app = new PersonalFinanceManager();
        app.setVisible(true);
    });
}
}