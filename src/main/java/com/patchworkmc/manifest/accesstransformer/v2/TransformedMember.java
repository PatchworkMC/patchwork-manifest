package com.patchworkmc.manifest.accesstransformer.v2;

import com.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import com.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;

public abstract class TransformedMember extends Transformed {
	private final String owner;

	public TransformedMember(String owner, String name, AccessLevel accessLevel, Finalization finalization) {
		super(name, accessLevel, finalization);
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}
}
