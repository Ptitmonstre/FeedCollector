import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
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

import org.mapdb.*;

/**
 * RSS Feed Reader
 * 
 * @author Aubert Gwendal & Scolan Alexis
 *
 */
public class FeedReader {

	/**
	 * URL of the feed
	 */
	private URL url;
	/**
	 * Detector object
	 */
	private static Detector detector = null;
	/**
	 * Database
	 */
	private static DB db;
	/**
	 * Map DB
	 */
	private static ConcurrentNavigableMap treeMap;
	
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
		
		//MapDB initialisation
		db = DBMaker.newMemoryDB().make();
		treeMap = db.getTreeMap("map");
		
		//Ajout en DB : 
		//treeMap.put(hash, object)
		//db.commit();
		
		//fermeture de la DB:
		//db.close();

		// Setting the path for Langdetector
		String dir = System.getProperty("user.dir");
		System.out.println("current dir = " + dir);
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
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		// Loading profile for LangDetector
		ObjectOutputStream o;
		try {
			o = new ObjectOutputStream(b);
			for (SyndEntry e : entries) {
				// Creating a hash of the message
				o.writeObject(e);
				System.out.print("Hash: " + b.toByteArray().toString());
				System.out.print(", URL: " + e.getUri());
				System.out.print(", Source: " + e.getSource());
				System.out.print(", Date: " + e.getPublishedDate());
				System.out.print(", Title: " + e.getTitle());
				System.out.print(", Description: " + e.getDescription());

				try {
					// Detector creation and exploitation
					detector = DetectorFactory.create();
					detector.append(e.getUri() + " " + e.getTitle() + " " + e.getDescription());
					System.out.print("Language : " + detector.detect() + "\n");
				} catch (LangDetectException e1) {
					e1.printStackTrace();
				}

			}
		} catch (IOException e2) {
			System.err.println("Error creating the messages hashes");
			e2.printStackTrace();
		}
	}

}