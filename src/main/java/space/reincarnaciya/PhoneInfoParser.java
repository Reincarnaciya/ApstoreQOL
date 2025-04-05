package space.reincarnaciya;

import java.util.*;

public class PhoneInfoParser {

    public static Map<String, String> parsePhoneInfo(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        List<String> lines = Arrays.asList(text.split("\n"));

        parseModel(lines, result);
        parseMemory(lines, result);
        parseColor(lines, result);
        parseBattery(lines, result);
        parseCondition(lines, result);
        parseKit(lines, result);

        return result;
    }

    private static void parseModel(List<String> lines, Map<String, String> result) {
        for (String line : lines) {
            String normalizedLine = line.toLowerCase()
                    .replaceAll("[^a-zа-яё0-9]", "")
                    .replace("iphоnе", "iphone");
            if (normalizedLine.contains("iphone") || normalizedLine.contains("айфон")) {
                String model = line.replaceAll("(?i).*?(iphone|айфон)", "")
                        .replaceAll("[^0-9a-zA-Zа-яА-ЯЁё\\s]", "")
                        .trim()
                        .replaceAll("\\s+", " ");
                if (!model.isEmpty()) {
                    result.put("model", model);
                }
                break;
            }
        }
    }

    private static void parseMemory(List<String> lines, Map<String, String> result) {
        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("память")) {
                String memory = extractValueAfterColon(line)
                        .replaceAll("(?i)[^0-9]|gb|гб", "")
                        .trim();

                if (!memory.isEmpty()) {
                    result.put("memory", memory);
                }
                break;
            }
        }
    }

    private static void parseColor(List<String> lines, Map<String, String> result) {
        for (String line : lines) {
            if (line.toLowerCase().contains("цвет")) {
                String color = extractValueAfterColon(line)
                        .replaceAll("[^а-яёА-ЯЁ\\s-]", "").trim();
                if (!color.isEmpty()) {
                    result.put("color", capitalizeFirstLetter(color));
                }
                break;
            }
        }
    }

    private static void parseBattery(List<String> lines, Map<String, String> result) {
        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if ((lowerLine.contains("емкость") || lowerLine.contains("батарея"))
                    && line.contains("%")) {
                String battery = line.replaceAll(".*?(\\d{1,3})%.*", "$1");
                if (!battery.isEmpty()) {
                    result.put("battery", battery + "%");
                }
                break;
            }
        }
    }

    private static void parseCondition(List<String> lines, Map<String, String> result) {
        for (String line : lines) {
            String normalizedLine = line.replaceAll("^[\\s•\\-]+", "").trim();
            if (normalizedLine.toLowerCase().startsWith("состояние")) {
                String condition = extractValueAfterColon(line)
                        .replaceAll("[^а-яёА-ЯЁ\\s().,-]", "").trim();
                if (!condition.isEmpty()) {
                    result.put("condition", capitalizeFirstLetter(condition));
                }
                break;
            }
        }
    }

    private static void parseKit(List<String> lines, Map<String, String> result) {
        for (String line : lines) {
            String normalizedLine = line.replaceAll("^[\\s•\\-]+", "").trim();
            if (normalizedLine.toLowerCase().startsWith("комплект")) {
                String kit = extractValueAfterColon(line)
                        .replaceAll("[^а-яёА-ЯЁ\\s.,-]", "").trim();
                if (!kit.isEmpty()) {
                    result.put("kit", capitalizeFirstLetter(kit));
                }
                break;
            }
        }
    }

    private static String extractValueAfterColon(String line) {
        return Arrays.stream(line.split("\\s*[:>\\-]\\s*", 2))
                .skip(1)
                .findFirst()
                .map(String::trim)
                .orElse("");
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}