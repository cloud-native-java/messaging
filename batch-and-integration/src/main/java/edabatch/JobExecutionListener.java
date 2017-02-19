package edabatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.JobExecutionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

@Component
class JobExecutionListener {

 private final JdbcTemplate jdbcTemplate;

 private Log log = LogFactory.getLog(getClass());

 @Autowired
 public JobExecutionListener(JdbcTemplate template) {
  this.jdbcTemplate = template;
 }

 @EventListener(JobExecutionEvent.class)
 public void job(JobExecutionEvent executionEvent) {
  log.info("jobExecutionEvent: "
   + executionEvent.getJobExecution().toString());
  jdbcTemplate.query("select * from CONTACT", (RowCallbackHandler) rs -> log
   .info(String.format("id=%s, full_name=%s, email=%s, valid_email=%s",
    rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
    rs.getBoolean("valid_email"))));
 }
}
