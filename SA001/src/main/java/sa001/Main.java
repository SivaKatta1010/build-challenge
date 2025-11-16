package sa001;

public class Main {
        public static void main(String[] args) throws Exception {
                // Load data so the application can be used programmatically.
                SalesAnalyzer analyzer = new SalesAnalyzer();
                analyzer.loadCSV("data/sales.csv");
        }
}
