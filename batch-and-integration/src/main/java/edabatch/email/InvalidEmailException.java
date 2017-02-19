package edabatch.email;

public class InvalidEmailException extends Exception {

 public InvalidEmailException(String email) {
  super(String.format("the email %s isn't valid", email));
 }
}
