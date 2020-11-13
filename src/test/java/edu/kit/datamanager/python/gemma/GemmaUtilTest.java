/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.python.gemma;

import edu.kit.datamanager.python.util.PythonUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GemmaUtilTest {

  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/indexer/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";

  private static final String RESULT = "{\n"
          + "  \"Publisher\": \"The publisher\",\n"
          + "  \"Publication Date\": \"2019\"\n"
          + "}";

  private static String PYTHON_EXECUTABLE;
  private static String GEMMA_CLASS;

  public GemmaUtilTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException {
    // Determine python location
    OutputStream os = new ByteArrayOutputStream();
    PythonUtils.run("which", "python3", os, null);
    String pythonExecutable = os.toString();
    os.flush();
    if (pythonExecutable.isBlank()) {
      PythonUtils.run("which", "python", os, null);
      pythonExecutable = os.toString();
    }
    if (pythonExecutable.isBlank()) {
      throw new IOException("Python seems not to be available!");
    }
    System.out.println("Location of python: " + pythonExecutable);
    PYTHON_EXECUTABLE = pythonExecutable.trim();
    GEMMA_CLASS = new File("src/test/resources/python/mapping_single.py").getAbsoluteFile().toString();
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    try {
      try (Stream<Path> walk = Files.walk(Paths.get(URI.create("file://" + TEMP_DIR_4_MAPPING)))) {
        walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
      }
      Paths.get(TEMP_DIR_4_MAPPING).toFile().mkdir();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of runGemma method, of class GemmaUtil.
   */
  @Test
  public void testRunGemma() throws IOException {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("runGemma");
    Path mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsoluteFile().toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/simple.json").getAbsoluteFile().toPath();
    Path resultFile = new File("/tmp/result.elastic.json").getAbsoluteFile().toPath();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    GemmaUtil instance = new GemmaUtil(conf);
    int expResult = 0;
    int result = instance.runGemma(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);

    assertTrue(resultFile.toFile().exists());
    String readFileToString = FileUtils.readFileToString(resultFile.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
  }

  /**
   * Test of runGemma method, of class GemmaUtil.
   */
  @Test
  public void testRunGemmaXmlMapping() throws IOException {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("testRunGemmaXmlMapping");
    Path mappingFile = new File("src/test/resources/mapping/gemma/simple.xml.mapping").getAbsoluteFile().toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/simple.xml").getAbsoluteFile().toPath();
    Path resultFile = new File("/tmp/result.xml.elastic.json").getAbsoluteFile().toPath();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    GemmaUtil instance = new GemmaUtil(conf);
    int expResult = 0;
    int result = instance.runGemma(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);

    assertTrue(resultFile.toFile().exists());
    String readFileToString = FileUtils.readFileToString(resultFile.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
  }

  /**
   * Test of runGemma method, of class GemmaUtil.
   */
  @Test
  public void testRunGemmaWrongMapping() throws IOException {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("testRunGemmaWrongMapping");
    Path mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsoluteFile().toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/notexists").getAbsoluteFile().toPath();
    Path resultFile = new File("/tmp/invalid_result.elastic.json").getAbsoluteFile().toPath();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    GemmaUtil instance = new GemmaUtil(conf);
    int expResult = PythonUtils.EXECUTION_ERROR;
    int result = instance.runGemma(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);

    assertTrue(resultFile.toFile().exists());
    String readFileToString = FileUtils.readFileToString(resultFile.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
  }

  /**
   * Test of downloadResource method, of class GemmaUtil.
   */
  @Test
  public void testDownloadResource() throws URISyntaxException {
    System.out.println("downloadResource");
    URI resourceURL = new URI("https://www.example.org");
    GemmaUtil instance = new GemmaUtil(new GemmaConfiguration());
    Optional<Path> result = instance.downloadResource(resourceURL);
    assertTrue(result.isPresent());
    assertTrue(result.get().toFile().exists());
    assertTrue(result.get().toFile().delete());

    resourceURL = new URI("https://invalidhttpaddress.de");
    result = instance.downloadResource(resourceURL);
    assertTrue(result.isEmpty());
  }

}
