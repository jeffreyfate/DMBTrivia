package com.jeffthefate.dmbquiz;

public class MenuRow {
	private String menuName;
	private int menuNumber;
	private int menuImage;
	
	public MenuRow(String menuName, int menuNumber, int menuImage) {
		this.menuName = menuName;
		this.menuNumber = menuNumber;
		this.menuImage = menuImage;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	public int getMenuNumber() {
		return menuNumber;
	}
	
	public int getMenuImage() {
		return menuImage;
	}
}