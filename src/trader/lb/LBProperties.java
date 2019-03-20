package trader.lb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import com.trader.definitions.TraderFolders;
import com.trader.definitions.TraderFolders.ProgramName;

public class LBProperties {

	private static final File file = new File(TraderFolders.getConfig(ProgramName.LunoBitstamp), "LB.properties");
	private static final PropertiesConfiguration props = new PropertiesConfiguration();
	private static final PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(props);

	static {

		System.out.println("Loading properties file: " + file.getAbsolutePath());

		try {
			layout.load(new InputStreamReader(new FileInputStream(file)));
		} catch (ConfigurationException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
