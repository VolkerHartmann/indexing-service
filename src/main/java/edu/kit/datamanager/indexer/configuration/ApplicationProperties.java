/*
 * Copyright 2018 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.indexer.configuration;

import edu.kit.datamanager.configuration.GenericPluginProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 */
@ConfigurationProperties(prefix = "indexer")
@Component
@Data
@Validated
@EqualsAndHashCode(callSuper = true)
public class ApplicationProperties extends GenericPluginProperties{
 @Value("${metastore.indexer.gemma.pythonLocation}")
  private String pythonLocation;

  @Value("${metastore.indexer.gemma.gemmaLocation}")
 private String gemmaLocation;

  @Value("${metastore.indexer.mappingsLocation}")
 private String mappingsLocation;

}
