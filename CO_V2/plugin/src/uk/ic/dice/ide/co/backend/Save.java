package uk.ic.dice.ide.co.backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Save {

	static Properties prop = new Properties();

	public static void saveProperty(String key, String value) {
		prop.setProperty(key, value);
	}

	public static String loadProperty(String key) {
		String value = prop.getProperty(key);
		return (value != null) ? value : "";
	}

	public static void loadFile() {
		try {
			prop.load(new FileInputStream("co_config"));
		} catch (FileNotFoundException e) {
			// no config file, thats fine.
		} catch (IOException e) {
		}
	}

	public static void write() {
		try {
			FileOutputStream output = new FileOutputStream("co_config");
			// System.out.println(output.getFD().toString());
			prop.store(output, null);
		} catch (IOException e) {
		}
	}
}
