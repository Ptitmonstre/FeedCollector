import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

/**
 * RSS Feed Reader
 * 
 * @author Aubert Gwendal & Scolan Alexis
 *
 */
public class FeedReader {

	/**
	 * XML File
	 */
	static File fileDB;

	/**
	 * FileWriter
	 */
	static FileWriter fw;
	
	/**
	 * BufferedWriter
	 */
	static BufferedWriter bw;
	
	/**
	 * Main fonction
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Proxy configuration
		/*System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "squidva.univ-ubs.fr");
		System.setProperty("http.proxyPort", "3128");
		System.setProperty("http.proxyType", "4");*/

		if (args.length != 1)
			System.exit(1);
		
		// Setting the path for Langdetector
		String dir = System.getProperty("user.dir");
		System.out.println("current dir = " + dir);

		//MapDB initialisation
		fileDB = new File( dir + "/db.xml");
		
		try {
			fw = new FileWriter(fileDB.getAbsoluteFile());
			bw = new BufferedWriter(fw);
		} catch (IOException e2) {
			e2.printStackTrace();
		}	

		try {
			DetectorFactory.loadProfile(dir + "/profiles");
		} catch (LangDetectException e1) {
			e1.printStackTrace();
		}

		// URL du feed
		try {
			String line;
			InputStream fis = new FileInputStream(dir+"/"+args[0]);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				URL feedUrl = new URL(line);
				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build(new XmlReader(feedUrl));
				printFeed(feed);
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Printer for the feed
	 * 
	 * @param feed
	 */
	private static void printFeed(SyndFeed feed) {
		List<SyndEntry> entries = feed.getEntries();

		for(SyndEntry e:entries){

			String title ="", description="", author="", txtcontent="", date="", url_src = "", txt_src= "", language = "", copyright="";

			//URL Source
			url_src = e.getLink();

			Tika tika=new Tika();
			try {
				URL url = new URL(e.getLink());

				if(! tika.detect(url).contains("html")){
					txtcontent=tika.parseToString(url);
				}else{

					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					txtcontent=connection.getResponseMessage();
					StringWriter wr=new StringWriter();
					IOUtils.copy(connection.getInputStream(), wr, "utf-8");
					txtcontent=wr.toString();
					//Contenu de la page
					txtcontent = ArticleSentencesExtractor.INSTANCE.getText(txtcontent).trim().replaceAll(" +|\n", " ");						

				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			if(e.getSource()!=null){
				//Sources du texte
				txt_src = e.getSource().toString();
			}

			//Titre de l'article
			title = e.getTitle();

			//Date de l'article
			date = e.getPublishedDate().toString();

			//Description de l'article
			description = e.getDescription().toString();

			try {
				Detector detector = DetectorFactory.create();
				detector.append(e.getSource()+" "+e.getTitle()+" "+e.getDescription());

				//Langage de l'article
				language = detector.detect();

			} catch (LangDetectException e1) {
				System.out.println("Couldnt detect the language");
				e1.printStackTrace();
			}

			MRIEntry entry = new MRIEntry(title, description, txtcontent, author, date, url_src, txt_src, language, copyright);
			System.out.println(entry.toString());
			try {
				bw.write(entry.toXMLString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

}