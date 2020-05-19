package net.patchworkmc.manifest.accesstransformer.v2.exception;

public class FatalMissingMappingException extends RuntimeException {
	public FatalMissingMappingException(String message, MissingMappingException parent) {
		super(message, parent);
	}

	public FatalMissingMappingException(MissingMappingException parent) {
		super(parent);
	}

	@Override
	public synchronized MissingMappingException getCause() {
		return (MissingMappingException) super.getCause();
	}

	@Override
	public synchronized Throwable initCause(Throwable cause) {
		if (!(cause instanceof MissingMappingException)) {
			throw new IllegalArgumentException("Cause must be an instance of MissingMappingException!");
		}

		return super.initCause(cause);
	}
}
