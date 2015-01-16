package net.boatcake.MyWorldGen.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.Schematic;
import net.boatcake.MyWorldGen.SchematicInfo;
import net.boatcake.MyWorldGen.WorldGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ResourceManagerListener implements IResourceManagerReloadListener {
	private WorldGenerator worldGen;
	private static final Gson gsonReader = (new GsonBuilder())
			.registerTypeAdapter(SchematicInfo.class,
					new SchematicListSerializer()).create();
	private static final ParameterizedType paramType = new ParameterizedType() {
		@Override
		public Type[] getActualTypeArguments() {
			return new Type[] { String.class, SchematicInfo.class };
		}

		@Override
		public Type getRawType() {
			return Map.class;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}
	};

	public ResourceManagerListener(WorldGenerator worldGen) {
		this.worldGen = worldGen;
	}

	public void register() {
		((SimpleReloadableResourceManager) Minecraft.getMinecraft()
				.getResourceManager()).registerReloadListener(this);
	}

	@Override
	public void onResourceManagerReload(IResourceManager manager) {
		worldGen.resourcePackSchemList.clear();
		Set<String> domains = manager.getResourceDomains();
		for (String domain : domains) {
			try {
				List<IResource> indexes = manager
						.getAllResources(new ResourceLocation(domain,
								"worldgen.json"));
				int count = 0;
				for (IResource jsonResource : indexes) {
					try {
						Map<String, SchematicInfo> indexJson = gsonReader
								.fromJson(
										new InputStreamReader(jsonResource
												.getInputStream()), paramType);
						Set<Entry<String, SchematicInfo>> indexEntries = indexJson
								.entrySet();

						for (Entry<String, SchematicInfo> entry : indexEntries) {
							ResourceLocation loc = new ResourceLocation(domain,
									"worldgen/" + entry.getKey() + ".schematic");
							IResource schemResource = manager.getResource(loc);
							Schematic newSchem = new Schematic(
									CompressedStreamTools.readCompressed(schemResource
											.getInputStream()), entry.getKey());
							// Warning: Overrides anything set in the schematic!
							// Maybe make SchematicInfo use Optionals so that we
							// can override them
							newSchem.info = entry.getValue();
							newSchem.info.name = entry.getKey();
							worldGen.resourcePackSchemList.add(newSchem);
							count++;
						}
					} catch (RuntimeException runtimeexception) {
						MyWorldGen.log.warn("Invalid worldgen.json",
								runtimeexception);
					}
					MyWorldGen.log.log(Level.INFO,
							"Loaded {} schematics from {}", count,
							jsonResource.toString());
				}
			} catch (IOException ioexception) {
				;
			}
		}
	}
}
