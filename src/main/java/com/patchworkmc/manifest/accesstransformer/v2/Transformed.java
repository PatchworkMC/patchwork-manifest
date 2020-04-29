package com.patchworkmc.manifest.accesstransformer.v2;

import com.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import com.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import com.patchworkmc.manifest.api.Remapper;

public abstract class Transformed {
	private final String name;
	private final AccessLevel accessLevel;
	private final Finalization finalization;

	public Transformed(String name, AccessLevel accessLevel, Finalization finalization) {
		this.name = name;
		this.accessLevel = accessLevel;
		this.finalization = finalization;
	}

	public abstract Transformed remap(Remapper remapper);

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public Finalization getFinalization() {
		return finalization;
	}

	public String getName() {
		return name;
	}
}
