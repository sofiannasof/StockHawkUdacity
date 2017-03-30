package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_HISTORY;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_SYMBOL;
import static com.udacity.stockhawk.data.Contract.Quote.QUOTE_COLUMNS;
import static com.udacity.stockhawk.ui.MainActivity.STOCK_NAME;

/**
 * Created by smenesid on 28-Mar-17.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int STOCK_LOADER = 0;

    @BindView(R.id.line_chart)
    LineChart mChart;
    private String mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSymbol = getIntent().getStringExtra(STOCK_NAME);
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(mSymbol), QUOTE_COLUMNS, null, null, COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Get data from cursor and make it to a list with the form of (x,y) format.
        if (data == null || !data.moveToFirst()) {
            return;
        }

        String dataString = data.getString(data.getColumnIndexOrThrow(COLUMN_HISTORY));
        String[] historyDatas = dataString.split("\n");
        TreeMap<Float, Float> map = new TreeMap<>();
        for (int i = 0; i < historyDatas.length; i++) {
            String[] oneDayData = historyDatas[i].split(",");
            map.put(Float.valueOf(oneDayData[0]), Float.valueOf(oneDayData[1]));
        }

        // Initial chart parameters, and Set data in chart and draw the line
        initChart(map);
    }

    private void initChart(TreeMap<Float, Float> map) {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("yy-MM-dd");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mFormat.format(new Date((long) value));
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        mChart.getAxisRight().setEnabled(false);
        mChart.setDescription(null);

        setData(map);
    }

    private void setData(TreeMap<Float, Float> map) {
        ArrayList<Entry> values = new ArrayList<Entry>();

        Iterator<HashMap.Entry<Float, Float>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Float, Float> entry = iterator.next();
            values.add(new Entry(entry.getKey(), entry.getValue()));
        }

        LineDataSet set1;
        // create a dataset and give it a type
        set1 = new LineDataSet(values, mSymbol);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataSets);

        // set data
        mChart.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

