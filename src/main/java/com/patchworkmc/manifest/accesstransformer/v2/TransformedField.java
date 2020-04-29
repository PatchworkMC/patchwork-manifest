package com.patchworkmc.manifest.accesstransformer.v2;

import com.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import com.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import com.patchworkmc.manifest.api.Remapper;

public class TransformedField extends TransformedMember {
	public TransformedField(String owner, String name, AccessLevel accessLevel, Finalization finalization) {
		super(owner, name, accessLevel, finalization);
	}

	@Override
	public TransformedField remap(Remapper remapper) {
		return new TransformedField(remapper.remapClassName(getOwner()), remapper.remapFieldName(getOwner(), getName()), getAccessLevel(), getFinalization());
	}
}
