package com.financialanalysis.strategyV2.bollinger;

import com.financialanalysis.analysis.AnalysisFunctionResult;
import com.financialanalysis.data.StockPrice;
import com.financialanalysis.strategyV2.Exit;

import java.util.List;

import static com.financialanalysis.analysis.AnalysisFunctions.bollingerBands;
import static com.financialanalysis.analysis.AnalysisTools.getClosingPrices;
import static com.financialanalysis.analysis.AnalysisTools.getHighPrices;

public class BollingerExit extends Exit {
    private static final int BB_PERIOD = 21;

    @Override
    public boolean shouldExit(List<StockPrice> stockPrices) {
        double[] closingPrices = getClosingPrices(stockPrices);
        double[] highPrices = getHighPrices(stockPrices);
        AnalysisFunctionResult result = bollingerBands(closingPrices, BB_PERIOD);
        double[] high = result.getBbHigh();
        double[] mid = result.getBbMid();
        double[] low = result.getBbLow();

        int i = closingPrices.length - 1;

        // If we hit our high target
        if(highPrices[i] >= high[i]) {
            return true;
        }

        // If for that past 2 days, we have closed below the low target
        if(closingPrices[i] <= low[i] && closingPrices[i-1] <= low[i-1]) {
            return true;
        }

        return false;
    }
}
