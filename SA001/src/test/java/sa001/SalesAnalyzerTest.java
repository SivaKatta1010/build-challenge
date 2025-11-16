package sa001;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class SalesAnalyzerTest {

    private List<SalesRecord> sampleRecords() {
        return Arrays.asList(
                new SalesRecord(1, LocalDate.of(2025,1,5), "Widget", "Electronics", 2, 10.0, "North"),
                new SalesRecord(2, LocalDate.of(2025,1,15), "Gadget", "Electronics", 3, 20.0, "South"),
                new SalesRecord(3, LocalDate.of(2025,2,3), "Gizmo", "Accessories", 5, 5.0, "East"),
                new SalesRecord(4, LocalDate.of(2025,2,20), "Widget", "Electronics", 1, 10.0, "North")
        );
    }

    @Test
    public void testTotalRevenue() {
        SalesAnalyzer a = new SalesAnalyzer();
        double rev = a.totalRevenue(sampleRecords());
        // 2*10 + 3*20 + 5*5 + 1*10 = 20 + 60 + 25 + 10 = 115
        assertEquals(115.0, rev, 0.0001);
    }

    @Test
    public void testQuantityByCategory() {
        SalesAnalyzer a = new SalesAnalyzer();
        Map<String, Integer> qty = a.quantityByCategory(sampleRecords());
        assertEquals(2, qty.size());
        assertEquals(6, (int) qty.get("Electronics"));
        assertEquals(5, (int) qty.get("Accessories"));
    }

    @Test
    public void testRevenueByRegion() {
        SalesAnalyzer a = new SalesAnalyzer();
        Map<String, Double> rev = a.revenueByRegion(sampleRecords());
        assertEquals(3, rev.size());
        assertEquals(30.0, rev.get("North"), 0.0001); // Widget orders: (2+1)*10=30
        assertEquals(60.0, rev.get("South"), 0.0001);
        assertEquals(25.0, rev.get("East"), 0.0001);
    }

    @Test
    public void testBestSellingProduct() {
        SalesAnalyzer a = new SalesAnalyzer();
        Optional<String> best = a.bestSellingProduct(sampleRecords());
        assertTrue(best.isPresent());
    assertEquals("Gizmo", best.get()); // Gizmo quantity 5 > Widget 3 > Gadget 3
    }

    @Test
    public void testAvgPriceByCategory() {
        SalesAnalyzer a = new SalesAnalyzer();
        Map<String, Double> avg = a.avgPriceByCategory(sampleRecords());
        assertEquals(2, avg.size());
        assertEquals((10.0 + 20.0 + 10.0) / 3.0, avg.get("Electronics"), 0.0001);
        assertEquals(5.0, avg.get("Accessories"), 0.0001);
    }

    @Test
    public void testLoadCSV() throws Exception {
        // Create a temp CSV file with header and two rows
        Path tmp = Files.createTempFile("test-sales", ".csv");
        List<String> lines = Arrays.asList(
                "orderId,product,category,quantity,unitPrice,region",
                "2001,Alpha,Tools,2,12.5,North",
                "2002,Beta,Tools,3,7.0,South"
        );
        Files.write(tmp, lines, StandardCharsets.UTF_8);

        SalesAnalyzer a = new SalesAnalyzer();
        List<SalesRecord> recs = a.loadCSV(tmp.toString());
        assertEquals(2, recs.size());
        SalesRecord r0 = recs.get(0);
        assertEquals(2001, r0.getOrderId());
        assertEquals("Alpha", r0.getProduct());
        assertEquals("Tools", r0.getCategory());
        assertEquals(2, r0.getQuantity());
        assertEquals(12.5, r0.getPrice(), 0.0001);
    }

    @Test
    public void testMonthlyRevenue() {
        SalesAnalyzer a = new SalesAnalyzer();
        Map<String, Double> m = a.monthlyRevenue(sampleRecords());
        assertEquals(2, m.size());
        assertEquals(80.0, m.get("2025-01"), 0.0001);
        assertEquals(35.0, m.get("2025-02"), 0.0001);
    }

    @Test
    public void testMedianOrderValue() {
        SalesAnalyzer a = new SalesAnalyzer();
        double med = a.medianOrderValue(sampleRecords());
        assertEquals(22.5, med, 0.0001);
    }

    @Test
    public void testPercentContributionByProduct() {
        SalesAnalyzer a = new SalesAnalyzer();
        Map<String, Double> pct = a.percentContributionByProduct(sampleRecords());
        assertEquals(3, pct.size());
        double totalPct = pct.values().stream().mapToDouble(Double::doubleValue).sum();
        // Percentages should sum to ~100
        assertEquals(100.0, totalPct, 0.0001);
        // Check expected per-product percentages
        assertEquals((30.0 / 115.0) * 100.0, pct.get("Widget"), 0.0001);
        assertEquals((60.0 / 115.0) * 100.0, pct.get("Gadget"), 0.0001);
        assertEquals((25.0 / 115.0) * 100.0, pct.get("Gizmo"), 0.0001);
    }

    @Test
    public void testTopNProductsAndCounts() {
        SalesAnalyzer a = new SalesAnalyzer();
        List<String> top1 = a.topNProducts(sampleRecords(), 1);
        assertEquals(1, top1.size());
        assertEquals("Gizmo", top1.get(0));

        LinkedHashMap<String, Integer> topCounts = a.topNProductsWithCounts(sampleRecords(), 3);
        assertEquals(3, topCounts.size());
        // First entry should be Gizmo with quantity 5
        Map.Entry<String, Integer> first = topCounts.entrySet().iterator().next();
        assertEquals("Gizmo", first.getKey());
        assertEquals(5, (int) first.getValue());
    }

    @Test
    public void testAsciiBarChart() {
        SalesAnalyzer a = new SalesAnalyzer();
        Map<String, Double> revByRegion = a.revenueByRegion(sampleRecords());
        List<String> chart = a.asciiBarChart(revByRegion, 10);
        assertEquals(3, chart.size());
        // Top region should be South (60)
        assertTrue(chart.get(0).contains("South"));
        // Each line should contain a numeric value in parentheses
        for (String line : chart) {
            assertTrue(line.matches(".*\\(\\d+\\.\\d{2}\\).*") || line.matches(".*\\(\\d+\\.\\d+\\).*"));
        }
    }

    @Test
    public void testPrintAnalysisToConsole() {
        SalesAnalyzer a = new SalesAnalyzer();
        List<SalesRecord> recs = sampleRecords();

        System.out.println("\n--- Console Analysis Output (from testPrintAnalysisToConsole) ---");
        System.out.println("Total Revenue: " + a.totalRevenue(recs));

        System.out.println("\nQuantity Sold by Category:");
        a.quantityByCategory(recs).forEach((k,v) -> System.out.println(k + ": " + v));

        System.out.println("\nRevenue by Region:");
        a.revenueByRegion(recs).forEach((k,v) -> System.out.println(k + ": $" + v));

        System.out.println("\nTop 3 Products (by quantity):");
        a.printTopNProductsWithCounts(recs, 3);

        System.out.println("\nMonthly Revenue:");
        a.printMonthlyRevenue(recs);

        System.out.println();
        a.printMedianOrderValue(recs);

        System.out.println("\nPercent Contribution by Product:");
        a.printPercentContributionByProduct(recs);

        System.out.println("\nRevenue Bar Chart (by product):");
        a.printAsciiBarChartByProductRevenue(recs, 30);

        System.out.println("\nAverage Price by Category:");
        a.avgPriceByCategory(recs).forEach((k,v) -> System.out.println(k + ": $" + String.format("%.2f", v)));

        // minimal assertion to satisfy test runner
        assertTrue(true);
    }
}
