package net.symplifier.lib.persistence.state.utils;

import net.symplifier.lib.persistence.state.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * Created by ranjan on 11/17/15.
 */
public class StateJsoniser {

  private final State state;

  public StateJsoniser(State state) {
    this.state = state;
  }


  public State getState() {
    return state;
  }

  public JSONObject toJSON() {
    JSONObject res = new JSONObject();

    JSONArray state = new JSONArray();
    res.put("state", state);

    for(String name:this.state.getLists()) {
      JSONObject o = new JSONObject();
      o.put("name", name);
      ElementList<? extends State.Element> items = this.state.getList(name);
      ElementType type = items.getType();
      o.put("type", type.getName());
      JSONArray list = new JSONArray();
      for(State.Element e:items) {
        list.put(e.getId());
      }
      o.put("items", list);

      state.put(o);
    }

    JSONArray oracle = new JSONArray();
    res.put("oracle", oracle);
    Map<ElementType, Set<ElementProxy>> all = this.state.getAll();
    for(Map.Entry<ElementType, Set<ElementProxy>> entry: all.entrySet()) {
      JSONObject o = new JSONObject();
      oracle.put(o);
      o.put("type", entry.getKey().getName());
      JSONArray structure = new JSONArray();
      o.put("structure", structure);
      Map<String, ValueType> struct = entry.getKey().getStructure();
      for(Map.Entry<String, ValueType> structEntry: struct.entrySet()) {
        JSONObject field = new JSONObject();
        structure.put(field);
        field.put(structEntry.getKey(), structEntry.getValue().getName());
      }

      JSONObject data = new JSONObject();
      o.put("data", data);
      for(ElementProxy e:entry.getValue()) {
        JSONArray record = new JSONArray();
        data.put(e.getId().toString(), record);

        int idx = 0;
        for(ValueType v:struct.values()) {
          Object value = e.getValue(idx++);
          if (v.isElement()) {
            if (value == null) {
              record.put(JSONObject.NULL);
            } else {
              record.put(((ElementProxy) value).getId());
            }
          } else if(v.isList()) {
            JSONArray list = new JSONArray();
            for(ElementProxy ch:((ElementListProxy)value).getElements()) {
              list.put(ch.getId());
            }
            record.put(list);
          } else {
            record.put(value);
          }
        }
      }

    }




    // Put the entire oracle for this state into a object
    return res;
  }




}
