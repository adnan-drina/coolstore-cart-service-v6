package com.demo.model;

public class Promotion {

	private String itemId;
	
	private double percentOff;

	public Promotion() {
		// Default constructor required for serialization and framework usage
	}
	
	public Promotion(String itemId, double percentOff) {
		super();
		this.itemId = itemId;
		this.percentOff = percentOff;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public double getPercentOff() {
		return percentOff;
	}

	public void setPercentOff(double percentOff) {
		this.percentOff = percentOff;
	}

	@Override
	public String toString() {
		return "Promotion [itemId=" + itemId + ", percentOff=" + percentOff
				+ "]";
	}
	
}
