package com.patchworkmc.manifest.accesstransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.patchworkmc.manifest.api.Remapper;
import com.patchworkmc.manifest.mod.ManifestParseException;

public class AccessTransformerList {
	private List<AccessTransformerEntry> entries;

	public AccessTransformerList(List<AccessTransformerEntry> entries) {
		this.entries = entries;
	}

	public static AccessTransformerList parse(Path accessTransformer) throws ManifestParseException {
		List<String> lines;

		try {
			lines = Files.readAllLines(accessTransformer);
		} catch (IOException ex) {
			// File can't be read (most likely doesn't exist), return a blank list
			return new AccessTransformerList(new ArrayList<>());
		}

		List<AccessTransformerEntry> entries = new ArrayList<>();

		// ATs are formatted like this:
		// public-f com/example/Foo bar # mars
		// We can make everything public and definalized and let the mod just live in its own world
		for (String line : lines) {
			//Get everything before a comment and throw the rest away
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			int index = line.indexOf('#');

			if (index != -1) {
				line = line.substring(0, index).trim();
			}

			if (line.contains("*")) {
				throw new UnsupportedOperationException("Wildcards are not supported");
			}

			String[] words = line.replace("\\", "/").split(" ");
			switch (words.length) {
			// public-f com/example/Foo (class)
			case 2:
				throw new UnsupportedOperationException("Classes are not supported");
			// public-f com/example/Foo bar (member)
			case 3:
				entries.add(new AccessTransformerEntry(words[1], words[2]));
				break;
			default:
				throw new ManifestParseException("access transformer line had too few/too many parts: expects 2/3/4, got " + words.length);
			}
		}

		return new AccessTransformerList(entries);
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
