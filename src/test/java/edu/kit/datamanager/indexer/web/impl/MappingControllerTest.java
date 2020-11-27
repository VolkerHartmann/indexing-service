/*
 * Copyright 2019 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.indexer.web.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.domain.acl.AclEntry;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {"server.port=41300"})
public class MappingControllerTest {
  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";
  private static final String MAPPING_ID = "my_dc";
 private static final String MAPPING_TYPE = "GEMMA";
  
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private FilterChainProxy springSecurityFilterChain;
  @Autowired
  private IMappingRecordDao mappingRecordDao;
  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  public MappingControllerTest() {
  }
  
  @Before
  public void setUp() {
    mappingRecordDao.deleteAll();
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
   this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .addFilters(springSecurityFilterChain)
           .apply(documentationConfiguration(this.restDocumentation))
            .build();
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMapping() throws Exception {
    System.out.println("createMapping");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
  }
  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingNoRecord() throws Exception {
    System.out.println("testCreateMappingNoRecord");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }
   /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingEmptyRecord() throws Exception {
    System.out.println("testCreateMappingEmptyRecord");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", "".getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }
 @Test
  public void testCreateMappingNoMapping() throws Exception {
    System.out.println("testCreateMappingNoMapping");
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }
  @Test
  public void testCreateMappingWrongRecord() throws Exception {
    System.out.println("testCreateMappingEmptyMapping");
    String mappingContent = "";
    MappingRecord record = new MappingRecord();
    record.setMappingId(null);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    record.setMappingId(MAPPING_ID);
    record.setMappingType(null);
    recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingTwice() throws Exception {
    System.out.println("testCreateMappingTwice");
    testCreateMapping();
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isConflict()).andReturn();
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingWithAcl() throws Exception {
    System.out.println("testCreateMappingWithAcl");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
  }
//
//  /**
//   * Test of getMappingById method, of class MappingController.
//   */
//  @Test
//  public void testGetMappingById() {
//    System.out.println("getMappingById");
//    String mappingId = MAPPING_ID;
//    String mappingType = MAPPING_TYPE;
//    WebRequest wr = null;
//    HttpServletResponse hsr = null;
//    MappingController instance = new MappingController();
//    ResponseEntity<MappingRecord> expResult = null;
//    ResponseEntity<MappingRecord> result = instance.getMappingById(mappingId, mappingType, wr, hsr);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getMappingDocumentById method, of class MappingController.
//   */
//  @Test
//  public void testGetMappingDocumentById() {
//    System.out.println("getMappingDocumentById");
//    String mappingId = MAPPING_ID;
//    String mappingType = MAPPING_TYPE;
//    WebRequest wr = null;
//    HttpServletResponse hsr = null;
//    MappingController instance = new MappingController();
//    ResponseEntity expResult = null;
//    ResponseEntity result = instance.getMappingDocumentById(mappingId, mappingType, wr, hsr);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getMappings method, of class MappingController.
//   */
//  @Test
//  public void testGetMappings() {
//    System.out.println("getMappings");
//    Pageable pgbl = null;
//    WebRequest wr = null;
//    HttpServletResponse hsr = null;
//    UriComponentsBuilder ucb = null;
//    MappingController instance = new MappingController();
//    ResponseEntity<List<MappingRecord>> expResult = null;
//    ResponseEntity<List<MappingRecord>> result = instance.getMappings(pgbl, wr, hsr, ucb);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of updateMapping method, of class MappingController.
//   */
//  @Test
//  public void testUpdateMapping() throws JsonProcessingException {
//    System.out.println("updateMapping");
//    String mappingId = MAPPING_ID;
//    String mappingType = MAPPING_TYPE;
//    MappingRecord record = new MappingRecord();
//    record.setMappingId(mappingId);
//    record.setMappingType(mappingType);
//    ObjectMapper mapper = new ObjectMapper();
//    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
//    MultipartFile document = null;
//    WebRequest request = null;
//    HttpServletResponse response = null;
//    UriComponentsBuilder uriBuilder = null;
//    MappingController instance = new MappingController();
//    ResponseEntity expResult = null;
//    ResponseEntity result = instance.updateMapping(recordFile, document, request, response, uriBuilder);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of deleteMapping method, of class MappingController.
//   */
//  @Test
//  public void testDeleteMapping() {
//    System.out.println("deleteMapping");
//    String mappingId = MAPPING_ID;
//    String mappingType = MAPPING_TYPE;
//    WebRequest wr = null;
//    HttpServletResponse hsr = null;
//    MappingController instance = new MappingController();
//    ResponseEntity expResult = null;
//    ResponseEntity result = instance.deleteMapping(mappingId, mappingType, wr, hsr);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of mergeRecords method, of class MappingController.
//   */
//  @Test
//  public void testMergeRecords() {
//    System.out.println("mergeRecords");
//    MappingRecord managed = null;
//    MappingRecord provided = null;
//    MappingController instance = new MappingController();
//    MappingRecord expResult = null;
//    MappingRecord result = instance.mergeRecords(managed, provided);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
  
}
