package trader.lb;

import java.io.File;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;

import com.trader.client.MarketEvents;
import com.trader.client.MarketEvents.ISpreadListener;
import com.trader.definitions.TraderFolders;
import com.trader.definitions.TraderFolders.ProgramName;
import com.trader.logging.LoggingUtil;
import com.trader.logging.Transaction;
import com.trader.luno.AccWallet;
import com.trader.luno.IOrderFilled;
import com.trader.luno.LunoBTCManager;
import com.trader.luno.OrderTracker;
import com.trader.model.MarketType;
import com.trader.model.Spread;
import com.trader.utility.MarketData;
import com.trader.utility.MarketData.MarketPrice;

import arbtrader.stats.TradeLimits;
import arbtrader.stats.limits.MeanStandardDeviation;
import arbtrader.stats.limits.MeanStandardDeviation.Formula;

public class LBTrader implements IOrderFilled, ISpreadListener {

	private final File transactionFile;

	final LunoBTCManager luno;

	private final MeanStandardDeviation limitGetterDiff;
	private final MeanStandardDeviation limitGetterRate;
	AccWallet wallet;

	public LBTrader() {

		// logging files
		transactionFile = new File(TraderFolders.getLogging(ProgramName.LunoBitstamp), "transactions.txt");

		wallet = new AccWallet(MarketType.ZAR_BTC);

		limitGetterDiff = new MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC_PERCDIFF, 24 * 60, 1);
		limitGetterRate = new MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC, 24 * 60, 1);

		luno = new LunoBTCManager(MarketType.ZAR_BTC, wallet);
		luno.addOrderFilledListener(this);

		MarketEvents.get(MarketType.ZAR_BTC).addSpreadListener(this);
	}

	@Override
	public void orderFilled(OrderTracker t) {
		// save data
		Transaction tluno = new Transaction(t.o.id, t.getFill(), t.o.price, t.orderType, CurrencyPair.BTC_ZAR);
		LoggingUtil.appendToFile(transactionFile, tluno.toString());
		System.out.println("Order filled lunoluno:" + t.o);

		// bitstamp trade
		OrderType btype = t.orderType.equals(OrderType.BID) ? OrderType.ASK : OrderType.BID;
		// org.knowm.xchange.dto.Order oBitstamp = BitstampTrading.placeOrder(o.volume,
		// btype);

		//
		MarketPrice mp = MarketData.INSTANCE.getUSDrBTC(1);
		Transaction tbitstamp = new Transaction("simul", 0, mp.mid(), btype, CurrencyPair.BTC_USD);
		LoggingUtil.appendToFile(transactionFile, tbitstamp.toString());

	}

	@Override
	public void spreadChanged() {
		try {

			TradeLimits limitsDiff = limitGetterDiff.getTradeLimits(1, 5);
			TradeLimits limitsRate = limitGetterRate.getTradeLimits(0.1, 5);

			double zarusd = MarketData.INSTANCE.getZARrUSD(1).mid();
			Spread spread = MarketEvents.getSpread(MarketType.ZAR_BTC);
			MarketPrice mp = MarketData.INSTANCE.getUSDrBTC(1);

			double rateUtoZ = spread.priceAsk / mp.ask;
			double rateZtoU = spread.priceBid / mp.bid;
			double diffUtoZ = (rateUtoZ - zarusd) / zarusd * 100;
			double diffZtoU = (rateZtoU - zarusd) / zarusd * 100;

			String log = "";
			if (diffUtoZ > limitsDiff.upper && rateUtoZ > limitsRate.upper) {
				// sell BTC
				System.out.println("Selling luno...");
				log = append("diff", log, diffUtoZ, limitsDiff.upper);
				log = append("rate", log, rateUtoZ, limitsRate.upper);
				System.out.println(log);
				luno.setWantedBTC(0);
			} else if (diffZtoU < limitsDiff.lower && rateZtoU < limitsRate.lower) {
				// buy BTC
				System.out.println("Buying luno...");

				log = append("diff", log, diffZtoU, limitsDiff.lower);
				log = append("rate", log, rateZtoU, limitsRate.lower);
				System.out.println(log);
				double buy = luno.getWallet().getMaxBuy(spread.priceAsk);
				luno.tradeBTC(buy);
			} else {
				// don't trade
				System.out.println("Not trading...");
				System.out.println(limitsDiff + "," + limitsRate + "," + diffUtoZ + "," + rateUtoZ);
				luno.setWantedBTC(wallet.getBtc());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String append(String description, String existing, double actual, double limit) {
		boolean eval = actual > limit;

		// logging
		actual = Math.floor(actual * 10_000) / 10_000;
		limit = Math.floor(limit * 10_000) / 10_000;
		String toAppend = description + " " + eval + " " + actual + ">" + limit + "  ";

		return existing + toAppend;
	}
}
