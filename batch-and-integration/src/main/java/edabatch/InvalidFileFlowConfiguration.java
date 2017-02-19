package edabatch;

import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileHeaders;

import java.io.File;

import static edabatch.Utils.mv;

@Configuration
class InvalidFileFlowConfiguration {

 @Bean
 IntegrationFlow invalidFileFlow(BatchChannels channels,
  @Value("${error-directory:${HOME}/Desktop/errors}") File errors) {
  return IntegrationFlows
    .from(channels.invalid())
    .handle(JobExecution.class,
      (je, headers) -> {
       String ogFileName = String.class.cast(headers
         .get(FileHeaders.ORIGINAL_FILE));
       File file = new File(ogFileName);
       mv(file, errors);
       return null;
      }).get();
 }

}
