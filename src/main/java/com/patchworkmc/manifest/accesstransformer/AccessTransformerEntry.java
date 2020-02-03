package com.patchworkmc.manifest.accesstransformer;

import com.patchworkmc.manifest.api.Remapper;

public class AccessTransformerEntry {
	private String className;
	private String memberName;
	private String descriptor = "";
	private boolean isField;
	private boolean inheritiedFromDependency;

	public AccessTransformerEntry(String className, String memberName, boolean inheritedFromDependency) {
		this.className = className;
		this.memberName = memberName;
		this.isField = !memberName.contains("(");

		if (!this.isField) {
			int split = memberName.indexOf('(');
			memberName = memberName.substring(0, split);
			this.descriptor = this.memberName.substring(split);
		}

		this.memberName = memberName;
		this.inheritiedFromDependency = false;
	}

	public AccessTransformerEntry remap(Remapper remapper) {
		String mappedMemberName;

		if (this.isField) {
			mappedMemberName = remapper.remapFieldName(this.className, this.memberName, "");
		} else {
			mappedMemberName = remapper.remapMethodName(this.className, this.memberName, this.descriptor);
			this.descriptor = remapper.remapMemberDescription(descriptor);
		}

		this.className = remapper.remapClassName(this.className);
		this.memberName = mappedMemberName;
		return this;
	}

	public String getClassName() {
		return this.className;
	}

	public String getMemberName() {
		return this.memberName;
	}

	public boolean isField() {
		return this.isField;
	}

	public String getDescriptor() {
		return this.descriptor;
	}

	public boolean inheritiedFromDependency() {
		return this.inheritiedFromDependency;
	}

	protected AccessTransformerEntry clone(boolean setAsInherited) {
		return new AccessTransformerEntry(className, memberName, setAsInherited);
	}
}
