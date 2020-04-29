package com.patchworkmc.manifest.accesstransformer.v2.flags;

public enum AccessLevel {
	PRIVATE,
	DEFAULT, // package-private--using FMLAT name for valueOf support
	PROTECTED,
	PUBLIC,
	KEEP // for classes
}
