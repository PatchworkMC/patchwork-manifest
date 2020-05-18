package net.patchworkmc.manifest.accesstransformer.v2.exception;

public class MissingMappingException extends Exception {
	public MissingMappingException(String message) {
		super(message);
	}

	public MissingMappingException(String message, Exception parent) {
		super(message, parent);
	}
}
