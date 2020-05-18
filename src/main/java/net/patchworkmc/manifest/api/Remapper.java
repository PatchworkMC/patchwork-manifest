package net.patchworkmc.manifest.api;

import net.patchworkmc.manifest.accesstransformer.v2.exception.MissingMappingException;

public interface Remapper {
	String remapMemberDescription(String descriptor) throws MissingMappingException;

	default String remapFieldName(String owner, String name) throws MissingMappingException {
		return remapFieldName(owner, name, "");
	}

	String remapFieldName(String owner, String name, String descriptor) throws MissingMappingException;

	String remapMethodName(String owner, String name, String descriptor) throws MissingMappingException;

	String remapClassName(String name) throws MissingMappingException;
}
