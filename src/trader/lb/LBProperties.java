package trader.lb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import arbtrader.credentials.TraderFolders;
import arbtrader.credentials.TraderFolders.ProgramName;

public class LBProperties {

	private static final File file = new File(TraderFolders.getConfig(ProgramName.LunoBitstamp), "LB.properties");
	private static final PropertiesConfiguration props = new PropertiesConfiguration();
	private static final PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(props);

	static {

		System.out.println("Loading properties file: " + file.getAbsolutePath());

		try {
			layout.load(new InputStreamReader(new FileInputStream(file)));
		} catch (ConfigurationException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public static final boolean useExchangerate =
	// Boolean.parseBoolean(getTextProperty("useExchangerate"));
	// public static final double exchangerate = getProperty("exchangerate");
	// public static final boolean useDifference =
	// Boolean.parseBoolean(getTextProperty("useDifference"));
	//
	// public static final double difference = getProperty("difference");

	private static double getProperty(String p) {
		String val = (String) props.getProperty(p);
		if (val == null) {
			throw new IllegalStateException();
		}
		return Double.parseDouble(val);
	}

	private static String getTextProperty(String p) {
		String val = (String) props.getProperty(p);
		if (val == null) {
			throw new IllegalStateException();
		}
		return val;
	}

}
