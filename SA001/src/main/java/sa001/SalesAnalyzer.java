package sa001;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.text.NumberFormat;
import java.util.Locale;

public class SalesAnalyzer {

        // If CSV doesn't contain a category column, derive categories from product name
        // using this map
        private static final Map<String, String> DEFAULT_CATEGORY = Map.of(
                        "Widget", "Electronics",
                        "Gadget", "Electronics",
                        "Gizmo", "Accessories");
        private Map<String, String> categoryMapping = new HashMap<>(DEFAULT_CATEGORY);

        /**
         * Load CSV using Apache Commons CSV. Handles headers and quoted fields
         * robustly.
         */
        public List<SalesRecord> loadCSV(String filePath) throws IOException {
                Path p = Paths.get(filePath);
                // load category mapping file if present
                Path mapFile = Paths.get("data/category-mapping.csv");
                if (Files.exists(mapFile)) {
                        // expected format: product,category
                        try (Stream<String> lines = Files.lines(mapFile)) {
                                categoryMapping = lines
                                                .map(String::trim)
                                                .filter(l -> !l.isEmpty())
                                                .map(l -> l.split(",", 2))
                                                .filter(a -> a.length == 2)
                                                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim(),
                                                                (a, b) -> b, HashMap::new));
                        }
                }

                try (CSVParser parser = CSVParser.parse(p, StandardCharsets.UTF_8,
                                CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                        List<SalesRecord> out = new ArrayList<>();
                        Map<String, Integer> headerMap = parser.getHeaderMap().entrySet().stream()
                                        .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

                        for (CSVRecord rec : parser) {
                                if (rec.size() == 0)
                                        continue;
                                String orderIdStr = getField(rec, headerMap, "orderid", "id");
                                int orderId = safeParseInt(orderIdStr, 0);
                                String dateStr = getField(rec, headerMap, "date", "date");
                                java.time.LocalDate date = null;
                                if (dateStr != null && !dateStr.isEmpty()) {
                                        try {
                                                date = java.time.LocalDate.parse(dateStr);
                                        } catch (Exception e) {
                                                date = null;
                                        }
                                }
                                String product = getField(rec, headerMap, "product", "product");
                                String category = getField(rec, headerMap, "category", null);
                                if (category == null || category.isEmpty())
                                        category = categoryMapping.getOrDefault(product,
                                                        DEFAULT_CATEGORY.getOrDefault(product, "Other"));
                                int quantity = safeParseInt(getField(rec, headerMap, "quantity", "qty"), 0);
                                double price = safeParseDouble(getField(rec, headerMap, "unitprice", "price"), 0.0);
                                String region = getField(rec, headerMap, "region", "region");

                                out.add(new SalesRecord(orderId, date, product, category, quantity, price, region));
                        }
                        return out;
                }
        }

        private String getField(CSVRecord rec, Map<String, Integer> headerMap, String key1, String key2) {
                String val = null;
                if (headerMap.containsKey(key1))
                        val = rec.get(headerMap.get(key1));
                else if (headerMap.containsKey(key2))
                        val = rec.get(headerMap.get(key2));
                return val == null ? null : val.trim();
        }

        private int safeParseInt(String s, int def) {
                if (s == null || s.isEmpty())
                        return def;
                try {
                        return Integer.parseInt(s.trim());
                } catch (Exception e) {
                        return def;
                }
        }

        private double safeParseDouble(String s, double def) {
                if (s == null || s.isEmpty())
                        return def;
                try {
                        return Double.parseDouble(s.trim());
                } catch (Exception e) {
                        return def;
                }
        }

        // Total revenue across all orders
        public double totalRevenue(List<SalesRecord> records) {
                return records.stream()
                                .mapToDouble(SalesRecord::getRevenue)
                                .sum();
        }

        // Total quantity sold per category
        public Map<String, Integer> quantityByCategory(List<SalesRecord> records) {
                return records.stream()
                                .collect(Collectors.groupingBy(
                                                SalesRecord::getCategory,
                                                Collectors.summingInt(SalesRecord::getQuantity)));
        }

        // Revenue per region
        public Map<String, Double> revenueByRegion(List<SalesRecord> records) {
                return records.stream()
                                .collect(Collectors.groupingBy(
                                                SalesRecord::getRegion,
                                                Collectors.summingDouble(SalesRecord::getRevenue)));
        }

        // Monthly revenue keyed by YYYY-MM
        public Map<String, Double> monthlyRevenue(List<SalesRecord> records) {
                return records.stream()
                                .filter(r -> r.getDate() != null)
                                .collect(Collectors.groupingBy(
                                                r -> r.getDate().getYear() + "-"
                                                                + String.format("%02d", r.getDate().getMonthValue()),
                                                Collectors.summingDouble(SalesRecord::getRevenue)));
        }

        // Median order value (by revenue per record)
        public double medianOrderValue(List<SalesRecord> records) {
                double[] vals = records.stream().mapToDouble(SalesRecord::getRevenue).sorted().toArray();
                if (vals.length == 0)
                        return 0.0;
                int mid = vals.length / 2;
                if (vals.length % 2 == 1)
                        return vals[mid];
                return (vals[mid - 1] + vals[mid]) / 2.0;
        }

        // Percent contribution by product (percent of total revenue)
        public Map<String, Double> percentContributionByProduct(List<SalesRecord> records) {
                double total = totalRevenue(records);
                if (total == 0.0)
                        return Collections.emptyMap();
                return records.stream()
                                .collect(Collectors.groupingBy(SalesRecord::getProduct,
                                                Collectors.summingDouble(SalesRecord::getRevenue)))
                                .entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue() / total) * 100.0));
        }

        // Generate a simple ASCII bar chart for the provided map (descending order)
        public List<String> asciiBarChart(Map<String, Double> data, int width) {
                double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                return data.entrySet().stream()
                                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                                .map(e -> {
                                        int bar = max <= 0 ? 0 : (int) Math.round((e.getValue() / max) * width);
                                        return String.format("%-15s | %s %s", e.getKey(), "#".repeat(bar),
                                                        String.format("(%.2f)", e.getValue()));
                                }).collect(Collectors.toList());
        }

        // Best-selling product by aggregated quantity (returns product name)
        public Optional<String> bestSellingProduct(List<SalesRecord> records) {
                return topNProducts(records, 1).stream().findFirst();
        }

        // Return list of top N products (product names) by quantity descending
        public List<String> topNProducts(List<SalesRecord> records, int n) {
                return records.stream()
                                .collect(Collectors.groupingBy(SalesRecord::getProduct,
                                                Collectors.summingInt(SalesRecord::getQuantity)))
                                .entrySet().stream()
                                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                                .limit(n)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
        }

        // Return top N products as product->totalQuantity preserving sort order
        public LinkedHashMap<String, Integer> topNProductsWithCounts(List<SalesRecord> records, int n) {
                return records.stream()
                                .collect(Collectors.groupingBy(SalesRecord::getProduct,
                                                Collectors.summingInt(SalesRecord::getQuantity)))
                                .entrySet().stream()
                                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                                .limit(n)
                                .collect(LinkedHashMap::new,
                                                (m, e) -> m.put(e.getKey(), e.getValue()),
                                                LinkedHashMap::putAll);
        }

        // Average price by category
        public Map<String, Double> avgPriceByCategory(List<SalesRecord> records) {
                return records.stream()
                                .collect(Collectors.groupingBy(
                                                SalesRecord::getCategory,
                                                Collectors.averagingDouble(SalesRecord::getPrice)));
        }

        /*
         * --- Print helper methods: convenience methods that print analysis results to
         * console ---
         */

        public void printMonthlyRevenue(List<SalesRecord> records) {
                NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
                monthlyRevenue(records).entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(e -> System.out.println(e.getKey() + ": " + currency.format(e.getValue())));
        }

        public void printMedianOrderValue(List<SalesRecord> records) {
                NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
                System.out.println("Median Order Value: " + currency.format(medianOrderValue(records)));
        }

        public void printPercentContributionByProduct(List<SalesRecord> records) {
                percentContributionByProduct(records)
                                .forEach((p, pct) -> System.out.println(p + ": " + String.format("%.2f%%", pct)));
        }

        public void printTopNProducts(List<SalesRecord> records, int n) {
                topNProducts(records, n).forEach(p -> System.out.println(p));
        }

        public void printTopNProductsWithCounts(List<SalesRecord> records, int n) {
                topNProductsWithCounts(records, n)
                                .forEach((prod, qty) -> System.out.println(prod + ": " + qty + " units"));
        }

        public void printAsciiBarChart(Map<String, Double> data, int width) {
                asciiBarChart(data, width).forEach(System.out::println);
        }

        public void printAsciiBarChartByRegion(List<SalesRecord> records, int width) {
                printAsciiBarChart(revenueByRegion(records), width);
        }

        public void printAsciiBarChartByProductRevenue(List<SalesRecord> records, int width) {
                Map<String, Double> revenueByProduct = records.stream()
                                .collect(Collectors.groupingBy(SalesRecord::getProduct,
                                                Collectors.summingDouble(SalesRecord::getRevenue)));
                printAsciiBarChart(revenueByProduct, width);
        }

        public void printAvgPriceByCategory(List<SalesRecord> records) {
                NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
                avgPriceByCategory(records).forEach((k, v) -> System.out.println(k + ": " + fmt.format(v)));
        }

        public void printTotalRevenue(List<SalesRecord> records) {
                NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
                System.out.println(fmt.format(totalRevenue(records)));
        }

        public void printQuantityByCategory(List<SalesRecord> records) {
                quantityByCategory(records).forEach((k, v) -> System.out.println(k + ": " + v));
        }

        public void printRevenueByRegion(List<SalesRecord> records) {
                NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
                revenueByRegion(records).forEach((k, v) -> System.out.println(k + ": " + fmt.format(v)));
        }
}
