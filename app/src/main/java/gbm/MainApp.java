package gbm;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F5F5F5;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));
        formGrid.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-width: 1px;");

        Label pathNumberLabel = new Label("Number of Paths:");
        Label samplePerPathLabel = new Label("Sample per Path:");
        Label initialPriceLabel = new Label("Initial Price:");
        Label driftLabel = new Label("Drift:");
        Label volatilityLabel = new Label("Volatility:");
        Label timeLengthLabel = new Label("Time Length:");
        TextField numPaths = new TextField();
        TextField samplePerPath = new TextField();
        TextField initialPrice = new TextField();
        TextField drift = new TextField();
        TextField volatility = new TextField();
        TextField timeLength = new TextField();
        Button start = new Button("Start Simulation");

        formGrid.addRow(0, pathNumberLabel, numPaths);
        formGrid.addRow(1, samplePerPathLabel, samplePerPath);
        formGrid.addRow(2, initialPriceLabel, initialPrice);
        formGrid.addRow(3, driftLabel, drift);
        formGrid.addRow(4, volatilityLabel, volatility);
        formGrid.addRow(5, timeLengthLabel, timeLength);
        formGrid.addRow(6, start);

        StackPane graphPane = new StackPane();
        graphPane.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-width: 1px;");

        root.setLeft(formGrid);
        root.setCenter(graphPane);
        Scene scene = new Scene(root, 1000, 800);

        stage.setTitle("Geometric Brownian Motion Simulator");
        stage.setScene(scene);
        stage.show();

        start.setOnAction(event -> {
            // Récupération des valeurs des TextField
            int paths = Integer.parseInt(numPaths.getText());
            int samples = Integer.parseInt(samplePerPath.getText());
            double initialPriceValue = Double.parseDouble(initialPrice.getText());
            double driftValue = Double.parseDouble(drift.getText());
            double volatilityValue = Double.parseDouble(volatility.getText());
            double timeLengthValue = Double.parseDouble(timeLength.getText());

            // Création du LineChart
            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Geometric Brownian Motion");
            lineChart.setPrefSize(800, 600);
            lineChart.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-width: 1px;");
            lineChart.setCreateSymbols(false);

            // Création de l'histogramme
            CategoryAxis histogramXAxis = new CategoryAxis();
            NumberAxis histogramYAxis = new NumberAxis();
            BarChart<String, Number> histogram = new BarChart<>(histogramXAxis, histogramYAxis);
            histogram.setTitle("Distribution of Final Values");
            histogram.setPrefSize(800, 200);
            histogram.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-width: 1px;");
            histogramYAxis.setLabel("Count");

            List<Double> finalValues = new ArrayList<>();

            Random random = new Random();

            for (int i = 0; i < paths; i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName("GBM " + (i + 1));

                double dt = timeLengthValue / samples;
                double t = 0.0;
                double S = initialPriceValue;

                for (int j = 0; j < samples; j++) {
                    series.getData().add(new XYChart.Data<>(t, S));
                    t += dt;
                    double dW = Math.sqrt(dt) * randomGaussian(random);
                    S = S * Math.exp((driftValue - 0.5 * volatilityValue * volatilityValue) * dt +
                            volatilityValue * dW);
                }

                lineChart.getData().add(series);
                finalValues.add(S);
            }

            // Construction de l'histogramme
            int numBins = 10;
            double minValue = finalValues.stream().min(Double::compare).orElse(0.0);
            double maxValue = finalValues.stream().max(Double::compare).orElse(0.0);
            double binSize = (maxValue - minValue) / numBins;

            XYChart.Series<String, Number> histogramSeries = new XYChart.Series<>();
            histogramSeries.setName("Histogram");

            for (int i = 0; i < numBins; i++) {
                double binStart = minValue + i * binSize;
                double binEnd = binStart + binSize;
                int count = (int) finalValues.stream().filter(value -> value >= binStart && value < binEnd).count();
                histogramSeries.getData().add(new XYChart.Data<>(String.format("%.2f-%.2f", binStart, binEnd), count));
            }

            histogram.getData().add(histogramSeries);

            graphPane.getChildren().clear();
            graphPane.getChildren().addAll(lineChart, histogram);
            StackPane.setMargin(histogram, new Insets(600, 0, 0, 0));
        });
    }

    public static void main(String[] args) {
        launch();
    }

    private double randomGaussian(Random random) {
        double u1 = 1.0 - random.nextDouble(); // [0, 1) interval
        double u2 = 1.0 - random.nextDouble(); // [0, 1) interval
        double normal = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return normal;
    }
}
