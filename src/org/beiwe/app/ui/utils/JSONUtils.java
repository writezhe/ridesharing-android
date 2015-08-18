package org.beiwe.app.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

//TODO: Low priority.  Eli/Josh. There are probably some more code snippets (json renderer? audio recorder activity?) that could be thrown in here.

public class JSONUtils {
	public static List<String> jsonArrayToStringList( JSONArray array ) {
		ArrayList<String> ret = new ArrayList<String>(array.length() );
		for (int i=0; i < array.length(); i++) { //Wow, JSONArrays are not iterable.
			try { ret.add( array.getString(i) ); } //uhg, json exceptions...
			catch (JSONException e) { throw new NullPointerException("unpacking json array failed, json string was: " + array.toString() ); }
		}
		return ret;
	}
	
	public static ArrayList<Integer> jsonArrayToIntegerList( JSONArray array ) {
		ArrayList<Integer> ret = new ArrayList<Integer>(array.length() );
		for (int i=0; i < array.length(); i++) { //Wow, JSONArrays are not iterable.
			try { ret.add( array.getInt(i) ); } //uhg, json exceptions...
			catch (JSONException e) { throw new NullPointerException("unpacking json array failed, json string was: " + array.toString() ); }
		}
		return ret;
	}
}
