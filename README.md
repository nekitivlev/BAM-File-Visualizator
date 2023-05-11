# BAM File Visualizator

The BAM File Visualizator project is a Java application that allows you to visualize the coverage and records of a BAM (Binary Alignment/Map) file. It utilizes the HTSJDK library for reading BAM files and the JFreeChart library for creating plots.

## Features

- Select a BAM file to visualize.
- Specify a genomic region to focus on using the chromosome, start, and end positions.
- Display the coverage plot, showing the number of records at each position within the specified region.
- Display the records plot, showing the alignment positions of each record within the specified region.

## Prerequisites

- Java 11 or higher
- Maven

## Usage

1. Clone the repository or download the source code.
2. Open a command line interface and navigate to the project directory.
3. Build the project using Maven:
```
mvn clean package
```
4. Run the application:
```
java -jar target/bam-visualizator-1.0-SNAPSHOT-jar-with-dependencies.jar
```
5. The application window will open, allowing you to select a BAM file and specify a genomic region.
6. Click the "Select BAM File" button to choose a BAM file from your local filesystem.
7. Enter a genomic region in the "Genomic Region" text field using the format: `chromosome:start-end`.
8. Click the "Visualize" button to generate the coverage and records plots based on the specified region.
9. The coverage plot will show the number of records at each position within the region.
10. The records plot will display the alignment positions of each record within the region.
## Dependencies

The project utilizes the following dependencies:

- HTSJDK (com.github.samtools:htsjdk:2.23.0) - For reading BAM files.
- JFreeChart (org.jfree:jfreechart:1.5.3) - For creating plots.
- JCommon (org.jfree:jcommon:1.0.23) - Required by JFreeChart.

These dependencies will be automatically downloaded and managed by Maven.

