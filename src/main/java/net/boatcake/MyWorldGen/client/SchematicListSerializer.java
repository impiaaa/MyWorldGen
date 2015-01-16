package net.boatcake.MyWorldGen.client;

import java.lang.reflect.Type;
import java.util.ArrayList;

import net.boatcake.MyWorldGen.SchematicInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SchematicListSerializer implements JsonDeserializer<SchematicInfo> {

	@Override
	public SchematicInfo deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		SchematicInfo info = new SchematicInfo();
		JsonObject jsonobject = json.getAsJsonObject();
		if (jsonobject.has("chestType")) {
			info.chestType = jsonobject.get("chestType").getAsString();
		}

		if (jsonobject.has("excludeBiomes")) {
			JsonArray l = jsonobject.get("excludeBiomes").getAsJsonArray();
			info.excludeBiomes = new ArrayList<String>(l.size());
			for (JsonElement el : l) {
				info.excludeBiomes.add(el.getAsString());
			}
		}

		if (jsonobject.has("onlyIncludeBiomes")) {
			JsonArray l = jsonobject.get("onlyIncludeBiomes").getAsJsonArray();
			info.onlyIncludeBiomes = new ArrayList<String>(l.size());
			for (JsonElement el : l) {
				info.onlyIncludeBiomes.add(el.getAsString());
			}
		}

		if (jsonobject.has("lockRotation")) {
			info.lockRotation = jsonobject.get("lockRotation").getAsBoolean();
		}

		if (jsonobject.has("randomWeight")) {
			info.randomWeight = jsonobject.get("randomWeight").getAsInt();
		}

		if (jsonobject.has("generateSpawners")) {
			info.generateSpawners = jsonobject.get("generateSpawners")
					.getAsBoolean();
		}

		if (jsonobject.has("fuzzyMatching")) {
			info.fuzzyMatching = jsonobject.get("fuzzyMatching").getAsBoolean();
		}

		if (jsonobject.has("terrainSmoothing")) {
			info.terrainSmoothing = jsonobject.get("terrainSmoothing")
					.getAsBoolean();
		}
		return info;
	}

}
