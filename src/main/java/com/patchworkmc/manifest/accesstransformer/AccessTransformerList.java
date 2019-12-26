package com.patchworkmc.manifest.accesstransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.patchworkmc.manifest.api.Remapper;

public class AccessTransformerList {
	private List<AccessTransformerEntry> entries;

	public AccessTransformerList(List<AccessTransformerEntry> entries) {
		this.entries = entries;
	}

	public static AccessTransformerList parse(Path accessTransformer) {
		List<String> lines;

		try {
			lines = accessTransformer.toFile().exists()
					? Files.readAllLines(accessTransformer)
					: new ArrayList<>();
		} catch (IOException ex) {
			return null;
		}

		List<AccessTransformerEntry> entries = new ArrayList<>();

		// ATs are formatted like this:
		// public-f com/example/Foo bar # mars
		// We can make everything public and definalized and let the mod just live in its own world,
		// so we only use the 2nd and 3rd words.
		for (String line : lines) {
			// Put everything into the map. Changes package "." to folder "/".
			// regex.
			String[] split = line.replaceAll("\\.", "/").split(" ");

			// If the line is at least two words AND none of those words contains a comment
			// symbol
			if (split.length < 2) {
				continue;
			}

			String combined = (split[0] + split[1] + getOrEmpty(split, 2));

			if (!combined.contains("#")) {
				// if it's only two words and not three we've got a class modifier
				if (getOrEmpty(split, 2).equals("") || getOrEmpty(split, 2).contains("#")) {
					throw new UnsupportedOperationException(combined + ": Transforming classes is unsupported");
					// note: we know that index 2 is part of the AT and not a comment from above
				} else if (combined.contains("*")) {
					throw new UnsupportedOperationException(combined + ": Wildcards are unsupported");
				}

				entries.add(new AccessTransformerEntry(split[1], split[2]));
			}
		}

		return new AccessTransformerList(entries);
	}

	private static String getOrEmpty(String[] array, int index) {
		try {
			return array[index];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return "";
		}
	}

	public List<AccessTransformerEntry> getEntries() {
		return entries;
	}

	public AccessTransformerList remap(Remapper remapper) {
		List<AccessTransformerEntry> newEntries = new ArrayList<>();
		entries.forEach(e -> newEntries.add(e.remap(remapper)));
		entries.clear();
		entries.addAll(newEntries);
		return this;
	}
}
