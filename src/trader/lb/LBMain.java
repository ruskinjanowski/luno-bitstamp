package trader.lb;

import java.io.IOException;

import com.trader.client.EventClientEndpoint;
import com.trader.controller.api.Api;

import arbtrader.credentials.TraderFolders.ProgramName;

public class LBMain {
	public static void main(String[] args) throws IOException {

		Api.createApis(ProgramName.LunoBitstamp);

		LBTrader trader2 = new LBTrader();

		EventClientEndpoint.startClient();
		// if (LBProperties.exchangerate) {
		// MeanStandardDeviation ms = new
		// MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC, 24 * 60, 1);
		// LBTrader trader1 = new LBTrader(ms);
		// }

		EventClientEndpoint.waitIndefinitely();

	}

}
