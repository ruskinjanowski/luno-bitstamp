package trader.lb;

import java.io.File;
import java.io.IOException;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;

import com.trader.logging.LimitsAndRates;
import com.trader.logging.LoggingUtil;
import com.trader.logging.Transaction;
import com.trader.market.data.MarketData;
import com.trader.market.data.MarketData.MarketPrice;
import com.trader.single.AccWallet;
import com.trader.single.IOrderFilled;
import com.trader.single.LunoBTCManager;
import com.trader.single.OrderTracker;

import arbtrader.controller.MarketEvents;
import arbtrader.controller.MarketEvents.ISpreadListener;
import arbtrader.credentials.EMarketType;
import arbtrader.credentials.TraderFolders;
import arbtrader.credentials.TraderFolders.ProgramName;
import arbtrader.model.SpreadChanged;
import arbtrader.stats.TradeLimits;
import arbtrader.stats.limits.MeanStandardDeviation;

public class LBTrader implements IOrderFilled, ISpreadListener {

	private final File transactionFile;
	private final File limitsFile;

	final LunoBTCManager luno;

	private final MeanStandardDeviation limitGetter;
	AccWallet wallet;

	public LBTrader(MeanStandardDeviation limitsGetter) {

		// logging files
		String type = limitsGetter.formula.toString();
		transactionFile = new File(TraderFolders.getLogging(ProgramName.LunoBitstamp), "transactions" + type + ".txt");
		limitsFile = new File(TraderFolders.getLogging(ProgramName.LunoBitstamp), "limits" + type + ".txt");
		wallet = new AccWallet(EMarketType.ZAR_BTC);

		this.limitGetter = limitsGetter;

		luno = new LunoBTCManager(EMarketType.ZAR_BTC, wallet);
		luno.addOrderFilledListener(this);

		MarketEvents.get(EMarketType.ZAR_BTC).addSpreadListener(this);
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

	TradeLimits tl;
	long tlTime = 0;

	private TradeLimits getTradeLimits() {
		if (System.currentTimeMillis() - tlTime > 5 * 60 * 1000) {
			try {
				tl = limitGetter.getTradeLimits();
				double dp = tl.upper - tl.lower;
				if (dp < 1) {
					System.out.println("limits too small: " + tl);
					double mid = (tl.upper + tl.lower) / 2;

					tl = new TradeLimits(mid + 0.5, mid - 0.5);
				} else {
					// okay
				}
				tlTime = System.currentTimeMillis();
				System.out.println("updated tradeLimimits: " + tl);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return tl;
	}

	@Override
	public void spreadChanged() {
		try {
			TradeLimits tradeLimits = getTradeLimits();
			double zarusd = MarketData.INSTANCE.getZARrUSD(1).mid();
			SpreadChanged spread = MarketEvents.getSpread(EMarketType.ZAR_BTC);
			MarketPrice mp = MarketData.INSTANCE.getUSDrBTC(1);

			double rateupper = spread.priceAsk / mp.bid;
			double ratelower = spread.priceBid / mp.ask;
			double diffupper = (rateupper - zarusd) / zarusd * 100;
			double difflower = (ratelower - zarusd) / zarusd * 100;

			LimitsAndRates lr = new LimitsAndRates(tradeLimits.upper, tradeLimits.lower, diffupper, difflower);
			System.out.println(lr);
			LoggingUtil.appendToFile(limitsFile, lr.toString());

			System.out.println("rates: " + lr);
			if (diffupper > tradeLimits.upper) {
				// sell BTC

				luno.setWantedBTC(0);
			} else if (difflower < tradeLimits.lower) {
				// buy BTC
				double buy = luno.getWallet().getMaxBuy(spread.priceAsk);
				luno.tradeBTC(buy);// use the wrong price on purpose for safety to
									// avoid
									// insufficient funds error
			} else {
				// don't trade
				luno.setWantedBTC(wallet.getBtc());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
