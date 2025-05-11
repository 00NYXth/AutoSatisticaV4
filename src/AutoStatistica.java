import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//Test GITHUB

public class AutoStatistica extends JFrame {

    private DefaultTableModel tableModel;
    private List<Record> records = new ArrayList<>();
    private final String FILE_NAME = "inregistrari.txt";
    private JPanel chartPanel;
    private JTextField greseliField;
    private JTextField timpField;
    private JButton refreshStatsButton;
    private JButton exportButton;
    private JButton deleteSelectedButton;
    private JButton schimbareTemaButton;
    private JTabbedPane tabbedPane;
    private boolean isDarkTheme = true;

    // Culori pentru tema întunecată
    private final Color DARK_BACKGROUND_COLOR = new Color(32, 32, 32);
    private final Color DARK_TEXT_COLOR = new Color(255, 255, 255);
    private final Color DARK_BUTTON_COLOR = new Color(33, 52, 72); // #213448
    private final Color DARK_TABLE_BACKGROUND = new Color(45, 45, 45);
    private final Color DARK_TABLE_FOREGROUND = Color.WHITE;
    private final Color DARK_TABLE_GRID = new Color(70, 70, 70);
    private final Color DARK_TABLE_HEADER_BG = new Color(60, 60, 60);
    private final Color DARK_TABLE_HEADER_FG = Color.WHITE;

    // Culori pentru tema luminoasă
    private final Color LIGHT_BACKGROUND_COLOR = new Color(240, 240, 240);
    private final Color LIGHT_TEXT_COLOR = new Color(0, 0, 0);
    private final Color LIGHT_BUTTON_COLOR = new Color(100, 150, 200);
    private final Color LIGHT_TABLE_BACKGROUND = Color.WHITE;
    private final Color LIGHT_TABLE_FOREGROUND = Color.BLACK;
    private final Color LIGHT_TABLE_GRID = new Color(200, 200, 200);
    private final Color LIGHT_TABLE_HEADER_BG = new Color(220, 220, 220);
    private final Color LIGHT_TABLE_HEADER_FG = Color.BLACK;

    // Culori comune pentru ambele teme
    private final Color PASSED_COLOR = new Color(0, 255, 0);
    private final Color FAILED_COLOR = new Color(220, 70, 70);

    public AutoStatistica() {
        setUndecorated(true); // Eliminăm bara de titlu standard
        setSize(1100, 700); // Dimensiunea ferestrei mărită
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Aplicăm tema întunecată la nivelul UI-ului
        applyTheme(isDarkTheme);

        // Adăugăm bara de titlu personalizată
        addCustomTitleBar();

        initializeComponents();
        loadFromFile();
        updateStats();

        // Adaugă handler pentru închiderea aplicației cu salvare
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveToFile();
            }
        });
    }

    private void addCustomTitleBar() {
        // Creăm o bară de titlu personalizată
        JPanel titleBar = new JPanel();
        titleBar.setBackground(DARK_BUTTON_COLOR); // Culoarea barei de titlu
        titleBar.setPreferredSize(new Dimension(getWidth(), 30));
        titleBar.setLayout(new BorderLayout());

        // Adăugăm text în bara de titlu
        JLabel titleLabel = new JLabel("AutoStatistivaV4 by NYXth");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Adăugăm butoane de minimizare și închidere
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);

        // Buton de minimizare îmbunătățit
        JButton minimizeButton = new JButton("-");
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 12));
        minimizeButton.setBackground(DARK_BUTTON_COLOR);
        minimizeButton.setForeground(Color.WHITE);
        minimizeButton.setFocusPainted(false);
        minimizeButton.setBorderPainted(false);
        minimizeButton.setPreferredSize(new Dimension(40, 30));
        minimizeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Adăugăm efect de hover pentru butonul de minimizare
        minimizeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minimizeButton.setBackground(new Color(60, 80, 100));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimizeButton.setBackground(DARK_BUTTON_COLOR);
            }
        });

        minimizeButton.addActionListener(e -> setState(JFrame.ICONIFIED));

        // Buton de închidere îmbunătățit
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Arial", Font.BOLD, 8));
        closeButton.setBackground(DARK_BUTTON_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(40, 30));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Adăugăm efect de hover pentru butonul de închidere
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(new Color(220, 70, 70));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(DARK_BUTTON_COLOR);
            }
        });

        closeButton.addActionListener(e -> {
            saveToFile();
            System.exit(0);
        });

        buttonPanel.add(minimizeButton);
        buttonPanel.add(closeButton);
        titleBar.add(buttonPanel, BorderLayout.EAST);

        // Adăugăm funcționalitate de drag pentru a muta fereastra
        MouseAdapter dragAdapter = new MouseAdapter() {
            private int initialX;
            private int initialY;

            @Override
            public void mousePressed(MouseEvent e) {
                initialX = e.getX();
                initialY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(
                        getLocation().x + e.getX() - initialX,
                        getLocation().y + e.getY() - initialY
                );
            }
        };

        titleBar.addMouseListener(dragAdapter);
        titleBar.addMouseMotionListener(dragAdapter);

        // Adăugăm bara de titlu la frame
        add(titleBar, BorderLayout.NORTH);
    }

    private void applyTheme(boolean isDark) {
        Color bgColor = isDark ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR;
        Color textColor = isDark ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR;
        Color buttonColor = isDark ? DARK_BUTTON_COLOR : LIGHT_BUTTON_COLOR;
        Color tableBackground = isDark ? DARK_TABLE_BACKGROUND : LIGHT_TABLE_BACKGROUND;
        Color tableForeground = isDark ? DARK_TABLE_FOREGROUND : LIGHT_TABLE_FOREGROUND;
        Color tableGrid = isDark ? DARK_TABLE_GRID : LIGHT_TABLE_GRID;
        Color tableHeaderBg = isDark ? DARK_TABLE_HEADER_BG : LIGHT_TABLE_HEADER_BG;
        Color tableHeaderFg = isDark ? DARK_TABLE_HEADER_FG : LIGHT_TABLE_HEADER_FG;

        UIManager.put("Panel.background", bgColor);
        UIManager.put("OptionPane.background", bgColor);
        UIManager.put("OptionPane.messageForeground", textColor);
        UIManager.put("TextField.background", isDark ? new Color(50, 50, 50) : Color.WHITE);
        UIManager.put("TextField.foreground", textColor);
        UIManager.put("TextField.caretForeground", textColor);
        UIManager.put("TextArea.background", isDark ? new Color(40, 40, 40) : Color.WHITE);
        UIManager.put("TextArea.foreground", textColor);
        UIManager.put("Button.background", buttonColor);
        UIManager.put("Button.foreground", textColor);
        UIManager.put("Label.foreground", textColor);
        UIManager.put("TabbedPane.background", bgColor);
        UIManager.put("TabbedPane.foreground", textColor);
        UIManager.put("TabbedPane.selected", isDark ? new Color(60, 60, 60) : new Color(220, 220, 220));
        UIManager.put("TabbedPane.contentAreaColor", bgColor);
        UIManager.put("TabbedPane.light", bgColor);
        UIManager.put("TabbedPane.highlight", bgColor);
        UIManager.put("TabbedPane.darkShadow", bgColor);
        UIManager.put("TabbedPane.focus", buttonColor);
        UIManager.put("Table.background", tableBackground);
        UIManager.put("Table.foreground", tableForeground);
        UIManager.put("TableHeader.background", tableHeaderBg);
        UIManager.put("TableHeader.foreground", tableHeaderFg);
        UIManager.put("ScrollPane.background", bgColor);

        // Setăm culori pentru componente specifice
        getContentPane().setBackground(bgColor);
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        schimbareTemaButton.setText(isDarkTheme ? "Schimbă tema (zi)" : "Schimbă tema (noapte)");
        applyTheme(isDarkTheme);

        // Actualizăm componentele vizibile
        SwingUtilities.updateComponentTreeUI(this);
        updateStats();
    }

    private void initializeComponents() {
        // Tabelul cu sortare
        tableModel = new DefaultTableModel(
                new Object[]{"Data", "Calificativ", "Greșeli", "Timp (sec)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabelul nu este editabil
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class;
                if (columnIndex == 3) return Double.class;
                return String.class;
            }
        };

        JTable table = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Color tableBackground = isDarkTheme ? DARK_TABLE_BACKGROUND : LIGHT_TABLE_BACKGROUND;
        Color tableForeground = isDarkTheme ? DARK_TABLE_FOREGROUND : LIGHT_TABLE_FOREGROUND;
        Color tableGrid = isDarkTheme ? DARK_TABLE_GRID : LIGHT_TABLE_GRID;
        Color tableHeaderBg = isDarkTheme ? DARK_TABLE_HEADER_BG : LIGHT_TABLE_HEADER_BG;
        Color tableHeaderFg = isDarkTheme ? DARK_TABLE_HEADER_FG : LIGHT_TABLE_HEADER_FG;

        table.setForeground(tableForeground);
        table.setBackground(tableBackground);
        table.setGridColor(tableGrid);
        table.getTableHeader().setBackground(tableHeaderBg);
        table.getTableHeader().setForeground(tableHeaderFg);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(tableBackground);
        scrollPane.setBorder(BorderFactory.createLineBorder(tableGrid));

        // Panou input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBackground(isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR);

        greseliField = new JTextField(5);
        timpField = new JTextField(5);
        JButton admisBtn = new JButton("Admis");
        JButton respinsBtn = new JButton("Respins");

        admisBtn.setBackground(PASSED_COLOR);
        respinsBtn.setBackground(FAILED_COLOR);
        admisBtn.setForeground(Color.BLACK);
        respinsBtn.setForeground(Color.BLACK);

        // Aplică stiluri personalizate pentru butoane
        styleButton(admisBtn);
        styleButton(respinsBtn);

        //------------------------------------------------------------
        // Butonul de schimbare a temei
        schimbareTemaButton = new JButton("Schimbă tema (zi)");
        styleButton(schimbareTemaButton);
        schimbareTemaButton.addActionListener(e -> toggleTheme());
        //-------------------------------------------------------------

        // Aplică stiluri pentru componente
        JLabel greseliLabel = new JLabel("Greșeli:");
        JLabel timpLabel = new JLabel("Timp (sec):");
        Color textColor = isDarkTheme ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR;
        greseliLabel.setForeground(textColor);
        timpLabel.setForeground(textColor);

        inputPanel.add(greseliLabel);
        inputPanel.add(greseliField);
        inputPanel.add(timpLabel);
        inputPanel.add(timpField);
        inputPanel.add(admisBtn);
        inputPanel.add(respinsBtn);
        inputPanel.add(schimbareTemaButton);

        // Validare input pentru câmpuri numerice
        greseliField.addKeyListener(new NumberOnlyKeyListener());
        timpField.addKeyListener(new NumberOnlyKeyListener(true));

        topPanel.add(inputPanel, BorderLayout.WEST);

        // Panoul de acțiuni
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR);

        refreshStatsButton = new JButton("Actualizează grafice");
        exportButton = new JButton("Exportă CSV");
        deleteSelectedButton = new JButton("Șterge selectat");

        // Aplică stiluri personalizate pentru butoane
        styleButton(refreshStatsButton);
        styleButton(exportButton);
        styleButton(deleteSelectedButton);

        actionPanel.add(refreshStatsButton);
        actionPanel.add(exportButton);
        actionPanel.add(deleteSelectedButton);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.SOUTH); // Am mutat panoul de acțiuni în partea de jos

        // Inițializăm tabbed pane pentru grafice
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR);
        tabbedPane.setForeground(textColor);

        // Panou pentru grafice
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Adăugăm tabelul și graficele în tabbed pane
        tabbedPane.addTab("Tabel", scrollPane);
        tabbedPane.addTab("Grafice", chartPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Event Handlers
        ActionListener addRecord = e -> {
            String calificativ = ((JButton) e.getSource()).getText();
            try {
                if (greseliField.getText().trim().isEmpty() || timpField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Toate câmpurile sunt obligatorii.");
                }

                int greseli = Integer.parseInt(greseliField.getText());
                if (greseli < 0) {
                    throw new IllegalArgumentException("Numărul de greșeli nu poate fi negativ.");
                }
                if (greseli > 2 && calificativ.equals("Admis")) {
                    throw new IllegalArgumentException("Nu se poate acorda calificativul Admis cu mai mult de 2 greșeli.");
                }

                double timp = Double.parseDouble(timpField.getText().replace(',', '.'));
                if (timp <= 0) {
                    throw new IllegalArgumentException("Timpul trebuie să fie pozitiv.");
                }

                String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Record rec = new Record(data, calificativ, greseli, timp);
                records.add(rec);
                tableModel.addRow(new Object[]{rec.data, rec.calificativ, rec.greseli, rec.timp});
                saveToFile();
                updateStats();

                greseliField.setText("");
                timpField.setText("");
                greseliField.requestFocus();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Introduceți valori numerice valide pentru greșeli și timp.",
                        "Eroare de validare", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Eroare de validare", JOptionPane.ERROR_MESSAGE);
            }
        };

        admisBtn.addActionListener(addRecord);
        respinsBtn.addActionListener(addRecord);

        // Adaugă shortcut-uri de tastatură
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke aKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
        KeyStroke rKey = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);

        // Enter pentru Admis când câmpurile sunt completate
        inputPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enterKey, "admis");
        inputPanel.getActionMap().put("admis", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!greseliField.getText().isEmpty() && !timpField.getText().isEmpty()) {
                    admisBtn.doClick();
                }
            }
        });

        // Ctrl+A pentru Admis
        inputPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(aKey, "ctrlA");
        inputPanel.getActionMap().put("ctrlA", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                admisBtn.doClick();
            }
        });

        // Ctrl+R pentru Respins
        inputPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(rKey, "ctrlR");
        inputPanel.getActionMap().put("ctrlR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                respinsBtn.doClick();
            }
        });

        // Handler pentru refresh statistici
        refreshStatsButton.addActionListener(e -> updateStats());

        // Handler pentru exportare
        exportButton.addActionListener(e -> exportCSV());

        // Handler pentru ștergere înregistrare
        deleteSelectedButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                if (JOptionPane.showConfirmDialog(this,
                        "Sigur doriți să ștergeți această înregistrare?",
                        "Confirmare ștergere", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    records.remove(modelRow);
                    tableModel.removeRow(modelRow);
                    saveToFile();
                    updateStats();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selectați o înregistrare pentru a o șterge");
            }
        });

        // La schimbarea tab-ului, actualizăm graficele
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                updateStats();
            }
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(isDarkTheme ? DARK_BUTTON_COLOR : LIGHT_BUTTON_COLOR);
        button.setForeground(isDarkTheme ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        button.setFocusPainted(false);
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Record r : records) {
                writer.println(r.data + "," + r.calificativ + "," + r.greseli + "," + r.timp);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Eroare la salvarea datelor: " + e.getMessage(),
                    "Eroare", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        if (!Files.exists(Path.of(FILE_NAME))) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            tableModel.setRowCount(0);
            records.clear();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    try {
                        Record r = new Record(
                                parts[0],
                                parts[1],
                                Integer.parseInt(parts[2]),
                                Double.parseDouble(parts[3])
                        );
                        records.add(r);
                        tableModel.addRow(new Object[]{r.data, r.calificativ, r.greseli, r.timp});
                    } catch (NumberFormatException e) {
                        System.err.println("Eroare la citirea liniei: " + line);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Eroare la încărcarea datelor: " + e.getMessage(),
                    "Eroare", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateStats() {
        Color textColor = isDarkTheme ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR;
        Color bgColor = isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR;

        if (records.isEmpty()) {
            showNoDataMessage();
            return;
        }

        // Calculăm statisticile
        long totalRecords = records.size();
        long admisCount = records.stream().filter(r -> r.calificativ.equals("Admis")).count();
        long respinsCount = totalRecords - admisCount;
        long zeroGreseli = records.stream().filter(r -> r.greseli == 0).count();
        long oneGreseli = records.stream().filter(r -> r.greseli == 1).count();
        long twoGreseli = records.stream().filter(r -> r.greseli == 2).count();
        long moreGreseli = records.stream().filter(r -> r.greseli > 2).count();
        double avgTime = records.stream().mapToDouble(r -> r.timp).average().orElse(0);
        double avgTimeAdmis = records.stream()
                .filter(r -> r.calificativ.equals("Admis"))
                .mapToDouble(r -> r.timp)
                .average().orElse(0);
        double avgTimeRespins = records.stream()
                .filter(r -> r.calificativ.equals("Respins"))
                .mapToDouble(r -> r.timp)
                .average().orElse(0);

        // Formatare numerică
        DecimalFormat df = new DecimalFormat("0.00");

        // Creăm graficele
        chartPanel.removeAll();

        // Layout pentru grafice
        JPanel graphsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        graphsPanel.setBackground(bgColor);

        // Grafic 1: Distribuția Admis/Respins
        JPanel passFailPanel = createPieChart(
                new String[]{"Admis", "Respins"},
                new long[]{admisCount, respinsCount},
                new Color[]{PASSED_COLOR, FAILED_COLOR},
                "Distribuția calificativelor"
        );

        // Grafic 2: Distribuția greșelilor
        JPanel greseliPanel = createBarChart(
                new String[]{"0 greșeli", "1 greșeală", "2 greșeli", ">2 greșeli"},
                new long[]{zeroGreseli, oneGreseli, twoGreseli, moreGreseli},
                new Color(75, 150, 200),
                "Distribuția greșelilor"
        );

        // Grafic 3: Timpii medii
        JPanel timpiPanel = createBarChart(
                new String[]{"Timp mediu total", "Timp mediu Admis", "Timp mediu Respins"},
                new double[]{avgTime, avgTimeAdmis, avgTimeRespins},
                new Color(200, 150, 75),
                "Timpii medii (secunde)"
        );

        // Grafic 4: Sumarul statisticilor
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(bgColor);
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "Sumar statistici",
                0,
                0,
                null,
                textColor
        ));

        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setBackground(isDarkTheme ? new Color(40, 40, 40) : Color.WHITE);
        summaryArea.setForeground(textColor);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        StringBuilder sb = new StringBuilder();
        sb.append("Total teste: ").append(totalRecords).append("\n\n");
        sb.append("Promovabilitate: ").append(df.format(admisCount * 100.0 / totalRecords)).append("%\n\n");
        sb.append("Rezultate perfecte (0 greșeli): ").append(df.format(zeroGreseli * 100.0 / totalRecords)).append("%\n\n");
        sb.append("Timp mediu total: ").append(df.format(avgTime)).append(" secunde\n\n");
        if (admisCount > 0) {
            sb.append("Timp mediu pentru Admis: ").append(df.format(avgTimeAdmis)).append(" secunde\n\n");
        }
        if (respinsCount > 0) {
            sb.append("Timp mediu pentru Respins: ").append(df.format(avgTimeRespins)).append(" secunde");
        }

        summaryArea.setText(sb.toString());
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        // Adăugăm toate graficele la panel
        graphsPanel.add(passFailPanel);
        graphsPanel.add(greseliPanel);
        graphsPanel.add(timpiPanel);
        graphsPanel.add(summaryPanel);

        chartPanel.add(graphsPanel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void showNoDataMessage() {
        Color textColor = isDarkTheme ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR;
        Color bgColor = isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR;

        chartPanel.removeAll();
        JLabel noDataLabel = new JLabel("Nu există înregistrări pentru a afișa grafice.", JLabel.CENTER);
        noDataLabel.setForeground(textColor);
        noDataLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        chartPanel.add(noDataLabel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private JPanel createPieChart(String[] labels, long[] values, Color[] colors, String title) {
        Color textColor = isDarkTheme ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR;
        Color bgColor = isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR;

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                title,
                0,
                0,
                null,
                textColor
        ));

        // Calculăm suma totală
        long total = 0;
        for (long value : values) {
            total += value;
        }

        // Creăm un panel custom pentru desenarea graficului
        long finalTotal = total;
        JPanel piePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int size = Math.min(width, height) - 40;
                int x = (width - size) / 2;
                int y = (height - size) / 2;
                int startAngle = 0;

                if (finalTotal == 0) {
                    g2d.setColor(Color.GRAY);
                    g2d.fillOval(x, y, size, size);
                    g2d.setColor(textColor);
                    g2d.drawString("Fără date", width / 2 - 30, height / 2);
                    return;
                }

                for (int i = 0; i < values.length; i++) {
                    int arcAngle = (int) Math.round(values[i] * 360.0 / finalTotal);
                    g2d.setColor(colors[i]);
                    g2d.fillArc(x, y, size, size, startAngle, arcAngle);
                    startAngle += arcAngle;
                }

                // Legenda
                int legendX = x + size + 20;
                int legendY = y;
                for (int i = 0; i < labels.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRect(legendX, legendY + i * 25, 18, 18);
                    g2d.setColor(textColor);
                    g2d.drawRect(legendX, legendY + i * 25, 18, 18);
                    g2d.drawString(labels[i] + " (" + values[i] + ")", legendX + 25, legendY + 14 + i * 25);
                }
            }
        };
        piePanel.setPreferredSize(new Dimension(200, 200));
        panel.add(piePanel, BorderLayout.CENTER);
        return panel;
    }

    // Bar chart pentru distribuții și timpi
    private JPanel createBarChart(String[] labels, long[] values, Color barColor, String title) {
        double[] doubleValues = new double[values.length];
        for (int i = 0; i < values.length; i++) doubleValues[i] = values[i];
        return createBarChart(labels, doubleValues, barColor, title);
    }

    private JPanel createBarChart(String[] labels, double[] values, Color barColor, String title) {
        Color textColor = isDarkTheme ? DARK_TEXT_COLOR : LIGHT_TEXT_COLOR;
        Color bgColor = isDarkTheme ? DARK_BACKGROUND_COLOR : LIGHT_BACKGROUND_COLOR;

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                title, 0, 0, null, textColor
        ));

        JPanel barPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int barWidth = Math.max(40, (width - 60) / values.length - 10);
                double max = 0;
                for (double v : values) if (v > max) max = v;
                if (max == 0) max = 1;

                for (int i = 0; i < values.length; i++) {
                    int barHeight = (int) (values[i] * (height - 60) / max);
                    int x = 30 + i * (barWidth + 10);
                    int y = height - barHeight - 30;
                    g2d.setColor(barColor);
                    g2d.fillRect(x, y, barWidth, barHeight);
                    g2d.setColor(textColor);
                    g2d.drawRect(x, y, barWidth, barHeight);
                    g2d.drawString(labels[i], x, height - 10);
                    g2d.drawString(String.valueOf((values[i] % 1 == 0) ? (int) values[i] : String.format("%.2f", values[i])), x, y - 5);
                }
            }
        };
        barPanel.setPreferredSize(new Dimension(200, 200));
        panel.add(barPanel, BorderLayout.CENTER);
        return panel;
    }

    // Export CSV
    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvează ca CSV");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(fileToSave)) {
                pw.println("Data,Calificativ,Greșeli,Timp (sec)");
                for (Record r : records) {
                    pw.println(r.data + "," + r.calificativ + "," + r.greseli + "," + r.timp);
                }
                JOptionPane.showMessageDialog(this, "Export realizat cu succes!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Eroare la export: " + e.getMessage());
            }
        }
    }

    // KeyListener pentru validare numerică
    private static class NumberOnlyKeyListener extends KeyAdapter {
        private final boolean allowDecimal;
        public NumberOnlyKeyListener() { this(false); }
        public NumberOnlyKeyListener(boolean allowDecimal) { this.allowDecimal = allowDecimal; }
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (!Character.isDigit(c) && !(allowDecimal && (c == '.' || c == ','))) {
                e.consume();
            }
        }
    }

    // Structura pentru o înregistrare
    private static class Record {
        String data, calificativ;
        int greseli;
        double timp;
        Record(String data, String calificativ, int greseli, double timp) {
            this.data = data;
            this.calificativ = calificativ;
            this.greseli = greseli;
            this.timp = timp;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AutoStatistica app = new AutoStatistica();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}