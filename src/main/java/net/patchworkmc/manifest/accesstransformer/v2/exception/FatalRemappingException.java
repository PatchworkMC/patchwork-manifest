package net.patchworkmc.manifest.accesstransformer.v2.exception;

public class FatalRemappingException extends RuntimeException {
	public FatalRemappingException(String message, Exception parent) {
		super(message, parent);
	}

	public FatalRemappingException(Exception parent) {
		super(parent);
	}
}
