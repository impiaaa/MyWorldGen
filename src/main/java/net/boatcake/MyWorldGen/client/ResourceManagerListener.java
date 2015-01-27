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
import net.boatcake.MyWorldGen.WorldGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@SideOnly(Side.CLIENT)
public class ResourceManagerListener implements IResourceManagerReloadListener {
	private WorldGenerator worldGen;
	private static final Gson gsonReader = (new GsonBuilder())
			.registerTypeAdapter(JsonObject.class, new StupidDeserializer())
			.create();
	private static final ParameterizedType paramType = new ParameterizedType() {
		@Override
		public Type[] getActualTypeArguments() {
			return new Type[] { String.class, JsonObject.class };
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
						Map<String, JsonObject> indexJson = gsonReader
								.fromJson(
										new InputStreamReader(jsonResource
												.getInputStream()), paramType);
						Set<Entry<String, JsonObject>> indexEntries = indexJson
								.entrySet();

						for (Entry<String, JsonObject> entry : indexEntries) {
							ResourceLocation loc = new ResourceLocation(domain,
									"worldgen/" + entry.getKey() + ".schematic");
							IResource schemResource = manager.getResource(loc);
							Schematic newSchem = new Schematic(
									CompressedStreamTools.readCompressed(schemResource
											.getInputStream()), entry.getKey());
							// Read the JSON *after* the NBT so that it
							// overrides properly
							newSchem.info.readFromJson(entry.getValue());
							worldGen.resourcePackSchemList.add(newSchem);
							count++;
						}
					} catch (RuntimeException runtimeexception) {
						MyWorldGen.log.warn("Invalid worldgen.json",
								runtimeexception);
					}
					MyWorldGen.log.log(Level.INFO,
							"Loaded {} schematics from {}", count,
							jsonResource.func_177240_d());
				}
			} catch (IOException ioexception) {
				;
			}
		}
	}
}
