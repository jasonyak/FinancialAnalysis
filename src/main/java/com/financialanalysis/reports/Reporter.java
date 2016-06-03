package com.financialanalysis.reports;

import com.financialanalysis.data.Account;
import com.financialanalysis.graphing.LineChart;
import com.financialanalysis.strategy.StrategyOutput;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.stream.Collectors;

@Log4j
public class Reporter {

    public List<Report> generateReports(List<StrategyOutput> allResults) {
        return allResults.stream().map(s -> s.getCharts()).flatMap(c -> c.stream()).map(c -> new Report(c)).collect(Collectors.toList());
    }

    public String generateIndividualAccountSummary(List<StrategyOutput> allResults) {
        StringBuffer buf = new StringBuffer();

        allResults.forEach(output -> {
            String info = output.getAccount().getSummary();
            buf.append(info + "\n");
            log.info(info);
        });

        return buf.toString();
    }

    public String generateIndividualAccountSummary(List<StrategyOutput> allResults, double gainLossThreshold) {
        List<StrategyOutput> list = allResults.stream().filter(
                o -> o.getAccount().getPercentageGainLoss() < -gainLossThreshold || gainLossThreshold < o.getAccount().getPercentageGainLoss())
                .collect(Collectors.toList());
        return generateIndividualAccountSummary(list);
    }

    public String generateDetailedAccountSummary(List<StrategyOutput> allResults) {
        StringBuffer buf = new StringBuffer();

        allResults.forEach(output -> {
            String info = output.getAccount().getAll();
            buf.append(info + "\n");
            log.info(info);
        });

        return buf.toString();
    }

    public String generateAverageAccountSummary(List<StrategyOutput> allResults) {
        List<Account> accounts = allResults.stream().map(StrategyOutput::getAccount).collect(Collectors.toList());
        double gainNotZero = 0;
        double numNotZero = 0;

        double gain = 0;
        double num = 0;
        double posGain = 0;
        double totalGainLossP = 0.0;
        for(Account account : accounts) {
            double gainLossP = account.getPercentageGainLoss();
            totalGainLossP += gainLossP;

            if(account.getActivity().isEmpty()) continue;
            if(gainLossP > 0) posGain++;
            gain += gainLossP;
            num++;

            if(gainLossP < -2.0 || 2.0 < gainLossP) {
                gainNotZero += gainLossP;
                numNotZero++;
            }
        }
        if(num == 0) return "";

        StringBuffer buf = new StringBuffer();
        String a = String.format("Positive Return:                %.2f%%", posGain / num * 100);
        String b = String.format("Gain/Loss Ave:                  %.2f%%", gain/num);
        String c = String.format("Gain/Loss NOT -2%% to 2%% Ave:    %.2f%%", gainNotZero/numNotZero);
        String d = String.format("Total Gain/Loss:                %.2f%%", totalGainLossP);
        String e = String.format("Number of meaningful trades:    %.2f%% %d/%d ", numNotZero * 100.0 / num, (int) numNotZero, (int) num);
        buf.append("\n" + a + "\n" + b + "\n" + c + "\n" + d + "\n" + e + "\n");
        log.info(buf.toString());

        return buf.toString();
    }

    public void generatePercentilesChart(List<StrategyOutput> allResults) {
        List<Account> accounts = allResults.stream().map(StrategyOutput::getAccount).collect(Collectors.toList());
        if(accounts.isEmpty() || accounts.size() < 2) return;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(Account account : accounts) {
            int p = (int) (account.getPercentageGainLoss());
            if(p < min) min = p;
            if(p > max) max = p;
        }

        int numPoints = max - min + 1;
        double[] percentile;
        int[] freq;

        // Case where min and max are greater than 0
        if(min > 0) {
            freq = new int[max + numPoints];
            percentile = new double[max + numPoints];
        } else {
            // Case where min is less than 0 and max is greater than 0
            freq = new int[numPoints];
            percentile = new double[numPoints];
        }

        for(Account account : accounts) {
            int index = (int) account.getPercentageGainLoss();
            freq[Math.abs(min) + index]++;
        }

        for(int i = 0; i < freq.length; i++) {
            percentile[i] = freq[i] * 100.0 / accounts.size();
        }

        for(int i = 0; i < percentile.length; i++) {
            if(percentile[i] == 0.0) continue;
            String format = String.format("Percentile: %3d%% %3.2f%% %3d/%d", i - Math.abs(min), percentile[i], freq[i], accounts.size());
            //log.info(format);
        }

        double[] x = new double[numPoints];
        double[] y = new double[numPoints];

        int xIndex = 0;
        for(int i = min; i <= max; i++) {
            x[xIndex] = i;
            y[xIndex] = percentile[xIndex];
            xIndex++;
        }

        LineChart lineChart = new LineChart("Distribution of Returns");
        lineChart.setXAxis("Percentage Gain/Loss");
        lineChart.setYAxis("%");
        lineChart.addXYLine(x, y, "Percentage Gain/Loss");
        lineChart.render();
    }
}
