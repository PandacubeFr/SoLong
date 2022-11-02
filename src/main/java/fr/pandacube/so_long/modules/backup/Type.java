package fr.pandacube.so_long.modules.backup;

public enum Type {
	WORLDS,
	OTHERS;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
	
}
