package com.patchworkmc.manifest.accesstransformer;

import com.patchworkmc.manifest.api.Remapper;

public class AccessTransformerEntry {
	private String clazzName;
	private String memberName;
	private String memberDescription = "";
	private boolean memberIsField;

	public AccessTransformerEntry(String clazzName, String memberName) {
		this.clazzName = clazzName;
		this.memberName = memberName;
		this.memberIsField = !memberName.contains("(");

		if (!this.memberIsField) {
			int split = memberName.indexOf('(');
			memberName = memberName.substring(0, split);
			this.memberDescription = this.memberName.substring(split);
		}

		this.memberName = memberName;
	}

	public AccessTransformerEntry remap(Remapper remapper) {
		String mappedMemberName;

		if (this.memberIsField) {
			mappedMemberName = remapper.remapFieldName(this.clazzName, this.memberName, "");
		} else {
			mappedMemberName = remapper.remapMethodName(this.clazzName, this.memberName, this.memberDescription);
			this.memberDescription = remapper.remapMemberDescription(memberDescription);
		}

		this.clazzName = remapper.remapClassName(this.clazzName);
		this.memberName = mappedMemberName;
		return this;
	}

	public String getClassName() {
		return this.clazzName;
	}

	public String getMemberName() {
		return this.memberName;
	}

	public boolean memberIsField() {
		return this.memberIsField;
	}

	public String getMemberDescription() {
		return this.memberDescription;
	}
}
