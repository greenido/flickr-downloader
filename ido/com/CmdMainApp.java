package ido.com;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.activity.ActivityInterface;
import com.aetrion.flickr.activity.Event;
import com.aetrion.flickr.activity.Item;
import com.aetrion.flickr.activity.ItemList;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photosets.PhotosetsInterface;
import com.aetrion.flickr.util.IOUtilities;

/**
 * Use to grap your friends' photo (sets) and save them localy
 * 
 * @author ido green
 * http://idojava.blogspot.com
 */
public class CmdMainApp {

	private final String SAVE_DIR = System.getProperty("user.home") + "/temp/";
	private final String REST_F_HOST = "www.flickr.com";

	private Flickr flickr;
	private REST rest;
	private RequestContext requestContext;
	private Properties properties = null;

	public enum PhotoSize {
		small, med, large, bigest
	};

	/**
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public CmdMainApp() throws ParserConfigurationException, IOException {
		InputStream in = null;
		try {
			in = new FileInputStream("etc/setup.properties");
			properties = new Properties();
			properties.load(in);
		} finally {
			IOUtilities.close(in);
		}
		rest = new REST();
		rest.setHost(REST_F_HOST);
		flickr = new Flickr(properties.getProperty("apiKey"), rest);
		Flickr.debugStream = false;
		// Set the shared secret which is used for any calls which require
		// signing.
		requestContext = RequestContext.getRequestContext();
		requestContext.setSharedSecret(properties.getProperty("secret"));
		Auth auth = new Auth();
		auth.setPermission(Permission.READ);
		auth.setToken(properties.getProperty("token"));
		requestContext.setAuth(auth);
		Flickr.debugRequest = false;
		Flickr.debugStream = true;
	}

	public void dowloadPhotos(String photosetId, PhotoSize size)
			throws Exception {
		PhotosetsInterface photos = flickr.getPhotosetsInterface();

		int total = photos.getInfo(photosetId).getPhotoCount();
		PhotoList photoList = photos.getPhotos(photosetId, total, 1);
		int nameNo = 0;
		for (Iterator iter = photoList.iterator(); iter.hasNext();) {
			Photo photo = (Photo) iter.next();
			nameNo++;
			// System.out.println("info on photo: " +photo.getId() + " format:
			// "+ photo.getOriginalFormat());
			String photoName = Integer.toString(nameNo)	+ ".jpg";
			savePhotoOnDisk((SAVE_DIR + "/" + photosetId), photoName, photo, size);
		}
	}

	/**
	 * 
	 * @param saveDir
	 * @param photoName -
	 *            should include the ext (e.g. jpg)
	 */
	private void savePhotoOnDisk(String saveDir, String photoName, Photo photo, PhotoSize size)
			throws Exception {
		if (!new File(saveDir).exists()) {
			// create the save dir
			new File(saveDir).mkdir();
		}
		File file = new File(saveDir + "/" + photoName);
		BufferedImage img = null;
		switch (size) {
		case small:
			img = photo.getSmallImage();
			break;
		case med:
			img = photo.getMediumImage();
			break;
		case large:
			img = photo.getLargeImage();
			break;
		case bigest:
			img = photo.getOriginalImage();
			break;
		}
		ImageIO.write(img, "jpg", file);
		System.out.println("Saved Photo: " + photo.getId() + " size:" + size +  
				" ( " + photo.getDateTaken() + " )");
	}

	/**
	 * 
	 * @throws FlickrException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void showActivity() throws FlickrException, IOException,
			SAXException {
		ActivityInterface iface = flickr.getActivityInterface();
		ItemList list = iface.userComments(10, 0);
		for (int j = 0; j < list.size(); j++) {
			Item item = (Item) list.get(j);
			System.out.println("Item " + (j + 1) + "/" + list.size()
					+ " type: " + item.getType());
			System.out.println("Item-id:       " + item.getId() + "\n");
			ArrayList events = (ArrayList) item.getEvents();
			for (int i = 0; i < events.size(); i++) {
				System.out.println("Event " + (i + 1) + "/" + events.size()
						+ " of Item " + (j + 1));
				System.out.println("Event-type: "
						+ ((Event) events.get(i)).getType());
				System.out.println("User:       "
						+ ((Event) events.get(i)).getUser());
				System.out.println("Username:   "
						+ ((Event) events.get(i)).getUsername());
				System.out.println("Value:      "
						+ ((Event) events.get(i)).getValue() + "\n");
			}
		}
		ActivityInterface iface2 = flickr.getActivityInterface();
		list = iface2.userPhotos(50, 0, "300d");
		for (int j = 0; j < list.size(); j++) {
			Item item = (Item) list.get(j);
			System.out.println("Item " + (j + 1) + "/" + list.size()
					+ " type: " + item.getType());
			System.out.println("Item-id:       " + item.getId() + "\n");
			ArrayList events = (ArrayList) item.getEvents();
			for (int i = 0; i < events.size(); i++) {
				System.out.println("Event " + (i + 1) + "/" + events.size()
						+ " of Item " + (j + 1));
				System.out.println("Event-type: "
						+ ((Event) events.get(i)).getType());
				if (((Event) events.get(i)).getType().equals("note")) {
					System.out.println("Note-id:    "
							+ ((Event) events.get(i)).getId());
				} else if (((Event) events.get(i)).getType().equals("comment")) {
					System.out.println("Comment-id: "
							+ ((Event) events.get(i)).getId());
				}
				System.out.println("User:       "
						+ ((Event) events.get(i)).getUser());
				System.out.println("Username:   "
						+ ((Event) events.get(i)).getUsername());
				System.out.println("Value:      "
						+ ((Event) events.get(i)).getValue());
				System.out.println("Dateadded:  "
						+ ((Event) events.get(i)).getDateadded() + "\n");
			}
		}
	}

	/**
	 * USED the API util from: http://flickrj.sourceforge.net/
	 * 
	 * @param args
	 *            Single photo:
	 *            http://flickr.com/photo_zoom.gne?id=photo-id&size=o
	 * 
	 * All the set:
	 * 
	 * http://www.flickr.com/services/api/keys/
	 * 
	 * e.g.
	 * http://www.flickr.com/photos/ofer_shaked/3032838002/in/set-72157609129902920/
	 */
	public static void main(String[] args) throws Exception {
		CmdMainApp app = new CmdMainApp();
		System.out.println("== Starting the party...");
		app.dowloadPhotos("72157609129902920", PhotoSize.bigest);
		System.out.println("== Done with the party.");
	}

}
