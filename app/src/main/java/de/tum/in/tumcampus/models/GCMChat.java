package de.tum.in.tumcampus.models;

/**
 * Used for parsing the GCM json payload for the 'chat' type
 */
@SuppressWarnings("UnusedDeclaration")
public class GCMChat {
	public int room;
	public int member;
	public int message;
}
