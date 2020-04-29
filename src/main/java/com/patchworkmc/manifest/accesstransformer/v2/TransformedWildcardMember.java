package com.patchworkmc.manifest.accesstransformer.v2;

import com.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import com.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import com.patchworkmc.manifest.api.Remapper;

public class TransformedWildcardMember extends Transformed {
	public TransformedWildcardMember(AccessLevel accessLevel, Finalization finalization) {
		super("", accessLevel, finalization);
	}

	@Override
	public Transformed remap(Remapper remapper) {
		return new TransformedWildcardMember(getAccessLevel(), getFinalization());
	}
}
