package org.example;

import static java.lang.Math.max;
import static java.lang.Math.min;
import htsjdk.samtools.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;

public class BAMFileVisualizator extends JFrame {

  private String bamFilePath;
  private JTextField regionTextField;

  ArrayList<String> log;

  public BAMFileVisualizator() {
    log = new ArrayList<>();
    initializeUI();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("BAM File Visualization");
    pack();
    setVisible(true);
  }

  private void initializeUI() {
    regionTextField = new JTextField(20);
    JButton selectBAMFileButton = new JButton("Select BAM File");
    selectBAMFileButton.addActionListener(e -> selectBAMFile());

    JButton visualizeButton = new JButton("Visualize");
    visualizeButton.addActionListener(e -> visualizeBAMRecords());

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel inputPanel = new JPanel();
    inputPanel.add(selectBAMFileButton);
    inputPanel.add(new JLabel("Genomic Region:"));
    inputPanel.add(regionTextField);
    inputPanel.add(visualizeButton);

    contentPane.add(inputPanel, BorderLayout.NORTH);

    // Create the chart panels
    ChartPanel coveragePanel = createCoveragePanel();
    ChartPanel recordsPanel = createRecordsPanel();

    // Create a split pane to display the two charts side by side
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, coveragePanel, recordsPanel);
    splitPane.setResizeWeight(0.5);
    contentPane.add(splitPane, BorderLayout.CENTER);

    setContentPane(contentPane);
  }

  private void selectBAMFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.getName().toLowerCase().endsWith(".bam") || f.isDirectory();
      }

      @Override
      public String getDescription() {
        return "BAM Files (*.bam)";
      }
    });

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      bamFilePath = selectedFile.getAbsolutePath();
    }

  }


  private ChartPanel createCoveragePanel() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    JFreeChart chart = ChartFactory.createBarChart(
        "Coverage", "Position", "Number of Records",
        dataset, PlotOrientation.VERTICAL, true, true, false);

    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setSeriesPaint(0, Color.BLUE);

    return new ChartPanel(chart);
  }


  private ChartPanel createRecordsPanel() {
    DefaultXYDataset dataset = new DefaultXYDataset();
    JFreeChart chart = ChartFactory.createXYLineChart(
        "Records", "Position", "Number of Record",
        dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, true, false);
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesPaint(0, Color.RED);
    plot.setRenderer(renderer);

    return new ChartPanel(chart);
  }

  private void visualizeBAMRecords() {
    String region = regionTextField.getText().trim().replace(",", "");

    if (bamFilePath == null || bamFilePath.isEmpty() || !bamFilePath.matches(".*\\.bam")) {
      log.add("Please select a BAM file.");
      JOptionPane.showMessageDialog(this, "Please select a BAM file.");
      return;
    }

    if (!region.matches("chr.+:[0-9]+-[0-9]+")) {
      log.add("Invalid region format. Please use the format: chromosome:start-end");
      JOptionPane.showMessageDialog(this,
          "Invalid region format. Please use the format: chromosome:start-end");
      return;
    }
    // Parse chromosome, start, and end from the input region
    String[] parts = region.split(":");
    String chromosome = parts[0];
    String[] range = parts[1].split("-");

    int start = Integer.parseInt(range[0]);
    int end = Integer.parseInt(range[1]);

    // Check if the start position is greater than the end position
    if (start > end) {
      log.add("Invalid region. Start position cannot be greater than the end position.");
      JOptionPane.showMessageDialog(this,
          "Invalid region. Start position cannot be greater than the end position.");
      return;
    }

    Path bamFile = Path.of(bamFilePath);

    try (SamReader reader = SamReaderFactory.makeDefault().open(bamFile)) {
      // Fetch the records overlapping the specified region
      SAMRecordIterator iterator = reader.query(chromosome, start, end, false);

      List<SAMRecord> recordsList = new ArrayList<>();
      while (iterator.hasNext()) {
        SAMRecord record = iterator.next();
        recordsList.add(record);
      }

      int regionLength = end - start + 1;

      ArrayList<Integer> coverage = new ArrayList<>(regionLength);
      for (int i = 0; i < regionLength; i++) {
        coverage.add(0);
      }
      for (SAMRecord record : recordsList) {
        int recordStart = record.getAlignmentStart();
        int recordEnd = record.getAlignmentEnd();
        int startPos = Math.max(start, recordStart);
        int endPos = Math.min(end, recordEnd);
        for (int i = startPos; i <= endPos; i++) {
          coverage.set(i - start, coverage.get(i - start) + 1);
        }
      }

      // Create coverage dataset
      DefaultCategoryDataset coverageDataset = new DefaultCategoryDataset();
      for (int i = 0; i < regionLength; i++) {
        int position = start + i;
        int count = coverage.get(i);
        coverageDataset.addValue(count, "Coverage", String.valueOf(position));
      }

      // Update the coverage chart
      JSplitPane splitPane = (JSplitPane) getContentPane().getComponent(1);
      ChartPanel coveragePanel = (ChartPanel) splitPane.getLeftComponent();
      JFreeChart coverageChart = ChartFactory.createBarChart(
          "Coverage", "Position", "Number of Records",
          coverageDataset, PlotOrientation.VERTICAL, true, true, false);
      CategoryPlot coveragePlot = coverageChart.getCategoryPlot();
      BarRenderer renderer = (BarRenderer) coveragePlot.getRenderer();
      renderer.setBarPainter(new StandardBarPainter());
      renderer.setSeriesPaint(0, Color.BLUE);
      coveragePlot.setRenderer(renderer);
      coveragePanel.setChart(coverageChart);

      // Create records dataset
      DefaultXYDataset recordsDataset = new DefaultXYDataset();
      int numRecords = recordsList.size();
      double[][][] recordsData = new double[numRecords][2][2];

      for (int i = 0; i < numRecords; i++) {
        SAMRecord record = recordsList.get(i);
        int recordStart = record.getAlignmentStart();
        int recordEnd = record.getAlignmentEnd();

        recordsData[i][0][0] = max(recordStart, start);
        recordsData[i][1][0] = i;
        recordsData[i][0][1] = min(recordEnd, end);
        recordsData[i][1][1] = i;
        recordsDataset.addSeries("Record " + (i + 1), recordsData[i]);
      }

      // Create records chart
      JFreeChart recordsChart = ChartFactory.createXYLineChart(
          "Records", "Position", "Number of Record",
          recordsDataset, PlotOrientation.VERTICAL, true, true, false);
      XYPlot recordsPlot = recordsChart.getXYPlot();

      // Set the renderer for the records plot
      XYLineAndShapeRenderer recordsRenderer = new XYLineAndShapeRenderer();
      for (int i = 0; i < numRecords; i++) {
        recordsRenderer.setSeriesPaint(i, Color.RED); // Set the line color for each record
        recordsRenderer.setSeriesLinesVisible(i, true); // Display lines for each record
        recordsRenderer.setSeriesShapesVisible(i, false); // Do not display shapes for each record
      }
      recordsPlot.setRenderer(recordsRenderer);

      // Create records panel
      ChartPanel recordsPanel = new ChartPanel(recordsChart);

      // Update the content pane
      JSplitPane splitPane1 = (JSplitPane) getContentPane().getComponent(1);
      splitPane1.setBottomComponent(recordsPanel);

      // Refresh the charts
      coveragePanel.repaint();
      recordsPanel.repaint();

      // Close the iterator
      iterator.close();
    } catch (IOException e) {
      log.add("Error reading BAM file: " + e.getMessage());
      JOptionPane.showMessageDialog(this, "Error reading BAM file: " + e.getMessage());
    }
  }


  public static void main(String[] args) {
    SwingUtilities.invokeLater(BAMFileVisualizator::new);
  }
}
