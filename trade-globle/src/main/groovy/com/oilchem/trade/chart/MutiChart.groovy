package com.oilchem.trade.chart

import ofc4j.model.Chart
import ofc4j.model.axis.YAxis
import ofc4j.model.elements.LineChart
import ofc4j.model.axis.XAxis
import ofc4j.model.axis.Label
import com.oilchem.trade.domain.abstrac.TradeSum
import com.oilchem.trade.domain.abstrac.TradeDetail
import ofc4j.model.Text

import com.oilchem.trade.bean.DocBean

import static java.lang.Integer.toHexString
import ofc4j.model.elements.BarChart
import ofc4j.model.elements.PieChart

import static com.oilchem.trade.bean.DocBean.ExcelFiled.*
import com.oilchem.trade.bean.ChartData

class MyChart {

    /**
     * 获得拆线图
     * @return
     */
    def List<Chart> getDetailLineChart(List<ChartData<TradeDetail>> chartDataList) {

        //-------------tradeDetail  ----------------------
        List<Chart> detailChartList = new ArrayList<Chart>();

        Chart amountChat = new Chart()
                .setTitle(new Text("数量")).setYLegend(new Text("数量"));
        Chart amountMoneyChart = new Chart()
                .setTitle(new Text("金额")).setYLegend(new Text("金额"));
        Chart unitpriceChart = new Chart()
                .setTitle(new Text("平均价格")).setYLegend(new Text("平均价格"));

        //遍历每个月
        chartDataList.each {
            List<List<LineChart>> detailFiledLineList = getDetailFiledListList(it.elementList)

            detailChartList << newChart(amountChat, it, "amount").addElements(detailFiledLineList.get(0))
            detailChartList << newChart(amountMoneyChart, it, "amountMoney").addElements(detailFiledLineList.get(1))
            detailChartList << newChart(unitpriceChart, it, "unitPrice").addElements(detailFiledLineList.get(2))
        }
        detailChartList
    }

    def List<Chart> getSumLineChart(List<ChartData<TradeSum>> chartDataList) {

        //--------------tradeSum----------------------------
        List<Chart> sumChartList = new ArrayList<Chart>();
        Chart nummonthChat = new Chart()
                .setTitle(new Text(excel_num_month.value())).setYLegend(new Text(excel_num_month.value()));
        Chart numSumChat = new Chart()
                .setTitle(new Text(excel_num_sum.value())).setYLegend(new Text(excel_num_sum.value()))
        Chart moneyMonthChat = new Chart()
                .setTitle(new Text(excel_money_month.value())).setYLegend(new Text(excel_money_month.value()))
        Chart moneySumChat = new Chart()
                .setTitle(new Text(excel_money_sum.value())).setYLegend(new Text(excel_money_sum.value()));
        Chart avgPriceMonthChat = new Chart()
                .setTitle(new Text(excel_avg_price_month.value())).setYLegend(new Text(excel_avg_price_month.value()));
        Chart avgPriceSumChat = new Chart()
                .setTitle(new Text(excel_avg_price_sum.value())).setYLegend(new Text(excel_avg_price_sum.value()));
        Chart numPreMonthIncratioChat = new Chart()
                .setTitle(new Text(excel_num_premonth_incratio.value())).setYLegend(new Text(excel_num_premonth_incratio.value()));
        Chart numPreYearSameMonthChat = new Chart()
                .setTitle(new Text(excel_num_preyearsamemonth_incratio.value())).setYLegend(new Text(excel_num_preyearsamemonth_incratio.value()));
        Chart numPreYearSameQuarterIncratioChat = new Chart()
                .setTitle(new Text(excel_num_preyearsamequarter_imcratio.value())).setYLegend(new Text(excel_num_preyearsamequarter_imcratio.value()));

        chartDataList.each {
            List<List<LineChart>> sumFiledLineList = getSumFiledLineList(it.elementList)

            sumChartList << newChart(nummonthChat, it, excel_num_month.value()).addElements(sumFiledLineList.get(0))
            sumChartList << newChart(numSumChat, it, excel_num_sum.value()).addElements(sumFiledLineList.get(1))
            sumChartList << newChart(moneyMonthChat, it, excel_money_month.value()).addElements(sumFiledLineList.get(2))
            sumChartList << newChart(moneySumChat, it, excel_money_sum.value()).addElements(sumFiledLineList.get(3))
            sumChartList << newChart(avgPriceMonthChat, it, excel_avg_price_month.value()).addElements(sumFiledLineList.get(4))
            sumChartList << newChart(avgPriceSumChat, it, excel_avg_price_sum.value()).addElements(sumFiledLineList.get(5))
            sumChartList << newChart(numPreMonthIncratioChat, it, excel_num_premonth_incratio.value()).addElements(sumFiledLineList.get(6))
            sumChartList << newChart(numPreYearSameMonthChat, it, excel_num_preyearsamemonth_incratio.value()).addElements(sumFiledLineList.get(7))
            sumChartList << newChart(numPreYearSameQuarterIncratioChat, it, excel_num_preyearsamequarter_imcratio.value()).addElements(sumFiledLineList.get(8))
        }
        sumChartList
    }


    def List<List<LineChart>> getSumFiledLineList(List<List<TradeSum>> labelSumsList) {
        List<List<LineChart>> sumFiledLineList = new ArrayList<List<LineChart>>(9);
        LineChart numMonthLineChart = new LineChart();
        LineChart numSumLineChart = new LineChart();
        LineChart moneyMonthLineChart = new LineChart();
        LineChart moneySumhLineChart = new LineChart();
        LineChart avgPriceMonthhLineChart = new LineChart();
        LineChart avgPriceSumLineChart = new LineChart();
        LineChart numPreMonthIncRatioLineChart = new LineChart();
        LineChart numPreYearSameMonthIncRatioLineChart = new LineChart();
        LineChart numPreYearSameQuarterInrRatioLineChart = new LineChart();

        labelSumsList.each {
            sumFiledLineList.size() < 1 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(0) << newLineElement(numMonthLineChart, it).addValues(it.numMonth)
            sumFiledLineList.size() < 2 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(1) << newLineElement(numSumLineChart, it).addValues(it.numSum)
            sumFiledLineList.size() < 3 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(2) << newLineElement(moneyMonthLineChart, it).addValues(it.moneyMonth)
            sumFiledLineList.size() < 4 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(3) << newLineElement(moneySumhLineChart, it).addValues(it.moneySum)
            sumFiledLineList.size() < 5 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(4) << newLineElement(avgPriceMonthhLineChart, it).addValues(it.avgPriceMonth)
            sumFiledLineList.size() < 6 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(5) << newLineElement(avgPriceSumLineChart, it).addValues(it.avgPriceSum)
            sumFiledLineList.size() < 7 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(6) << newLineElement(numPreMonthIncRatioLineChart, it).addValues(it.numPreMonthIncRatio)
            sumFiledLineList.size() < 8 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(7) << newLineElement(numPreYearSameMonthIncRatioLineChart, it).addValues(it.numPreYearSameMonthIncRatio)
            sumFiledLineList.size() < 9 ? sumFiledLineList << new ArrayList<LineChart>() : sumFiledLineList
            sumFiledLineList.get(8) << newLineElement(numPreYearSameQuarterInrRatioLineChart, it).addValues(it.numPreYearSameQuarterInrRatio)
        }
        sumFiledLineList
    }

    def List<List<LineChart>> getDetailFiledListList(List<TradeDetail> detailList) {

        List<List<LineChart>> detailFiledLineList = new ArrayList<List<LineChart>>(3);

        LineChart amountLineChart = new LineChart();
        LineChart amountMoneyLineChart = new LineChart();
        LineChart unitPriceLineChart = new LineChart();


        detailList.each {
            detailFiledLineList.size() < 1 ? detailFiledLineList << new ArrayList<LineChart>() : detailFiledLineList
            detailFiledLineList.get(0) << newLineElement(amountLineChart, it).addValues(it.amount)

            detailFiledLineList.size() < 2 ? detailFiledLineList << new ArrayList<LineChart>() : detailFiledLineList
            detailFiledLineList.get(1) << newLineElement(amountMoneyLineChart, it).addValues(it.amountMoney)

            detailFiledLineList.size() < 3 ? detailFiledLineList << new ArrayList<LineChart>() : detailFiledLineList
            detailFiledLineList.get(2) << newLineElement(unitPriceLineChart, it).addValues(it.unitPrice)
        }
        detailFiledLineList

    }

    private def Chart newChart(Chart chart, ChartData chartData, BigDecimal key) {
        return chart.setXAxis(new XAxis().addLabels(chartData.labels))
                .setXLegend(new Text(chartData.x_legend))
                .setYAxis(new YAxis().setRange(0,           //rang从0到最大值
                chartData.maxRangMap.get(key),
                chartData.maxRangMap.get(key) / 20))
    }

    private def LineChart newLineElement(LineChart lineChart, def it) {
        lineChart.setWidth(1)
                .setColour(getRadomColor())
                .setDotSize(5)
                .setText(it.productName.length() > 6 ? it.productName.substring(0, 6) : it.productName)
    }

    /**
     * 获得柱状图
     * @return
     */
    def Chart getBarChart(List<TradeSum> tradeSumList, ArrayList<LineChart> lineElements,
                          List<TradeDetail> tradeDetailList, ArrayList<Label> labels,
                          DocBean.ChartProps chartProps) {

    }

    /**
     * 获得饼状图
     * @return
     */
    def Chart getpieChart(List<TradeSum> tradeSumList, ArrayList<LineChart> lineElements,
                          List<TradeDetail> tradeDetailList, ArrayList<Label> labels,
                          DocBean.ChartProps chartProps) {

    }

    /**
     * 随机获得颜色值
     * @return
     */
    private def getRadomColor() {
        def rand = new Random()
        def color = "#" + toHexString(rand.nextInt(255)) + toHexString(rand.nextInt(255)) + toHexString(rand.nextInt(255))

        for (def i = 7 - color.length(); i > 0; i--) {
            color = color + "0";
        }
        return color;
    }

    static void main(agrs) {
        def x_legend = "aaa"
        def y_legend = "bbb"
        List<TradeSum> tradeSumList = null
        List<TradeDetail> tradeDetailList1 = new ArrayList<TradeDetail>()
        List<TradeDetail> tradeDetailList2 = new ArrayList<TradeDetail>()
        tradeDetailList1 << new TradeDetail().setAmount(12345).setUnitPrice(11111).setProductName("aaaaaaaa").setAmountMoney(555555).setYearMonth("2010-05")
        tradeDetailList1 << new TradeDetail().setAmount(23333).setUnitPrice(22222).setProductName("bbbbbbbb").setAmountMoney(666666).setYearMonth("2010-05")
        tradeDetailList2 << new TradeDetail().setAmount(33563).setUnitPrice(33333).setProductName("cccccccc").setAmountMoney(777777).setYearMonth("2012-11")
        tradeDetailList2 << new TradeDetail().setAmount(44524).setUnitPrice(44444).setProductName("dddddddd").setAmountMoney(888888).setYearMonth("2012-11")

        List<List<TradeDetail>> labelDetailList = new ArrayList<List<TradeDetail>>();
        labelDetailList << tradeDetailList1
        labelDetailList << tradeDetailList2

        List<Label> labels = []
        labels << new Label("2010-05")
        labels << new Label("2012-11")
        List<Chart> chartList = new MyChart().getDetailLineChart(labelDetailList, labels, new DocBean.ChartProps(x_legend, y_legend))
        println chartList;
    }

}