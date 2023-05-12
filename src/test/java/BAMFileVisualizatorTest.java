import java.util.ArrayList;
import org.example.BAMFileVisualizator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JTextField;

public class BAMFileVisualizatorTest {

  private BAMFileVisualizator visualizator;

  @BeforeEach
  public void setup() {
    visualizator = new BAMFileVisualizator();
    visualizator.setVisible(false); // Set the frame to invisible for testing
  }

  @Test
  public void testVisualizeBAMRecordsWithCorrectInput()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    // Set the bamFilePath field using reflection
    Field bamFilePathField = BAMFileVisualizator.class.getDeclaredField("bamFilePath");
    bamFilePathField.setAccessible(true);
    bamFilePathField.set(visualizator, "./example_bsseq.hg19.se.sorted.bam");

    // Set the regionTextField text using reflection
    JTextField regionTextField = (JTextField) getField(visualizator, "regionTextField");
    regionTextField.setText("chr16:10,239-10,543");

    invokeVisualizeBAMRecords();

    // Assertions for correct input
    Assertions.assertTrue(getOptionPaneMessage().isEmpty(),
        "No error message expected for correct input");
  }

  @Test
  public void testVisualizeBAMRecordsWithInvalidRegionFormat()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    // Set the bamFilePath field using reflection
    Field bamFilePathField = BAMFileVisualizator.class.getDeclaredField("bamFilePath");
    bamFilePathField.setAccessible(true);
    bamFilePathField.set(visualizator, "/example_bsseq.hg19.se.sorted.bam");

    // Set the regionTextField text using reflection
    JTextField regionTextField = (JTextField) getField(visualizator, "regionTextField");
    regionTextField.setText("invalid");

    invokeVisualizeBAMRecords();

    // Assertions for invalid region format
    Assertions.assertFalse(getOptionPaneMessage().isEmpty(),
        "Error message expected for invalid region format");
  }

  @Test
  public void testVisualizeBAMRecordsWithStartPositionGreaterThanEndPosition()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    // Set the bamFilePath field using reflection
    Field bamFilePathField = BAMFileVisualizator.class.getDeclaredField("bamFilePath");
    bamFilePathField.setAccessible(true);
    bamFilePathField.set(visualizator, "./example_bsseq.hg19.se.sorted.bam");

    // Set the regionTextField text using reflection
    JTextField regionTextField = (JTextField) getField(visualizator, "regionTextField");
    regionTextField.setText("chr1:1000-1");

    invokeVisualizeBAMRecords();

    // Assertions for start position greater than end position
    Assertions.assertFalse(getOptionPaneMessage().isEmpty(),
        "Error message expected for start position greater than end position");
  }

  @Test
  public void testVisualizeBAMRecordsWithInvalidFileFormat()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    // Set the bamFilePath field using reflection
    Field bamFilePathField = BAMFileVisualizator.class.getDeclaredField("bamFilePath");
    bamFilePathField.setAccessible(true);
    bamFilePathField.set(visualizator, "./test.txt"); // Invalid file format, not a BAM file

    // Set the regionTextField text using reflection
    JTextField regionTextField = (JTextField) getField(visualizator, "regionTextField");
    regionTextField.setText("chr1:1-1000");

    invokeVisualizeBAMRecords();

    // Assertions for invalid file format
    Assertions.assertFalse(getOptionPaneMessage().isEmpty(),
        "Error message expected for invalid file format");
  }

  @Test
  public void testVisualizeBAMRecordsWithoutSelectingBAMFile()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    // Set the regionTextField text using reflection
    JTextField regionTextField = (JTextField) getField(visualizator, "regionTextField");
    regionTextField.setText("chr1:1-1000");

    invokeVisualizeBAMRecords();

    // Assertions for not selecting a BAM file
    Assertions.assertFalse(getOptionPaneMessage().isEmpty(),
        "Error message expected for not selecting a BAM file");
  }

  private void invokeVisualizeBAMRecords()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method visualizeBAMRecords = BAMFileVisualizator.class.getDeclaredMethod("visualizeBAMRecords");
    visualizeBAMRecords.setAccessible(true);
    visualizeBAMRecords.invoke(visualizator);
  }

  private Object getField(Object object, String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = object.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(object);
  }

  private String getOptionPaneMessage() throws NoSuchFieldException, IllegalAccessException {
    ArrayList<String> log = (ArrayList<String>) getField(visualizator, "log");
    if (log.size() > 0) {
      return log.get(log.size() - 1);
    } else {
      return "";
    }
  }
}
