package de.budde.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestHelper {
    /**
     * convert a string to a JSON object
     *
     * @param json JSON object as string. But, for convenience, with ' where " is expected
     * @return new JSON object after ' has been replaced by "
     * @throws JSONException
     */
    public static JSONObject jo(String json) throws JSONException {
        return new JSONObject(json.replaceAll("'", "\""));
    }

    /**
     * convert a string to a JSON array
     *
     * @param json JSON array as string. But, for convenience, with ' where " is expected
     * @return new JSON array after ' has been replaced by "
     * @throws JSONException
     */
    public static JSONArray ja(String json) throws JSONException {
        return new JSONArray(json.replaceAll("'", "\""));
    }
}
