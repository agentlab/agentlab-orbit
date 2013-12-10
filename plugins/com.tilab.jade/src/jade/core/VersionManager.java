package jade.core;

class VersionManager {
	
	public String getVersion() {
		String version = "4.3.1"; // The $ surrounded Version keyword is automatically replaced by the target doTag of build.xml
		return version;
	}
	
	public String getRevision() {
		String revision = "6695"; // The $ surrounded WCREV keyword is automatically replaced by WCREV with subversion
		return revision;
	}
	
	public String getDate() {
		String date = "2013/12/03 14:41:54"; // The $ surrounded WCDATE keyword is automatically replaced by WCREV with subversion
		return date;
	}
}
