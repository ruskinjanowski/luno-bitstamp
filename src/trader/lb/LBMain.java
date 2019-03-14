package trader.lb;

import java.io.IOException;

import com.trader.client.EventClientEndpoint;
import com.trader.controller.api.Api;

import arbtrader.credentials.TraderFolders.ProgramName;
import arbtrader.stats.limits.MeanStandardDeviation;
import arbtrader.stats.limits.MeanStandardDeviation.Formula;

public class LBMain {
	public static void main(String[] args) throws IOException {

		Api.createApis(ProgramName.LunoBitstamp);
		if (LBProperties.difference) {
			MeanStandardDeviation ms = new MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC_PERCDIFF, 24 * 60,
					0.3);
			LBTrader trader2 = new LBTrader(ms);
		}

		EventClientEndpoint.startClient();
		// if (LBProperties.exchangerate) {
		// MeanStandardDeviation ms = new
		// MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC, 24 * 60, 1);
		// LBTrader trader1 = new LBTrader(ms);
		// }

		EventClientEndpoint.waitIndefinitely();

	}

}
