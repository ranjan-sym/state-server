package net.symplifier.lib.persistence.state.utils;

import net.symplifier.lib.persistence.state.State;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by ranjan on 11/15/15.
 */
public class List {
  public static Object toJSON(java.util.List<? extends State.Entity> list) {
    if (list == null) {
      return JSONObject.NULL;
    }

    JSONArray res = new JSONArray();
    for(State.Entity e:list) {
      res.put(e.getId());
    }
    return res;
  }
}
