package net.patchworkmc.manifest.accesstransformer.v2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.patchworkmc.manifest.accesstransformer.v2.exception.MissingMappingException;
import net.patchworkmc.manifest.accesstransformer.v2.flags.AccessLevel;
import net.patchworkmc.manifest.accesstransformer.v2.flags.Finalization;
import net.patchworkmc.manifest.api.Remapper;
import net.patchworkmc.manifest.mod.ManifestParseException;

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
	public void remap(Remapper remapper, Consumer<MissingMappingException> errorLogger) {
		Set<TransformedClass> remappedClasses = new HashSet<>();

		for (TransformedClass transformedClass : this.classes) {
			try {
				remappedClasses.add(transformedClass.remap(remapper, errorLogger));
			} catch (MissingMappingException ex) {
				errorLogger.accept(ex);
			}
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
				int commentIndex = line.indexOf('#');
				String strippedLine = "";

				if (commentIndex != -1) {
					strippedLine = line.substring(0, line.indexOf('#'));
				} else {
					strippedLine = line;
				}

				// Trim any leading or trailing whitespace
				strippedLine = strippedLine.trim();

				if (strippedLine.isEmpty()) {
					continue;
				}

				// Remove empty substrings since they represent whitespace
				List<String> words = Arrays.stream(strippedLine.split(" "))
						.filter(string -> !string.isEmpty())
						.collect(Collectors.toList());

				// make sure our length is correct
				if (words.size() < 2 || words.size() > 3) {
					throw new ManifestParseException("Invalid AT line: expected size of 2 or 3, got " + words.size());
				}

				String modifier = words.get(0);
				Finalization finalization = getFinalization(modifier);
				AccessLevel accessLevel = getAccessLevel(modifier, finalization);

				String targetClassName = words.get(1);

				TransformedClass targetClass = classes.computeIfAbsent(targetClassName, name -> new TransformedClass(name, Finalization.KEEP, AccessLevel.KEEP));

				if (words.size() == 2) {
					if (targetClass.getFinalization() != Finalization.KEEP || targetClass.getAccessLevel() != AccessLevel.KEEP) {
						throw new ManifestParseException("two transformations of the same class!");
					}

					// It's possible for an access transformation to a field within a class to be stated before an
					// access transformation on the class itself, therefore we have to be able to change the access of
					// the TransformedClass even after it has been created. Unfortunately it means that we can't keep
					// the fields final, but oh well.
					targetClass.setFinalization(finalization);
					targetClass.setAccessLevel(accessLevel);
				} else {
					// Method or field
					String memberWord = words.get(2);

					if (memberWord.equals("*()")) {
						targetClass.acceptMethodWildcard(new TransformedWildcardMember(accessLevel, finalization));
					} else if (memberWord.contains("(") && memberWord.contains(")")) {
						// Method
						String methodName = memberWord.substring(0, memberWord.indexOf('('));
						String methodDescriptor = memberWord.substring(memberWord.indexOf('(', memberWord.indexOf(')' + 1)));

						targetClass.acceptMethod(new TransformedMethod(targetClassName, methodName, methodDescriptor, accessLevel, finalization));
					} else {
						// Fail hard for malformed member entries
						if (memberWord.contains("(") || memberWord.contains(")")) {
							throw new ManifestParseException("unopened/closed parenthesis for " + memberWord);
						}

						// Field
						if (memberWord.equals("*")) {
							targetClass.acceptFieldWildcard(new TransformedWildcardMember(accessLevel, finalization));
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
		if (finalization != Finalization.KEEP) {
			modifier = modifier.substring(0, modifier.length() - 2);
		}

		return AccessLevel.valueOf(modifier.toUpperCase());
	}
}
