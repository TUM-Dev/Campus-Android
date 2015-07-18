package de.tum.in.tumcampus.models;

/**
 * Used for parsing the GCM json payload for the 'update' type
 */
@SuppressWarnings("UnusedDeclaration")
public class GCMUpdate {

	public int packageVersion;
	public int sdkVersion;
	public String releaseDate;
	public int lowestVersion;

}
