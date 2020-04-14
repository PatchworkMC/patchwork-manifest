package com.patchworkmc.manifest.accesstransformer.v2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
	}

	public Set<TransformedClass> getClasses() {
		return Collections.unmodifiableSet(classes);
	}

	public static ForgeAccessTransformer of(Path path) throws ManifestParseException {
		HashMap<String, TransformedClass> classes = new HashMap<>();

		try {
			List<String> lines;
			lines = Files.readAllLines(path);

			for (String line : lines) {
				// List of all words in this line with comments stripped
				List<String> words = removeCommentsFromLine(Arrays.asList(line.split(" ")));

				if (words.isEmpty()) {
					continue;
				}

				// make sure our length is correct
				if (words.size() < 2 || words.size() > 3) {
					throw new ManifestParseException("expected size of 2 or 3, got " + words.size());
				}

				AccessLevel accessLevel = getAccessLevel(words.get(0));
				Finalization finalization = getFinalization(words.get(0));
				String targetClassName = words.get(1);

				if (words.size() == 2) {
					if (classes.containsKey(targetClassName)) {
						throw new ManifestParseException("two transformations of the same class!");
					}

					classes.put(targetClassName, new TransformedClass(targetClassName, finalization, accessLevel));
				} else {
					// Method or field
					TransformedClass targetClass;

					if (classes.containsKey(targetClassName)) {
						targetClass = classes.get(targetClassName);
					} else {
						targetClass = classes.put(targetClassName, new TransformedClass(targetClassName, finalization, accessLevel));
					}

					String memberWord = words.get(3);

					if (memberWord.contains("(") && memberWord.contains(")")) {
						// Method
						String methodName = memberWord.substring(0, memberWord.indexOf('('));
						String methodDescriptor = memberWord.substring(memberWord.indexOf('(', memberWord.indexOf(')' + 1)));

						if (methodName.equals("*")) {
							targetClass.acceptDefaultMethod(new TransformedNameless(accessLevel, finalization));
						} else {
							targetClass.acceptMethod(new TransformedMethod(targetClassName, methodName, "(" + methodDescriptor, accessLevel, finalization));
						}
					} else {
						if (memberWord.contains("(") || memberWord.contains(")")) {
							throw new ManifestParseException("unopened/closed parenthesis for " + memberWord);
						}

						// Field
						if (memberWord.equals("*")) {
							targetClass.acceptDefaultField(new TransformedNameless(accessLevel, finalization));
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

	private static AccessLevel getAccessLevel(String modifier) {
		// Remove -f and +f
		String accessLevel = modifier.split("\\+")[0].split("-")[0];
		return AccessLevel.valueOf(accessLevel.toUpperCase());
	}

	private static List<String> removeCommentsFromLine(List<String> words) {
		List<String> newWords = new ArrayList<>();

		for (String word : words) {
			if (word.contains("#")) {
				newWords.add(word.substring(0, word.indexOf('#')));
			} else {
				newWords.add(word);
			}
		}

		return newWords;
	}
}
