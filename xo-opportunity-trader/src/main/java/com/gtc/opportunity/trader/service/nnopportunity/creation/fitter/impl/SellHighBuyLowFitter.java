package com.gtc.opportunity.trader.service.nnopportunity.creation.fitter.impl;

import com.gtc.model.provider.OrderBook;
import com.gtc.opportunity.trader.domain.ClientConfig;
import com.gtc.opportunity.trader.service.nnopportunity.creation.fitter.FeeFitted;
import com.gtc.opportunity.trader.service.nnopportunity.creation.fitter.Fitter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by Valentyn Berezin on 31.08.18.
 */
@Service
public class SellHighBuyLowFitter implements Fitter {

    @Override
    public FeeFitted after(OrderBook book, ClientConfig config) {
        BigDecimal priceWeSell = Util.ceilPrice(config, book.getBestBuy());
        BigDecimal sellAmount = Util.calculateAmount(config, priceWeSell);
        double charge = Util.computeCharge(config);
        BigDecimal buyAmount = Util.ceilAmount(config, sellAmount.doubleValue() / charge);
        BigDecimal priceWeBuy = Util.floorPrice(config,
                priceWeSell.doubleValue() * sellAmount.doubleValue() * charge / Util.computeGain(config)
                        / buyAmount.doubleValue()
        );

        return new FeeFitted(
                priceWeBuy,
                priceWeSell,
                buyAmount,
                sellAmount,
                Util.avg(sellAmount, buyAmount),
                buyAmount.multiply(BigDecimal.valueOf(charge)).subtract(sellAmount),
                sellAmount.multiply(priceWeSell).multiply(BigDecimal.valueOf(charge))
                        .subtract(buyAmount.multiply(priceWeBuy))
        );
    }

    @Override
    public FeeFitted before(OrderBook book, ClientConfig config) {
        BigDecimal priceWeSell = Util.ceilPrice(config, book.getBestBuy());
        BigDecimal amount = Util.calculateAmount(config, priceWeSell);
        double charge = Util.computeCharge(config);
        BigDecimal priceWeBuy = Util.floorPrice(config,
                priceWeSell.doubleValue() * charge * charge / Util.computeGain(config)
        );

        return new FeeFitted(
                priceWeBuy,
                priceWeSell,
                amount,
                amount,
                amount,
                BigDecimal.ZERO,
                BigDecimal.valueOf(
                        amount.doubleValue() * (priceWeSell.doubleValue() * charge - priceWeBuy.doubleValue() / charge)
                )
        );
    }
}
