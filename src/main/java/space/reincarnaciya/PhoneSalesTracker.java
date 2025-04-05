package space.reincarnaciya;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class PhoneSalesTracker {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField phoneInfoField;
    private JTextField purchaseDateField;
    private JTextField saleDateField;
    private JTextField purchasePriceField;
    private JTextField salePriceField;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PhoneSalesTracker().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Учет продаж телефонов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        JTextArea parseTextArea = new JTextArea(5, 40);
        parseTextArea.setLineWrap(true);
        JScrollPane parseScrollPane = new JScrollPane(parseTextArea);

        JButton parseButton = new JButton("Разобрать текст");
        parseButton.addActionListener(e -> parseAndFillFields(parseTextArea.getText()));

        JPanel parsePanel = new JPanel(new BorderLayout());
        parsePanel.add(new JLabel("Вставьте текст с информацией о телефоне:"), BorderLayout.NORTH);
        parsePanel.add(parseScrollPane, BorderLayout.CENTER);
        parsePanel.add(parseButton, BorderLayout.SOUTH);

        String[] columnNames = {
                "Информация о телефоне",
                "Дата покупки",
                "Дата продажи",
                "Цена покупки",
                "Цена продажи",
                "Дней в наличии",
                "Выручка",
                "Заработок в день"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
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
        inputPanel.add(parsePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addButton = new JButton("Добавить запись");
        addButton.addActionListener(this::addRecord);
        buttonPanel.add(addButton);

        JButton importButton = new JButton("Импорт из Excel");
        importButton.addActionListener(this::importFromExcel);
        buttonPanel.add(importButton);

        JButton exportButton = new JButton("Экспорт в Excel");
        exportButton.addActionListener(this::exportToExcel);
        buttonPanel.add(exportButton);

        JButton clearButton = new JButton("Очистить таблицу");
        clearButton.addActionListener(e -> tableModel.setRowCount(0));
        buttonPanel.add(clearButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        table.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                if (column == 7) {
                    setText(String.format("%.2f", (double)value));
                }
                return c;
            }
        });
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
                phoneInfo.append(parsedInfo.get("model"));
            }
            if (parsedInfo.containsKey("memory")) {
                phoneInfo.append(", ").append(parsedInfo.get("memory")).append("GB");
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

            double dailyProfit = 0;
            if (daysBetween > 0) {
                dailyProfit = profit / daysBetween;
            }

            tableModel.addRow(new Object[]{
                    phoneInfo,
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
        } catch (Exception ex) {
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
            Sheet sheet = workbook.getSheetAt(0);

            tableModel.setRowCount(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Object[] rowData = new Object[8];

                for (int j = 0; j < 8; j++) {
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

            JOptionPane.showMessageDialog(frame,
                    "Данные успешно импортированы из " + fileToImport.getName(),
                    "Импорт завершен",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Ошибка при импорте: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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