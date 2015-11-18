import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MRIEntry implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2557689469770990245L;
	
	private String title;
	private String description;
	private String author;
	private String date;
	private String content;
	private String url_src;
	private String txt_src;
	private String language;
	private String copyright;
	private String hash;
	private MessageDigest md;
	
	public MRIEntry(String title, String description, String content, String author, String date, String url_src, String txt_src,
			String language, String copyright) {
		super();
		this.title = title;
		this.description = description;
		this.author = author;
		this.date = date;
		this.url_src = url_src;
		this.txt_src = txt_src;
		this.language = language;
		this.copyright = copyright;
		this.content=content;
		try {
			md = MessageDigest.getInstance("MD5");
			//cryptage en md5. Cle choisie titre + url + langue
			byte[] toBytes = md.digest((title+url_src+language).getBytes());
			hash = toBytes.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public MRIEntry(String hash){
		
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUrl_src() {
		return url_src;
	}
	public void setUrl_src(String url_src) {
		this.url_src = url_src;
	}
	public String getTxt_src() {
		return txt_src;
	}
	public void setTxt_src(String txt_src) {
		this.txt_src = txt_src;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString(){
		String ret = "";
		
		ret += "{Hash : "+this.getHash()+"}";
		ret += "{Title : "+this.getTitle();
		ret += "\tLanguage : "+this.getLanguage();
		ret += "\tLink : "+this.getUrl_src();
//		ret += "\tDesc : "+this.getDescription();
		ret += "\tAuthor : "+this.getAuthor();
		ret += "\tText : "+this.getContent();
		ret += "\tSources : "+this.getTxt_src();
		ret += "\tDate : "+this.getDate();
		ret += "\tCopyright : "+this.getCopyright()+"}";
		
		return ret;
	}
	
	public String toXMLString(){
		String ret = "<article>\n";
		
		ret += "\t<name>"+this.getHash()+"</name>\n";
		ret += "\t<title>"+this.getTitle()+"</title>\n";
		ret += "\t<language>"+this.getLanguage()+"</language>\n";
		ret += "\t<url_src>"+this.getUrl_src()+"</url_src>\n";
		ret += "\t<description>"+this.getDescription()+"</description>\n";
		ret += "\t<author>"+this.getAuthor()+"</author>\n";
		ret += "\t<content>"+this.getContent()+"</content>\n";
		ret += "\t<txt_src>"+this.getTxt_src()+"</txt_src>\n";
		ret += "\t<date>"+this.getDate()+"</date>\n";
		ret += "\t<copyright>"+this.getCopyright()+"</copyright>\n";
		
		ret += "</article>\n\n";
		
		return ret;
	}
	
}