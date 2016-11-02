package de.tum.in.tumcampusapp.models.cafeteria;

import android.support.annotation.NonNull;

/**
 * Cafeteria Object
 */
public class Cafeteria implements Comparable<Cafeteria> {
	/**
	 * Address, e.g. Boltzmannstr. 3
	 */
	public final String address;

	/**
	 * Cafeteria ID, e.g. 412
	 */
	public final int id;

	/**
	 * Name, e.g. MensaX
	 */
	public final String name;

    public final double latitude;
    public final double longitude;

    // Used for ordering cafeterias
    public float distance;

	/**
	 * new Cafeteria
	 *
	 * @param id Cafeteria ID, e.g. 412
	 * @param name Name, e.g. MensaX
	 * @param address Address, e.g. Boltzmannstr. 3
     * @param latitude Coordinates of the cafeteria
     * @param longitude Coordinates of the cafeteria
	 */
	public Cafeteria(int id, String name, String address, double latitude, double longitude) {
		this.id = id;
		this.name = name;
		this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
	}

	@Override
	public String toString() {
		return name;
	}

    @Override
    public int compareTo(@NonNull Cafeteria cafeteria) {
        return distance<cafeteria.distance?-1:1;
    }
}