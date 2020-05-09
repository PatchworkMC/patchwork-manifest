package net.patchworkmc.manifest.accesstransformer.v2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;

public class TransformedClass extends Transformed {
	private final Set<TransformedField> fields;
	private final Set<TransformedMethod> methods;
	@Nullable
	private TransformedWildcardMember fieldWildcard;
	@Nullable
	private TransformedWildcardMember methodWildcard;

	public TransformedClass(String name, Finalization finalization, AccessLevel accessLevel,
	                        Set<TransformedField> fields, Set<TransformedMethod> methods,
	                        @Nullable TransformedWildcardMember fieldWildcard, @Nullable TransformedWildcardMember methodWildcard) {
		super(name, accessLevel, finalization);
		this.fields = fields;
		this.methods = methods;
		this.fieldWildcard = fieldWildcard;
		this.methodWildcard = methodWildcard;
	}

	// For use with the parser in ForgeAccessTransformer
	protected TransformedClass(String name, Finalization finalization, AccessLevel accessLevel) {
		this(name, finalization, accessLevel, new HashSet<>(), new HashSet<>(), null, null);
	}

	public Set<TransformedField> getFields() {
		// We want to make it clear this is a read-only representation of an AT besides remapping.
		return Collections.unmodifiableSet(fields);
	}

	public Set<TransformedMethod> getMethods() {
		return Collections.unmodifiableSet(methods);
	}

	@Override
	public TransformedClass remap(Remapper remapper) {
		String remappedName = remapper.remapClassName(getName());
		Set<TransformedField> remappedFields = new HashSet<>();
		Set<TransformedMethod> remappedMethods = new HashSet<>();

		for (TransformedField field : this.getFields()) {
			remappedFields.add(field.remap(remapper));
		}

		for (TransformedMethod method : getMethods()) {
			remappedMethods.add(method.remap(remapper));
		}

		return new TransformedClass(remappedName, getFinalization(), getAccessLevel(), remappedFields, remappedMethods, fieldWildcard, methodWildcard);
	}

	@Nullable
	public TransformedWildcardMember getFieldWildcard() {
		return fieldWildcard;
	}

	@Nullable
	public TransformedWildcardMember getMethodWildcard() {
		return methodWildcard;
	}

	// These methods are helpers for the parser.
	// "Transformed" objects should __NEVER__ be modified after they have left the parsing method's scope.
	protected void acceptField(TransformedField field) {
		if (!this.fields.add(field)) {
			throw new IllegalArgumentException("field " + field.getName() + " already present in set!");
		}
	}

	protected void acceptMethod(TransformedMethod method) {
		if (!this.methods.add(method)) {
			throw new IllegalArgumentException("method " + method.getName() + "already present in set!");
		}
	}

	protected void acceptFieldWildcard(TransformedWildcardMember fieldWildcard) {
		if (this.fieldWildcard == null) {
			this.fieldWildcard = fieldWildcard;
		} else {
			throw new IllegalStateException("fieldWildcard has already been set!");
		}
	}

	protected void acceptMethodWildcard(TransformedWildcardMember methodWildcard) {
		if (this.methodWildcard == null) {
			this.methodWildcard = methodWildcard;
		} else {
			throw new IllegalStateException("methodWildcard has already been set!");
		}
	}
}
