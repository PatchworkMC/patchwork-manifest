package net.patchworkmc.manifest.accesstransformer.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.patchworkmc.manifest.accesstransformer.v2.exception.MissingMappingException;
import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;
import org.jetbrains.annotations.Nullable;

public class TransformedClass extends Transformed {
	private final Set<TransformedField> fields;
	private final Set<TransformedMethod> methods;
	@Nullable
	private TransformedWildcardMember fieldWildcard;
	@Nullable
	private TransformedWildcardMember methodWildcard;

	protected TransformedClass(String name, Finalization finalization, AccessLevel accessLevel,
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

	public TransformedClass remap(Remapper remapper, Consumer<MissingMappingException> errorLogger) throws MissingMappingException {
		String remappedName = remapper.remapClassName(getName());
		Set<TransformedField> remappedFields = new HashSet<>();
		Set<TransformedMethod> remappedMethods = new HashSet<>();

		for (TransformedField field : this.getFields()) {
			try {
				remappedFields.add(field.remap(remapper));
			} catch (MissingMappingException ex) {
				errorLogger.accept(ex);
			}
		}

		for (TransformedMethod method : getMethods()) {
			try {
				remappedMethods.add(method.remap(remapper));
			} catch (MissingMappingException ex) {
				errorLogger.accept(ex);
			}
		}

		return new TransformedClass(remappedName, getFinalization(), getAccessLevel(), remappedFields, remappedMethods, fieldWildcard, methodWildcard);
	}

	/**
	 * @deprecated Use {@link TransformedClass#remap(Remapper, Consumer)} instead.
	 */
	@Override
	@Deprecated
	public TransformedClass remap(Remapper remapper) throws MissingMappingException {
		ArrayList<MissingMappingException> suppressedExceptions = new ArrayList<>();
		TransformedClass result = remap(remapper, suppressedExceptions::add);

		if (!suppressedExceptions.isEmpty()) {
			MissingMappingException ex = new MissingMappingException("Failed to remap some members for class!");
			suppressedExceptions.forEach(ex::addSuppressed);
			throw ex;
		}

		return result;
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
