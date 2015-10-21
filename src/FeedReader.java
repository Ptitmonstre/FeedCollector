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

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.codehaus.plexus.util.StringInputStream;
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
			br.close();
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

		ObjectOutputStream o;
		try {
			o = new ObjectOutputStream(b);		
			for(SyndEntry e:entries){
				//Creating a hash of the message
				o.writeObject(e);
				System.out.print("Hash: "+b.toByteArray().toString());
				System.out.print(", URL: "+e.getLink());
				Tika tika=new Tika();
				try {
					URL url = new URL(e.getLink());
					InputStream is = url.openStream();  // throws an IOException
					byte[] b1=new byte[1024];
					int n=0;
					ByteArrayOutputStream bos=new ByteArrayOutputStream();
					while((n=is.read(b1, 0, 1024))!=-1){
						bos.write(n);
					}
					byte[] srcContent=bos.toByteArray();
					String content="";//TODO
					
					if(! tika.detect(srcContent).contains("html")){
						content="";//tika.parseToString(new StringInputStream(new String(srcContent, "UTF-8")));
					}
					try {
						System.out.print(", URL-content: "+ArticleSentencesExtractor.INSTANCE.getText(content));						
					} catch (BoilerpipeProcessingException e1) {
						e1.printStackTrace();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				if(e.getSource()!=null){System.out.print(", Source: "+e.getSource());}
				System.out.print(", Date: "+e.getPublishedDate());
				System.out.print(", Title: "+e.getTitle());
//				System.out.print(", Description: "+e.getDescription());
				try {
					Detector detector = DetectorFactory.create();
					detector.append(e.getSource()+" "+e.getTitle()+" "+e.getDescription());
					System.out.println(", Language: "+detector.detect());
				} catch (LangDetectException e1) {
					System.out.println("Couldnt detect the language");
					e1.printStackTrace();
				}
			}
		} catch (IOException e2) {
			System.err.println("Error creating the messages hashes");
			e2.printStackTrace();
		}
	}

}