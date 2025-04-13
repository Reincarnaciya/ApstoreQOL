package space.reincarnaciya;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhoneSalesTracker {
    private BudgetManager budgetManager;
    private JLabel budgetLabel;
    private JTextArea budgetHistoryArea;

    private final String[] columnNames = {
            "Общие сведения о телефоне",
            "Модель телефона и память",
            "Дата покупки",
            "Дата продажи",
            "Цена покупки",
            "Цена продажи",
            "Дней в наличии",
            "Выручка",
            "Заработок в день"
    };




    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField modelField;
    private JTextField phoneInfoField;
    private JTextField purchaseDateField;
    private JTextField saleDateField;
    private JTextField purchasePriceField;
    private JTextField salePriceField;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    JPanel chartsPanel;
    JPanel tablePanel;
    JTabbedPane tabbedPane;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PhoneSalesTracker().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Учет продаж телефонов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);

        tabbedPane = new JTabbedPane();

        tablePanel = createTablePanel();
        tabbedPane.addTab("Таблица", tablePanel);

        chartsPanel = createChartsPanel();
        tabbedPane.addTab("Графики", chartsPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        budgetManager = new BudgetManager(0); // Начальный бюджет 0
    }
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Верхняя часть - поля ввода и парсера
        JPanel inputPanel = createInputPanel(); // Вынесем создание полей ввода в отдельный метод

        // Центральная часть - таблица
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Нижняя часть - кнопки и бюджет
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Панель кнопок
        JPanel buttonPanel = createButtonPanel(); // Вынесем создание кнопок в отдельный метод
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        // Панель бюджета
        JPanel budgetPanel = createBudgetPanel(); // Вынесем создание бюджета в отдельный метод
        bottomPanel.add(budgetPanel, BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель с полями ввода
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 5, 5));

        fieldsPanel.add(new JLabel("Модель телефона/GB"));
        modelField = new JTextField();
        fieldsPanel.add(modelField);

        fieldsPanel.add(new JLabel("Информация о телефоне:"));
        phoneInfoField = new JTextField();
        fieldsPanel.add(phoneInfoField);

        fieldsPanel.add(new JLabel("Дата покупки (дд.мм.гггг):"));
        purchaseDateField = new JTextField();
        fieldsPanel.add(purchaseDateField);

        fieldsPanel.add(new JLabel("Дата продажи (дд.мм.гггг):"));
        saleDateField = new JTextField();
        fieldsPanel.add(saleDateField);

        fieldsPanel.add(new JLabel("Цена покупки:"));
        purchasePriceField = new JTextField();
        fieldsPanel.add(purchasePriceField);

        fieldsPanel.add(new JLabel("Цена продажи:"));
        salePriceField = new JTextField();
        fieldsPanel.add(salePriceField);

        inputPanel.add(fieldsPanel);
        inputPanel.add(Box.createVerticalStrut(10));

        // Панель парсера текста
        JTextArea parseTextArea = new JTextArea(5, 40);
        parseTextArea.setLineWrap(true);
        JScrollPane parseScrollPane = new JScrollPane(parseTextArea);

        JButton parseButton = new JButton("Разобрать текст");
        parseButton.addActionListener(e -> parseAndFillFields(parseTextArea.getText()));

        JPanel parsePanel = new JPanel(new BorderLayout());
        parsePanel.add(new JLabel("Вставьте текст с информацией о телефоне:"), BorderLayout.NORTH);
        parsePanel.add(parseScrollPane, BorderLayout.CENTER);
        parsePanel.add(parseButton, BorderLayout.SOUTH);

        inputPanel.add(parsePanel);

        return inputPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addButton = new JButton("Добавить запись");
        addButton.addActionListener(this::addRecord);
        buttonPanel.add(addButton);

        JButton deleteButton = new JButton("Удалить запись");
        deleteButton.addActionListener(this::deleteSelectedRecord);
        buttonPanel.add(deleteButton);

        JButton importButton = new JButton("Импорт из Excel");
        importButton.addActionListener(this::importFromExcel);
        buttonPanel.add(importButton);

        JButton exportButton = new JButton("Экспорт в Excel");
        exportButton.addActionListener(this::exportToExcel);
        buttonPanel.add(exportButton);

        JButton clearButton = new JButton("Очистить таблицу");
        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            updateCharts();
        });
        buttonPanel.add(clearButton);

        JButton setBudgetButton = new JButton("Установить начальный бюджет");
        setBudgetButton.addActionListener(e -> setInitialBudget());
        buttonPanel.add(setBudgetButton);

        JButton showHistoryButton = new JButton("История бюджета");
        showHistoryButton.addActionListener(e -> showBudgetHistory());
        buttonPanel.add(showHistoryButton);

        return buttonPanel;
    }

    private JPanel createBudgetPanel() {
        JPanel budgetPanel = new JPanel(new BorderLayout());
        budgetLabel = new JLabel("Текущий бюджет: 0.00 руб.", JLabel.CENTER);
        budgetLabel.setFont(new Font("Arial", Font.BOLD, 16));
        budgetPanel.add(budgetLabel, BorderLayout.NORTH);

        budgetHistoryArea = new JTextArea(5, 40);
        budgetHistoryArea.setEditable(false);
        JScrollPane historyScrollPane = new JScrollPane(budgetHistoryArea);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JLabel("История операций:"), BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        budgetPanel.add(historyPanel, BorderLayout.CENTER);

        return budgetPanel;
    }

    // Новые методы для работы с бюджетом:
    private void setInitialBudget() {
        String input = JOptionPane.showInputDialog(frame,
                "Введите начальный бюджет:",
                budgetManager.getInitialBudget());
        if (input != null && !input.trim().isEmpty()) {
            try {
                double newBudget = Double.parseDouble(input.trim());
                budgetManager = new BudgetManager(newBudget);
                updateBudgetDisplay();
                JOptionPane.showMessageDialog(frame,
                        "Начальный бюджет установлен: " + newBudget + " руб.",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Введите корректную сумму",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateBudgetDisplay() {
        budgetLabel.setText(String.format("Текущий бюджет: %.2f руб.",
                budgetManager.getCurrentBudget()));

        StringBuilder historyText = new StringBuilder();
        for (String transaction : budgetManager.getTransactionHistory()) {
            historyText.append(transaction).append("\n");
        }
        budgetHistoryArea.setText(historyText.toString());
    }

    private void showBudgetHistory() {
        JDialog historyDialog = new JDialog(frame, "История операций с бюджетом", true);
        historyDialog.setSize(500, 400);

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        for (String transaction : budgetManager.getTransactionHistory()) {
            historyArea.append(transaction + "\n");
        }

        historyDialog.add(new JScrollPane(historyArea));
        historyDialog.setLocationRelativeTo(frame);
        historyDialog.setVisible(true);
    }


    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2));

        JFreeChart revenueChart = ChartFactory.createBarChart(
                "Выручка по месяцам",
                "Месяц",
                "Выручка (руб)",
                createRevenueDataset()
        );
        ChartPanel revenueChartPanel = new ChartPanel(revenueChart);
        panel.add(revenueChartPanel);

        JFreeChart modelsChart = ChartFactory.createPieChart(
                "Распределение моделей",
                createModelsDataset(),
                true,
                true,
                false
        );
        ChartPanel modelsChartPanel = new ChartPanel(modelsChart);
        panel.add(modelsChartPanel);

        JFreeChart dailyProfitChart = ChartFactory.createLineChart(
                "Заработок в день",
                "Дата продажи",
                "Заработок (руб/день)",
                createDailyProfitDataset()
        );
        ChartPanel dailyProfitChartPanel = new ChartPanel(dailyProfitChart);
        panel.add(dailyProfitChartPanel);

        return panel;
    }
    private DefaultCategoryDataset createRevenueDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> monthlyRevenue = new HashMap<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String saleDateStr = (String) tableModel.getValueAt(i, 2); // Дата продажи
            LocalDate saleDate = LocalDate.parse(saleDateStr, dateFormatter);
            String monthKey = saleDate.getMonth().toString() + " " + saleDate.getYear();
            double profit = (Double) tableModel.getValueAt(i, 7); // Выручка

            monthlyRevenue.merge(monthKey, profit, Double::sum);
        }

        monthlyRevenue.forEach((month, revenue) -> dataset.addValue(revenue, "Выручка", month));
        return dataset;
    }
    private DefaultPieDataset createModelsDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> modelCounts = new HashMap<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String model =(String) tableModel.getValueAt(i, 1);
            modelCounts.merge(model, 1, Integer::sum);
        }

        modelCounts.forEach(dataset::setValue);
        return dataset;
    }
    private DefaultCategoryDataset createDailyProfitDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String saleDateStr = (String) tableModel.getValueAt(i, 2);
            double dailyProfit = (Double) tableModel.getValueAt(i, 7);
            dataset.addValue(dailyProfit, "Заработок", saleDateStr);
        }

        return dataset;
    }
    private void updateCharts() {
        chartsPanel.removeAll();
        chartsPanel = createChartsPanel();
        tabbedPane.setComponentAt(1, chartsPanel);
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    private void parseAndFillFields(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Введите текст с информацией о телефоне",
                        "Нет данных",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Map<String, String> parsedInfo = PhoneInfoParser.parsePhoneInfo(text);

            phoneInfoField.setVisible(true);
            salePriceField.setVisible(true);

            StringBuilder phoneInfo = new StringBuilder();

            if (parsedInfo.containsKey("model")) {
                String model = parsedInfo.get("model");
                phoneInfo.append(model);
                modelField.setText(model);
            }
            if (parsedInfo.containsKey("memory")) {
                String memory = parsedInfo.get("memory").toLowerCase().contains("gb") ? parsedInfo.get("memory") : parsedInfo.get("memory") + "Gb";
                phoneInfo.append(", ").append(memory);
                modelField.setText(modelField.getText() + ", " + memory);
            }
            if (parsedInfo.containsKey("battery")){
                phoneInfo.append(", ").append(parsedInfo.get("battery"));
            }
            if (parsedInfo.containsKey("color")) {
                phoneInfo.append(", ").append(parsedInfo.get("color"));
            }
            if (parsedInfo.containsKey("condition")){
                phoneInfo.append(", ").append(parsedInfo.get("condition"));
            }
            if (parsedInfo.containsKey("kit")){
                phoneInfo.append(", ").append(parsedInfo.get("kit"));
            }

            phoneInfoField.setText(phoneInfo.toString());
            phoneInfoField.setBackground(Color.GREEN);
            new Timer(1000, e -> phoneInfoField.setBackground(Color.WHITE)).start();



            // Показать уведомление об успешном парсинге
            JOptionPane.showMessageDialog(frame,
                    "Данные успешно извлечены из текста!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Ошибка при разборе текста: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRecord(ActionEvent e) {
        try {
            String phoneInfo = phoneInfoField.getText().trim();
            String purchaseDateStr = purchaseDateField.getText().trim();
            String saleDateStr = saleDateField.getText().trim();
            double purchasePrice = Double.parseDouble(purchasePriceField.getText().trim());
            double salePrice = Double.parseDouble(salePriceField.getText().trim());

            if (phoneInfo.isEmpty() || purchaseDateStr.isEmpty() || saleDateStr.isEmpty()) {
                throw new IllegalArgumentException("Все поля должны быть заполнены");
            }

            LocalDate purchaseDate = LocalDate.parse(purchaseDateStr, dateFormatter);
            LocalDate saleDate = LocalDate.parse(saleDateStr, dateFormatter);

            if (saleDate.isBefore(purchaseDate)) {
                throw new IllegalArgumentException("Дата продажи не может быть раньше даты покупки");
            }

            long daysBetween = ChronoUnit.DAYS.between(purchaseDate, saleDate);
            double profit = salePrice - purchasePrice;

            double dailyProfit;
            if (daysBetween > 0) {
                dailyProfit = profit / daysBetween;
            }else {
                dailyProfit = profit;
            }

            tableModel.addRow(new Object[]{
                    phoneInfo,
                    modelField.getText(),
                    purchaseDate.format(dateFormatter),
                    saleDate.format(dateFormatter),
                    purchasePrice,
                    salePrice,
                    daysBetween,
                    profit,
                    dailyProfit
            });

            phoneInfoField.setText("");
            purchaseDateField.setText("");
            saleDateField.setText("");
            purchasePriceField.setText("");
            salePriceField.setText("");
            updateCharts();

            budgetManager.subtractFunds(purchasePrice, "Покупка телефона " + modelField.getText());
            budgetManager.addFunds(salePrice, "Продажа телефона " + modelField.getText());

            updateBudgetDisplay();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка ввода",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importFromExcel(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите Excel файл для импорта");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel Files", "xlsx"));

        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToImport = fileChooser.getSelectedFile();

        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(fileToImport))) {
            // Импорт данных о продажах
            Sheet sheet = workbook.getSheetAt(0);
            tableModel.setRowCount(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Object[] rowData = new Object[9];
                for (int j = 0; j < 9; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData[j] = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                rowData[j] = cell.getLocalDateTimeCellValue().toLocalDate()
                                        .format(dateFormatter);
                            } else {
                                rowData[j] = cell.getNumericCellValue();
                            }
                            break;
                        case BLANK:
                            rowData[j] = "";
                            break;
                        default:
                            rowData[j] = cell.toString();
                    }
                }
                tableModel.addRow(rowData);
            }

            // Импорт бюджета
            Sheet budgetSheet = workbook.getSheet("Бюджет");
            if (budgetSheet != null) {
                Row budgetRow = budgetSheet.getRow(0);
                if (budgetRow != null) {
                    Cell initialBudgetCell = budgetRow.getCell(0);
                    if (initialBudgetCell != null && initialBudgetCell.getCellType() == CellType.NUMERIC) {
                        double initialBudget = initialBudgetCell.getNumericCellValue();

                        // Создаём временный список для истории
                        java.util.List<String> importedHistory = new ArrayList<>();
                        double lastBudgetValue = initialBudget;


                        // Собираем историю из файла (начиная со второй строки)
                        for (int i = 1; i <= budgetSheet.getLastRowNum(); i++) {
                            Row historyRow = budgetSheet.getRow(i);
                            if (historyRow != null) {
                                Cell historyCell = historyRow.getCell(0);
                                if (historyCell != null && historyCell.getCellType() == CellType.STRING) {
                                    String transaction = historyCell.getStringCellValue();
                                    importedHistory.add(transaction);

                                    // Парсим последнее значение бюджета из записи
                                    String[] parts = transaction.split("\\(Текущий бюджет: ");
                                    if (parts.length > 1) {
                                        String budgetPart = parts[1].replace(" руб.)", "").replace(",", ".").trim();
                                        try {
                                            lastBudgetValue = Double.parseDouble(budgetPart);
                                        } catch (NumberFormatException ex) {
                                            // Если не удалось распарсить, используем предыдущее значение
                                        }
                                    }
                                }
                            }
                        }

                        // Создаём BudgetManager с последним актуальным значением бюджета
                        budgetManager = new BudgetManager(lastBudgetValue);
                        // Очищаем автоматически добавленную запись об инициализации
                        budgetManager.getTransactionHistory().clear();
                        // Добавляем все импортированные записи
                        budgetManager.getTransactionHistory().addAll(importedHistory);
                    }
                }
            }

            updateBudgetDisplay();
            JOptionPane.showMessageDialog(frame,
                    "Данные успешно импортированы из " + fileToImport.getName(),
                    "Импорт завершен",
                    JOptionPane.INFORMATION_MESSAGE);
            updateCharts();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Ошибка при импорте: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void deleteSelectedRecord(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame,
                    "Выберите запись для удаления",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.removeRow(selectedRow);
        updateCharts();
    }
    private void exportToExcel(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame,
                    "Нет данных для экспорта",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить как Excel файл");
        fileChooser.setSelectedFile(new File("phone_sales.xlsx"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel Files", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Продажи телефонов");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                headerRow.createCell(i).setCellValue(tableModel.getColumnName(i));
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Row excelRow = sheet.createRow(row + 1);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    if (value != null) {
                        if (value instanceof Number) {
                            excelRow.createCell(col).setCellValue(((Number) value).doubleValue());
                        } else {
                            excelRow.createCell(col).setCellValue(value.toString());
                        }
                    }
                }
            }

            Sheet budgetSheet = workbook.createSheet("Бюджет");
            Row initialBudgetRow = budgetSheet.createRow(0);
            initialBudgetRow.createCell(0).setCellValue(budgetManager.getInitialBudget());

            java.util.List<String> history = budgetManager.getTransactionHistory();
            for (int i = 0; i < history.size(); i++) {
                Row historyRow = budgetSheet.createRow(i + 1);
                historyRow.createCell(0).setCellValue(history.get(i));
            }

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(fileToSave)) {
                workbook.write(outputStream);
            }

            JOptionPane.showMessageDialog(frame,
                    "Данные успешно экспортированы в " + fileToSave.getName(),
                    "Экспорт завершен",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Ошибка при экспорте: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}