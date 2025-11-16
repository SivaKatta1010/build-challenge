# SA-001 — Sales Analysis (Assignment 2)

This Java project demonstrates stream operations, functional programming, aggregation and grouping on CSV sales data.

What I implemented
- Header-aware CSV loader that handles different column names (e.g. `orderId`, `product`, `region`, `quantity`, `unitPrice`).
- Derives `category` when missing using a small product->category map (Widget/Gadget -> Electronics; Gizmo -> Accessories).
- Analysis methods using Java Streams:
  - totalRevenue
  - quantityByCategory
  - revenueByRegion
  - bestSellingProduct (aggregated by total quantity)
  - avgPriceByCategory
- Unit tests for all analysis methods (JUnit 5)

Enhancements
- Robust CSV parsing using Apache Commons CSV (handles quoted fields and headers reliably).
- Configurable category mapping: place `data/category-mapping.csv` (product,category) to override the built-in mapping.
- Additional analyses:
  - monthlyRevenue (YYYY-MM -> revenue)
  - medianOrderValue (median revenue per order)
  - percentContributionByProduct (percent of total revenue by product)
  - ASCII bar chart output for quick visualization in the console

  Print helper methods
  - `SalesAnalyzer` now includes convenience methods that print analysis results directly to the console:
    - `printMonthlyRevenue(List<SalesRecord>)` — prints monthly revenue (YYYY-MM)
    - `printMedianOrderValue(List<SalesRecord>)` — prints median order value
    - `printPercentContributionByProduct(List<SalesRecord>)` — prints percent contribution by product
    - `printTopNProducts(List<SalesRecord>, int)` — prints top N product names
    - `printTopNProductsWithCounts(List<SalesRecord>, int)` — prints top N products with quantities
    - `printAsciiBarChartByProductRevenue(List<SalesRecord>, int)` — prints an ASCII bar chart of revenue by product

  These are available as convenience helpers you can call from your own runner. The test-suite and the included console tests exercise these helpers and show expected output.

  Print helper methods (on `SalesAnalyzer`)
  - `printTotalRevenue(List<SalesRecord>)` — prints total revenue formatted as currency
  - `printQuantityByCategory(List<SalesRecord>)` — prints quantities per category
  - `printRevenueByRegion(List<SalesRecord>)` — prints revenue per region formatted as currency
  - `printTopNProducts(List<SalesRecord>, int)` — prints top N product names
  - `printTopNProductsWithCounts(List<SalesRecord>, int)` — prints top N products with quantities
  - `printMonthlyRevenue(List<SalesRecord>)` — prints YYYY-MM keyed revenue
  - `printMedianOrderValue(List<SalesRecord>)` — prints median order value formatted as currency
  - `printPercentContributionByProduct(List<SalesRecord>)` — prints percent contribution by product
  - `printAsciiBarChartByProductRevenue(List<SalesRecord>, int)` — prints an ASCII bar chart of revenue by product
  - `printAvgPriceByCategory(List<SalesRecord>)` — prints average price per category formatted as currency

Main behavior
- The `sa001.Main` entry point intentionally does not print analysis results to stdout. It still loads the CSV so the application can be used programmatically. This keeps interactive/automated use separate from presentation.
- To see console output, either run the unit tests (they include console-output tests) or call the print helpers from your own runner (example below).

Example: small runner that prints results
```java
package sa001;

import java.util.List;

public class Runner {
    public static void main(String[] args) throws Exception {
        SalesAnalyzer analyzer = new SalesAnalyzer();
        List<SalesRecord> records = analyzer.loadCSV("data/sales.csv");
        analyzer.printTotalRevenue(records);
        analyzer.printQuantityByCategory(records);
        analyzer.printRevenueByRegion(records);
        analyzer.printTopNProductsWithCounts(records, 3);
    }
}
```
You can compile & run this with Maven (example):
```bash
mvn compile exec:java -Dexec.mainClass=sa001.Runner
```

  Example: programmatic printing (replace or call from your own runner)
  ```java
  SalesAnalyzer analyzer = new SalesAnalyzer();
  List<SalesRecord> records = analyzer.loadCSV("data/sales.csv");
  analyzer.printTotalRevenue(records);
  analyzer.printQuantityByCategory(records);
  // etc.
  ```

  If you prefer `Main` to print by default, I can restore the previous behavior — tell me and I'll revert.

Assumptions and dataset choices
- The provided `data/sales.csv` uses headers: `orderId,date,customer,product,region,quantity,unitPrice`.
  The loader reads headers and maps fields accordingly.
- The CSV does not contain a `category` column. I derived category values via a small mapping inside `SalesAnalyzer`.

How to build and run
Prerequisite: Java 11+ and Maven installed.

Build and run tests:

```bash
mvn test
```

Run the program (prints results to console):

```bash
mvn compile exec:java -Dexec.mainClass=sa001.Main
```

Notes
- The code is intentionally small and focuses on stream-based transformations and aggregations.
- If you want a different category mapping or a CSV with explicit categories, update the `data/sales.csv` or the `DEFAULT_CATEGORY` map in `SalesAnalyzer`.

Sample console output (captured from a run using the included `data/sales.csv` and `data/category-mapping.csv`):

=== SALES ANALYSIS REPORT ===
Total Revenue: $1,145.04

Quantity Sold by Category:
Accessories: 9
Electronics: 27

Revenue by Region:
West: $301.72
South: $218.99
North: $438.88
East: $185.45

Top 3 Products (by quantity):
Widget: 21 units
Gizmo: 9 units
Gadget: 6 units

Monthly Revenue:
2025-01: $139.48
2025-02: $142.70
2025-03: $218.99
2025-04: $256.00
2025-05: $299.40
2025-06: $88.47

Median Order Value: $79.73

Percent Contribution by Product:
Gadget: 52.14%
Gizmo: 11.20%
Widget: 36.66%

Revenue Bar Chart (by product):
North           | ############################## (438.88)
West            | ##################### (301.72)
South           | ############### (218.99)
East            | ############# (185.45)

Average Price by Category:
Accessories: $14.25
Electronics: $55.33
