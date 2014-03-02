package client;

public class AccessDeniedException extends RuntimeException {
	private static final long serialVersionUID = -2346835601351999050L;

	public AccessDeniedException(String message) {
		super(message);
	}
}
