package net.patchworkmc.manifest.accesstransformer.v2;

import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;

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
