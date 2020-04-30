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
	private Transformed defaultForFields;
	@Nullable
	private Transformed defaultForMethods;

	public TransformedClass(String name, Finalization finalization, AccessLevel accessLevel,
					Set<TransformedField> fields, Set<TransformedMethod> methods,
					@Nullable Transformed defaultForFields, @Nullable Transformed defaultForMethods) {
		super(name, accessLevel, finalization);
		this.fields = fields;
		this.methods = methods;
		this.defaultForFields = defaultForFields;
		this.defaultForMethods = defaultForMethods;
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

		return new TransformedClass(remappedName, getFinalization(), getAccessLevel(), remappedFields, remappedMethods, defaultForFields, defaultForMethods);
	}

	@Nullable
	public Transformed getDefaultForFields() {
		return defaultForFields;
	}

	@Nullable
	public Transformed getDefaultForMethods() {
		return defaultForMethods;
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

	protected void acceptDefaultField(Transformed fieldDefault) {
		if (this.defaultForFields == null) {
			this.defaultForFields = fieldDefault;
		} else {
			throw new IllegalStateException("defaultForFields has already been set!");
		}
	}

	protected void acceptDefaultMethod(Transformed methodDefault) {
		if (this.defaultForMethods == null) {
			this.defaultForMethods = methodDefault;
		} else {
			throw new IllegalStateException("defaultForMethods has already been set!");
		}
	}
}
