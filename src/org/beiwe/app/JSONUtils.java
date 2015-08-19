package org.beiwe.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

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

	//this is the hackiest...
	public static JSONArray stringListToJSONArray( List<String> list ) {
		try { return new JSONArray(list.toString()); }
		catch (JSONException e) {
			try { Log.e("JSONUtils", "a list could not be converted to json");
				e.printStackTrace();
				return new JSONArray( new ArrayList<String>().toString() ); }
			catch (JSONException e1) { throw new NullPointerException("The syntax of the toString function for arraylists is incorrect"); }
		}
	}
	
	public static JSONArray shuffleJSONArray(JSONArray jsonArray, int numberElements) {
		List<String> javaList = JSONUtils.jsonArrayToStringList(jsonArray);
		Collections.shuffle(javaList, new Random(System.currentTimeMillis()) );
		//if length supplied is 0 or greater than number of elements...
		if (numberElements == 0 || numberElements > javaList.size() ) { jsonArray = JSONUtils.stringListToJSONArray(javaList); }
		else { jsonArray = JSONUtils.stringListToJSONArray(javaList.subList(0, numberElements)); }
		return jsonArray;
	}
	
}
