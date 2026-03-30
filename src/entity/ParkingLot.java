package entity;
//Entity: The main parking facility
public class ParkingLot {
	private String lotID;
    private String lotName;
    private String address;
    private String city;
    private int availableSpaces;

    public ParkingLot(String id, String name, String addr, String city, int spaces) {
        this.lotID = id;
        this.lotName = name;
        this.address = addr;
        this.city = city;
        this.availableSpaces = spaces;
    }

	public String getLotID() {
		return lotID;
	}

	public String getLotName() {
		return lotName;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public int getAvailableSpaces() {
		return availableSpaces;
	}

	public void setLotID(String lotID) {
		this.lotID = lotID;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setAvailableSpaces(int availableSpaces) {
		this.availableSpaces = availableSpaces;
	}

}
