package com.naizo.elementals.net;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naizo.elementals.core.TreeCodec;

/**
 * Remnants sends JSON messages over one channel per direction. Each message is an object with a
 * "t" (type) field. The largest one, the skill tree, stays well under the payload limits.
 */
public final class Net {
    public static final int MAX_LENGTH = 4_000_000;

    // server to client
    public static final String INIT = "init";
    public static final String PROFILE = "profile";
    public static final String ESSENCE = "essence";
    public static final String NOTICE = "notice";
    public static final String CAST_FX = "cast";
    // client to server
    public static final String HELLO = "hello";
    public static final String COMMIT = "commit";
    public static final String RESET = "reset";
    public static final String SLOT = "slot";
    public static final String USE = "use";

    private Net() {
    }

    public static JsonObject message(String type) {
        JsonObject o = new JsonObject();
        o.addProperty("t", type);
        return o;
    }

    public static String write(JsonObject message) {
        return TreeCodec.compact().toJson(message);
    }

    public static JsonObject read(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    public static String type(JsonObject message) {
        return message.has("t") ? message.get("t").getAsString() : "";
    }
}
