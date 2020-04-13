package com.patchworkmc.manifest.accesstransformer.v2;

import com.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import com.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import com.patchworkmc.manifest.api.Remapper;

public class TransformedNameless extends Transformed {
	public TransformedNameless(AccessLevel accessLevel, Finalization finalization) {
		super("", accessLevel, finalization);
	}

	@Override
	public Transformed remap(Remapper remapper) {
		return new TransformedNameless(getAccessLevel(), getFinalization());
	}
}
