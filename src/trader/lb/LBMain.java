package trader.lb;

import java.io.IOException;

import com.trader.api.Api;
import com.trader.client.EventClientEndpoint;
import com.trader.definitions.TraderFolders.ProgramName;
import com.trader.utility.Utility;

public class LBMain {
	public static void main(String[] args) throws IOException {

		Api.createApis(ProgramName.LunoBitstamp);

		LBTrader trader2 = new LBTrader();

		EventClientEndpoint.startClient();

		Utility.waitIndefinitely();

	}

}
