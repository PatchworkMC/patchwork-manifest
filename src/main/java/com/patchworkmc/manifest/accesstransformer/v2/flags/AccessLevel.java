package com.patchworkmc.manifest.accesstransformer.v2.flags;

public enum AccessLevel {
	PRIVATE("private"),
	PACKAGE_PRIVATE("default"),
	PROTECTED("protected"),
	PUBLIC("public");

	String name;
	AccessLevel(String name) {
		// NO-OP
	}

	public String getForgeName() {
		return name;
	}
}
