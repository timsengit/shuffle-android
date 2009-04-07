package org.dodgybits.android.shuffle.util;

import org.dodgybits.android.shuffle.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

public class TextColours {
	private static final String cTag = "TextColours";
	private static TextColours instance = null;
	
	private int[] textColours;
	private int[] bgColours;

	public static TextColours getInstance(Context context) {
		if (instance == null) {
			instance = new TextColours(context);
		}
		return instance;
	}

	private TextColours(Context context) {
		Log.d(cTag, "Fetching colours");
		String[] colourStrings = context.getResources().getStringArray(R.array.text_colours);
		Log.d(cTag, "Fetched colours");
		textColours = parseColourString(colourStrings);
		colourStrings = context.getResources().getStringArray(R.array.background_colours);
		bgColours = parseColourString(colourStrings);
	}
	
	private int[] parseColourString(String[] colourStrings) {
		int[] colours = new int[colourStrings.length];
		for (int i = 0; i < colourStrings.length; i++) {
			String colourString = '#' + colourStrings[i].substring(1);
			Log.d(cTag, "Parsing " + colourString);
			colours[i] = Color.parseColor(colourString);
		}
		return colours;
	}
	
	public int getNumColours() {
		return textColours.length;
	}
	
	public int getTextColour(int position) {
		return textColours[position];
	}
	
	public int getBackgroundColour(int position) {
		return bgColours[position];
	}
	
}
