package com.patchworkmc.manifest.accesstransformer.v2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import com.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import com.patchworkmc.manifest.api.Remapper;
import com.patchworkmc.manifest.mod.ManifestParseException;

/**
 * A representation of an access transformer file that adheres to the
 * <a href="https://github.com/MinecraftForge/AccessTransformers/blob/master/FMLAT.md">FML Access Transformer file specification</a>.
 */
// Technically this should be called "ModAccessTransformer" (see the "mod" package) but that conflicts with a Patcher name.
public class ForgeAccessTransformer {
	private Set<TransformedClass> classes;
	public ForgeAccessTransformer(Set<TransformedClass> classes) {
		this.classes = classes;
	}

	/**
	 * Remaps everything in this AT representation, down to the fields and methods.
	 */
	public void remap(Remapper remapper) {
		Set<TransformedClass> remappedClasses = new HashSet<>();

		for (TransformedClass transformedClass : this.classes) {
			remappedClasses.add(transformedClass.remap(remapper));
		}

		this.classes = remappedClasses;
	}

	public Set<TransformedClass> getClasses() {
		return Collections.unmodifiableSet(classes);
	}

	public static ForgeAccessTransformer parse(Path path) throws ManifestParseException {
		HashMap<String, TransformedClass> classes = new HashMap<>();

		try {
			List<String> lines;
			lines = Files.readAllLines(path);

			for (String line : lines) {
				// List of all words in this line with comments stripped
				String[] words = line.split("#")[0].split(" ");

				if (words.length == 0) {
					continue;
				}

				// make sure our length is correct
				if (words.length < 2 || words.length > 3) {
					throw new ManifestParseException("expected size of 2 or 3, got " + words.length);
				}

				String modifier = words[0];
				Finalization finalization = getFinalization(modifier);
				AccessLevel accessLevel = getAccessLevel(modifier, finalization);

				String targetClassName = words[1];

				if (words.length == 2) {
					if (classes.containsKey(targetClassName)) {
						throw new ManifestParseException("two transformations of the same class!");
					}

					classes.put(targetClassName, new TransformedClass(targetClassName, finalization, accessLevel));
				} else {
					// Method or field
					TransformedClass targetClass = classes.computeIfAbsent(targetClassName, name -> new TransformedClass(name, Finalization.KEEP, AccessLevel.KEEP));

					String memberWord = words[2];

					if (memberWord.equals("*()")) {
						targetClass.acceptDefaultMethod(new TransformedWildcardMember(accessLevel, finalization));
					} else if (memberWord.contains("(") && memberWord.contains(")")) {
						// Method
						String methodName = memberWord.substring(0, memberWord.indexOf('('));
						String methodDescriptor = memberWord.substring(memberWord.indexOf('(', memberWord.indexOf(')' + 1)));

						targetClass.acceptMethod(new TransformedMethod(targetClassName, methodName, "(" + methodDescriptor, accessLevel, finalization));
					} else {
						// Fail hard for malformed member entries
						if (memberWord.contains("(") || memberWord.contains(")")) {
							throw new ManifestParseException("unopened/closed parenthesis for " + memberWord);
						}

						// Field
						if (memberWord.equals("*")) {
							targetClass.acceptDefaultField(new TransformedWildcardMember(accessLevel, finalization));
						} else {
							targetClass.acceptField(new TransformedField(targetClassName, memberWord, accessLevel, finalization));
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new ManifestParseException("Failed to parse mod access transformer: ", ex);
		}

		return new ForgeAccessTransformer(new HashSet<>(classes.values()));
	}

	private static Finalization getFinalization(String modifier) {
		if (modifier.contains("+f")) {
			return Finalization.ADD;
		} else if (modifier.contains("-f")) {
			return Finalization.REMOVE;
		} else {
			return Finalization.KEEP;
		}
	}

	private static AccessLevel getAccessLevel(String modifier, Finalization finalization) {
		if (finalization != Finalization.REMOVE) {
			modifier = modifier.substring(0, modifier.length() - 2);
		}

		return AccessLevel.valueOf(modifier.toUpperCase());
	}
}
