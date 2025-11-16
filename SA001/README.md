# SA-001 — Sales Analysis

A small Java project demonstrating CSV parsing, Java Stream-based aggregations, and simple console reporting for sales data.

## Overview

This project loads a CSV of sales records, derives missing category information using a lightweight mapping, and provides analysis utilities such as total revenue, quantity and revenue aggregations, top products, monthly revenue, median order value, and simple console-print helpers.

## Features

- Header-aware CSV loader that maps columns by header name (e.g. `orderId`, `product`, `region`, `quantity`, `unitPrice`).
- Derives `category` when missing using a product→category mapping (e.g. Widget/Gadget → Electronics; Gizmo → Accessories).
- Analysis methods implemented with Java Streams:
  - totalRevenue
  - quantityByCategory
  - revenueByRegion
  - bestSellingProduct (by total quantity)
  - avgPriceByCategory
- Convenience printing helpers in `SalesAnalyzer`:
  - printTotalRevenue(List<SalesRecord>)
  - printQuantityByCategory(List<SalesRecord>)
  - printRevenueByRegion(List<SalesRecord>)
  - printTopNProducts(List<SalesRecord>, int)
  - printTopNProductsWithCounts(List<SalesRecord>, int)
  - printMonthlyRevenue(List<SalesRecord>)
  - printMedianOrderValue(List<SalesRecord>)
  - printPercentContributionByProduct(List<SalesRecord>)
  - printAsciiBarChartByProductRevenue(List<SalesRecord>, int)
  - printAvgPriceByCategory(List<SalesRecord>)

## Project entry points

- `sa001.Main` — a small runner that demonstrates loading the CSV files and printing analysis results.
- Unit tests (JUnit 5) cover the analysis methods and some console-output helpers. See `src/test/java/sa001`.

## Assumptions and data

- The bundled `data/sales.csv` uses headers: `orderId,date,customer,product,region,quantity,unitPrice`.
- There is no `category` column in the sample CSV. Categories are derived from a small mapping inside `SalesAnalyzer`. To change categories, edit `data/category-mapping.csv` or update the `DEFAULT_CATEGORY` map in the code.

## Prerequisites

- Java 11 or newer
- Maven 3.x

## Build and test

Run the unit tests:

```bash
cd "/Users/sivaspc/Documents/My Projects/Intuit/SA001"
mvn test
```

## Run the demo

Run the main demo class (prints results to console):

```bash
cd "/Users/sivaspc/Documents/My Projects/Intuit/SA001"
mvn compile exec:java -Dexec.mainClass=sa001.Main
```

## Notes

- The code emphasizes clarity and use of Java Streams for aggregations. It is intentionally small and easy to read.
- To use your own CSV, replace `data/sales.csv` or provide a file with compatible headers.

## Sample output

The following is an example console output produced by the demo using the included sample data:

```
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
```
