package edabatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileHeaders;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.List;

import static edabatch.Utils.mv;

@Configuration
class FinishedFileFlowConfiguration {

 private Log log = LogFactory.getLog(getClass());

 @Bean
 IntegrationFlow finishedJobsFlow(BatchChannels channels,
  @Value("${completed-directory:${HOME}/Desktop/completed}") File finished,
  JdbcTemplate jdbcTemplate) {
  return IntegrationFlows
    .from(channels.completed())
    .handle(JobExecution.class,
      (je, headers) -> {
       String ogFileName = String.class.cast(headers
         .get(FileHeaders.ORIGINAL_FILE));
       File file = new File(ogFileName);
       mv(file, finished);
       List<Contact> contacts = jdbcTemplate.query(
         "select * from CONTACT",
         (rs, i) -> new Contact(
           rs.getBoolean("valid_email"),
           rs.getString("full_name"),
           rs.getString("email"),
           rs.getLong("id")));
       contacts.forEach(log::info);
       return null;
      }).get();
 }

}
