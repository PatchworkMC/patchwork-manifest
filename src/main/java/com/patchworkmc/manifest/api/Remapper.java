package com.patchworkmc.manifest.api;

public interface Remapper {
	String remapMemberDescription(String descriptor);

	default String remapFieldName(String owner, String name) {
		return remapFieldName(owner, name, "");
	}

	String remapFieldName(String owner, String name, String descriptor);

	String remapMethodName(String owner, String name, String descriptor);

	String remapClassName(String name);
}
