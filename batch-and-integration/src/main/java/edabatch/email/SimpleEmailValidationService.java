package edabatch.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Profile("!production")
class SimpleEmailValidationService implements EmailValidationService {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public boolean isEmailValid(String email) {
		boolean emailIsValid = StringUtils.hasText(email) && email.length() > 1
				&& email.contains("@");
		log.debug("emailIsValid: " + emailIsValid);
		return emailIsValid;
	}
}
