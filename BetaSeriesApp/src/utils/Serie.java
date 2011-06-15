package utils;

public class Serie {
	private String name;
	private boolean archive;

	public Serie(String name) {
		super();
		this.name = name;
	}
	
	
	
	public Serie(String name, boolean archive) {
		super();
		this.name = name;
		this.archive = archive;
	}

	

	public boolean isArchive() {
		return archive;
	}



	public String getName() {
		return name;
	}
	
}
