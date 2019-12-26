package com.patchworkmc.manifest.api;

public interface Remapper {
	String remapMemberDescription(String descriptor);

	String remapFieldName(String owner, String name, String descriptor);

	String remapMethodName(String owner, String name, String descriptor);

	String remapClassName(String name);
}
