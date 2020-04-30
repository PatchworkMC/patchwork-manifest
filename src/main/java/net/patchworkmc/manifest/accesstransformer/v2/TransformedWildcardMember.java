package net.patchworkmc.manifest.accesstransformer.v2;

import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;

public class TransformedWildcardMember extends Transformed {
	public TransformedWildcardMember(AccessLevel accessLevel, Finalization finalization) {
		super("", accessLevel, finalization);
	}

	@Override
	public Transformed remap(Remapper remapper) {
		return new TransformedWildcardMember(getAccessLevel(), getFinalization());
	}
}
