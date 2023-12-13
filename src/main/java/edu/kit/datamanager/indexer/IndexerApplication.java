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
package edu.kit.datamanager.indexer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.entities.messaging.IAMQPSubmittable;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.service.IMessagingService;
import edu.kit.datamanager.service.impl.RabbitMQMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"edu.kit.datamanager.indexer", "edu.kit.datamanager.messaging.client.configuration", "edu.kit.datamanager.messaging.client.receiver", "edu.kit.datamanager.configuration"})
// @ComponentScan({"edu.kit.datamanager.indexer", "edu.kit.datamanager.messaging.client"})
//@ComponentScan({"edu.kit.datamanager.indexer", "edu.kit.datamanager.entities", "edu.kit.datamanager.configuration", "edu.kit.datamanager.messaging.client"})
public class IndexerApplication {

  private static final Logger LOG = LoggerFactory.getLogger(IndexerApplication.class);

	@Bean
    @Scope("prototype")
    public Logger logger(InjectionPoint injectionPoint) {
        Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(targetClass.getCanonicalName());
    }

    @Bean(name = "OBJECT_MAPPER_BEAN")
  public ObjectMapper jsonObjectMapper(){
    return Jackson2ObjectMapperBuilder.json()
            .serializationInclusion(JsonInclude.Include.NON_EMPTY) // Don’t include null values
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
            .modules(new JavaTimeModule())
            .build();
  }
  @Bean
  @ConfigurationProperties("repo")
  public ApplicationProperties applicationProperties(){
    return new ApplicationProperties();
  }

//  @Bean
//  public IMessagingService messagingService(){
//    return new RabbitMQMessagingService();
//  }
//
	public static void main(String[] args) {
		SpringApplication.run(IndexerApplication.class, args);
		System.out.println("Indexing service is running!");
	}
//
//  @Bean
//  @ConditionalOnProperty(prefix = "repo.messaging", name = "enabled", havingValue = "true")
//  public IMessagingService messagingService() {
//    LOG.trace("LOAD RabbitMQ");
//    return new RabbitMQMessagingService();
//  }
//
//  @Bean(name = "messagingService")
//  @ConditionalOnProperty(prefix = "repo.messaging", name = "enabled", havingValue = "false")
//  public IMessagingService dummyMessagingService() {
//    LOG.trace("LOAD DUMMY RabbitMQ");
//    return new IMessagingService() {
//      @Override
//      public void send(IAMQPSubmittable iamqps) {
//        LOG.trace("RabbitMQ send dummy");
//      }
//
//      @Override
//      public Health health() {
//        LOG.trace("RabbitMQ health dummy");
//        return new Health.Builder().up().build();
//      }
//    };
//  }

}
