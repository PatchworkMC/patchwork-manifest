package net.patchworkmc.manifest.accesstransformer.v2;

import net.patchworkmc.manifest.accesstransformer.v2.exception.MissingMappingException;
import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;

public class TransformedField extends TransformedMember {
	public TransformedField(String owner, String name, AccessLevel accessLevel, Finalization finalization) {
		super(owner, name, accessLevel, finalization);
	}

	@Override
	public TransformedField remap(Remapper remapper) throws MissingMappingException {
		return new TransformedField(remapper.remapClassName(getOwner()), remapper.remapFieldName(getOwner(), getName()), getAccessLevel(), getFinalization());
	}
}
