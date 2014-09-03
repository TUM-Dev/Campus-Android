package de.tum.in.tumcampus.models;

/**
 * Cafeteria Object
 */
public class Cafeteria {
	/**
	 * Address, e.g. Boltzmannstr. 3
	 */
	public String address;

	/**
	 * Cafeteria ID, e.g. 412
	 */
	public int id;

	/**
	 * Name, e.g. MensaX
	 */
	public String name;

    public double latitude;
    public double longitude;

	/**
	 * new Cafeteria
	 * 
	 * <pre>
	 * @param id Cafeteria ID, e.g. 412
	 * @param name Name, e.g. MensaX
	 * @param address Address, e.g. Boltzmannstr. 3
	 * </pre>
	 */
	public Cafeteria(int id, String name, String address) {
		this.id = id;
		this.name = name;
		this.address = address;
	}

	@Override
	public String toString() {
		return "id=" + this.id + " name=" + this.name + " address="
				+ this.address;
	}
}