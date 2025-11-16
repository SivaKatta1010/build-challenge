package sa001;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SalesAnalyzerConsoleTest {

    private List<SalesRecord> sampleRecords() {
        return Arrays.asList(
                new SalesRecord(1, LocalDate.of(2025,1,5), "Widget", "Electronics", 2, 10.0, "North"),
                new SalesRecord(2, LocalDate.of(2025,1,15), "Gadget", "Electronics", 3, 20.0, "South"),
                new SalesRecord(3, LocalDate.of(2025,2,3), "Gizmo", "Accessories", 5, 5.0, "East"),
                new SalesRecord(4, LocalDate.of(2025,2,20), "Widget", "Electronics", 1, 10.0, "North")
        );
    }

    @Test
    public void testPrintHelpersProduceConsoleOutput() {
        SalesAnalyzer analyzer = new SalesAnalyzer();
        List<SalesRecord> records = sampleRecords();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        PrintStream old = System.out;
        try {
            System.setOut(ps);

            // invoke a few print helpers
            analyzer.printMonthlyRevenue(records);
            analyzer.printMedianOrderValue(records);
            analyzer.printPercentContributionByProduct(records);
            analyzer.printTopNProductsWithCounts(records, 3);
            analyzer.printAsciiBarChartByProductRevenue(records, 20);

            ps.flush();
            String output = out.toString();

            // Basic sanity checks on produced output
            assertTrue(output.contains("2025-01"), "monthly revenue should include 2025-01");
            assertTrue(output.contains("Median Order Value"), "should print median order value label");
            assertTrue(output.contains("Widget") || output.contains("Gizmo") || output.contains("Gadget"), "should mention product names");
            assertTrue(output.contains("units") || output.contains("("), "should include units or numeric values from bar chart");
        } finally {
            System.setOut(old);
        }
    }
}
