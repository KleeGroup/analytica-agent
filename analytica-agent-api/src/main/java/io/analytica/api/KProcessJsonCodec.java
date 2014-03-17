/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 */
package io.analytica.api;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * Codec used to convert KProcess to Json and Json to KProcess.  
 * @author pchretien, npiedeloup
 */
public final class KProcessJsonCodec {

	private static final KProcessDeserializer PROCESS_DESERIALIZER = new KProcessDeserializer();

	/**
	 * Convert a KProcess list to Json String.
	 * @param processes Process list
	 * @return Json String
	 */
	public static String toJson(final List<KProcess> processes) {
		return new Gson().toJson(processes);
	}

	/**
	 * Convert a Json String to KProcess list.
	 * @param json Json string
	 * @return Process list
	 */
	public static List<KProcess> fromJson(final String json) {
		final Gson gson = new GsonBuilder().registerTypeAdapter(KProcess.class, PROCESS_DESERIALIZER).create();
		return gson.fromJson(json, KProcessDeserializer.LIST_PROCESS_TYPE);
	}

	/**
	 * Gson deserializer for KProcess Object.
	 * @author npiedeloup
	 */
	static final class KProcessDeserializer implements JsonDeserializer<KProcess> {

		/**
		 * Type List<KProcess>.
		 */
		public static final Type LIST_PROCESS_TYPE = new TypeToken<List<KProcess>>() { //empty
		}.getType();
		private static final Type MAP_STRING_DOUBLE_TYPE = new TypeToken<Map<String, Double>>() { //empty
		}.getType();
		private static final Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>() { //empty
		}.getType();
		private static final String[] EMPTY_STRING_ARRAY = new String[0];

		/** {@inheritDoc} */
		public KProcess deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			//{"type":"COMMANDE","subTypes":["5 Commandes"],"startDate":"Mar 12, 2014 2:37:48 PM",
			// "measures":{"sub-duration":3.0,"duration":4.0},"metaDatas":{},"subProcesses":[]}
			final JsonObject jsonObject = json.getAsJsonObject();

			final JsonPrimitive jsonType = jsonObject.getAsJsonPrimitive("type");
			final String type = jsonType.getAsString();

			final JsonArray jsonSubTypes = jsonObject.getAsJsonArray("subTypes");
			final String[] subTypes = deserialize(context, jsonSubTypes, String[].class, EMPTY_STRING_ARRAY);

			final JsonPrimitive jsonStartDate = jsonObject.getAsJsonPrimitive("startDate");
			final Date startDate = context.deserialize(jsonStartDate, Date.class);

			final JsonObject jsonMeasures = jsonObject.getAsJsonObject("measures");
			final Map<String, Double> measures = deserialize(context, jsonMeasures, MAP_STRING_DOUBLE_TYPE, Collections.<String, Double> emptyMap());

			final JsonObject jsonMetaDatas = jsonObject.getAsJsonObject("metaDatas");
			final Map<String, String> metaDatas = deserialize(context, jsonMetaDatas, MAP_STRING_STRING_TYPE, Collections.<String, String> emptyMap());

			final JsonArray jsonSubProcesses = jsonObject.getAsJsonArray("subProcesses");
			final List<KProcess> processes = deserialize(context, jsonSubProcesses, LIST_PROCESS_TYPE, Collections.<KProcess> emptyList());

			return new KProcess(type, subTypes, startDate, measures, metaDatas, processes);
		}

		private static <O> O deserialize(final JsonDeserializationContext context, final JsonElement jsonElement, final Type typeOf, final O defaultValue) {
			if (jsonElement != null) {
				return context.deserialize(jsonElement, typeOf);
			} else {
				return defaultValue;
			}
		}
	}
}
