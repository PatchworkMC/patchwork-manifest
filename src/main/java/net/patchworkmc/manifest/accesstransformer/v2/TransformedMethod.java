package net.patchworkmc.manifest.accesstransformer.v2;

import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;

public class TransformedMethod extends TransformedMember {
	private final String descriptor;
	public TransformedMethod(String owner, String name, String descriptor, AccessLevel accessLevel, Finalization finalization) {
		super(owner, name, accessLevel, finalization);
		this.descriptor = descriptor;
	}

	@Override
	public TransformedMethod remap(Remapper remapper) {
		return new TransformedMethod(remapper.remapClassName(getOwner()), remapper.remapMethodName(getOwner(), getName(), getDescriptor()),
						remapper.remapMemberDescription(getDescriptor()), getAccessLevel(), getFinalization());
	}

	public String getDescriptor() {
		return descriptor;
	}
}
