package net.patchworkmc.manifest.mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

/**
 * Parsed representation of a mods.toml file.
 */
public class ModManifest {
	private String modLoader;
	private String loaderVersion;
	private boolean showAsResourcePack;
	private Map<String, Object> properties;
	private String issueTrackerUrl;
	private String logoFile;
	private List<ModManifestEntry> mods;
	private Map<String, List<ModManifestDependency>> dependencyMapping;

	private ModManifest() {
		mods = new ArrayList<>();
		dependencyMapping = new HashMap<>();
	}

	/**
	 * Parses a TOML object into a ModManifest.
	 *
	 * @param data A Map containing the parsed form of the TOML object
	 * @return A ModManifest parsed from the specified TOML object
	 * @throws ManifestParseException if one of the keys has a wrong data type or a required key is
	 *         missing
	 */
	public static ModManifest parse(Map<String, Object> data) throws ManifestParseException {
		ModManifest manifest = new ModManifest();

		try {
			manifest.modLoader = ManifestParseHelper.getString(data, "modLoader", true);
			manifest.loaderVersion = ManifestParseHelper.getString(data, "loaderVersion", true);
			manifest.showAsResourcePack = data.get("showAsResourcePack") == Boolean.TRUE;
			manifest.properties = ManifestParseHelper.getMap(data, "properties", false);
			manifest.issueTrackerUrl =
					ManifestParseHelper.getString(data, "issueTrackerURL", false);
			manifest.logoFile = ManifestParseHelper.getString(data, "logoFile", false);

			// Parse the mods list

			List modsRaw = ManifestParseHelper.getList(data, "mods", true);
			Objects.requireNonNull(modsRaw);

			for (Object object : modsRaw) {
				Map<String, Object> map = ManifestParseHelper.toMap(object, "Mod list entry");

				manifest.mods.add(ModManifestEntry.parse(map));
			}

			// Parse the dependency mapping
			Map<String, Object> dependencies = getDependenciesOrFailSilently(data);

			if (dependencies != null) {
				for (Map.Entry<String, Object> dependencySet : dependencies.entrySet()) {
					String key = dependencySet.getKey();
					Object potentialList = dependencySet.getValue();

					if (!(potentialList instanceof List)) {
						throw ManifestParseHelper.typeError(
								"Mod dependency set", potentialList, "List");
					}

					List<ModManifestDependency> dependencyList = new ArrayList<>();

					for (Object object : (List) potentialList) {
						Map<String, Object> map =
								ManifestParseHelper.toMap(object, "Mod list entry");

						dependencyList.add(ModManifestDependency.parse(map));
					}

					manifest.dependencyMapping.put(key, dependencyList);
				}
			}
		} catch (Exception e) {
			throw new ManifestParseException("Failed to parse mod manifest", e);
		}

		if (manifest.properties == null) {
			manifest.properties = new HashMap<>();
		}

		return manifest;
	}

	/**
	 * Tries to obtain the dependencies map from the mod manifest, failing silently on type errors to mimic forge behavior.
	 *
	 * @param data the parsed representation of the mod manifest
	 * @return the dependencies map if it is present and of the right type, null otherwise
	 */
	@Nullable
	private static Map<String, Object> getDependenciesOrFailSilently(Map<String, Object> data) {
		try {
			return ManifestParseHelper.getMap(data, "dependencies", false);
		} catch (ManifestParseException e) {
			// If the dependencies list is of the wrong type, Forge silently ignores the dependency list instead of
			// revealing any sort of error
			//
			// This is needed to properly patch the following mod:
			// https://www.curseforge.com/minecraft/mc-mods/spice-of-life-carrot-edition
			return null;
		}
	}

	public String getModLoader() {
		return modLoader;
	}

	public String getLoaderVersion() {
		return loaderVersion;
	}

	public boolean isShowAsResourcePack() {
		return showAsResourcePack;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Optional<String> getIssueTrackerUrl() {
		return Optional.ofNullable(issueTrackerUrl);
	}

	public Optional<String> getLogoFile() {
		return Optional.ofNullable(logoFile);
	}

	public List<ModManifestEntry> getMods() {
		return mods;
	}

	public Map<String, List<ModManifestDependency>> getDependencyMapping() {
		return dependencyMapping;
	}

	@Override
	public String toString() {
		return "ModManifest{"
				+ "modLoader='" + modLoader + '\'' + ", loaderVersion='" + loaderVersion + '\''
				+ ", showAsResourcePack=" + showAsResourcePack + ", properties=" + properties
				+ ", issueTrackerUrl='" + issueTrackerUrl + '\'' + ", logoFile='" + logoFile + '\''
				+ ", mods=" + mods + ", dependencyMapping=" + dependencyMapping + '}';
	}
}
