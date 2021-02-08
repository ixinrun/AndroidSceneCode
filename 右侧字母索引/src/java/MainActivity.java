package com.bigrun.assortdemo;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bigrun.assortdemo.adapter.CarBrandsAdapter;
import com.bigrun.assortdemo.assortpinyin.AssortPinyinList;
import com.bigrun.assortdemo.assortpinyin.AssortView;
import com.bigrun.assortdemo.assortpinyin.LanguageComparator_CN;
import com.bigrun.assortdemo.model.CarImagesBean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private CarBrandsAdapter adapter;
    private ExpandableListView eListView;
    private AssortView assortView;
    // 所有指示标记集合（ExpandList父级数据）
    private List<String> strList = new ArrayList<>();
    //分组后的数据
    private List<List<CarImagesBean>> childs = new ArrayList<>();
    // 中文转汉语拼音
    private AssortPinyinList assort = new AssortPinyinList();
    // 中文排序
    private LanguageComparator_CN cnSort = new LanguageComparator_CN();

    private List<CarImagesBean> CarImagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (CarImagesList.size()<=0){
            setCarImagesList("allcar.json");
        }
        initView();
        //排序
        sort();

        adapter = new CarBrandsAdapter(this, strList, childs);
        eListView.setAdapter(adapter);

        // 这里要展开所有
        for (int i = 0, length = adapter.getGroupCount(); i < length; i++) {
            eListView.expandGroup(i);
        }

        // 字母按键回调
        assortView.setOnTouchAssortListener(new AssortView.OnTouchAssortListener() {

            View layoutView = LayoutInflater.from(MainActivity.this).inflate(R.layout.assortview_popwindow_layout, null);
            TextView text = (TextView) layoutView.findViewById(R.id.content);
            PopupWindow popupWindow;

            public void onTouchAssortListener(String str) {

                for (int i = 0; i < strList.size(); i++) {
                    if (str.equals(strList.get(i))) {
                        eListView.setSelectedGroup(i);
                        if (popupWindow == null) {
                            popupWindow = new PopupWindow(layoutView, 180, 180, false);
                            popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        }
                        text.setText(str);
                    }
                }
            }

            public void onTouchAssortUP() {
                if (popupWindow != null)
                    popupWindow.dismiss();
                popupWindow = null;
            }
        });


    }

    private void initView() {
        eListView = (ExpandableListView) findViewById(R.id.elist);
        assortView = (AssortView) findViewById(R.id.assort_view);
        eListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                CarImagesBean carImagesBean = childs.get(groupPosition).get(childPosition);
                Toast.makeText(MainActivity.this, carImagesBean.getDDValue(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void sort() {
        //设置ExpandListview父类数据
        boolean isSpecial = false;
        for (int i = 0; i < CarImagesList.size(); i++) {
            String initial = CarImagesList.get(i).getInitial();
            if (!assort.getFirstChar(initial).equals("#")){
                strList.add(assort.getFirstChar(initial));
            }else {
                isSpecial = true;
            }
        }
        strList = removeDuplicateWithOrder(strList);
        Collections.sort(strList, cnSort); //排序
        if (isSpecial){
            strList.add("#");
        }
        //根据已处理的父类数据进行分组
        for (int i = 0; i < strList.size(); i++) {
            List<CarImagesBean> list2 = new ArrayList<>();
            for (int j = 0; j < CarImagesList.size(); j++) {
                CarImagesBean carImagesBean = CarImagesList.get(j);
                if (strList.get(i).equals(CarImagesList.get(j).getInitial())) {
                    list2.add(carImagesBean);
                }
            }
            childs.add(list2);
        }
    }

    //添加车型图片数据
    public void setCarImagesList(String fileName) {
        CarImagesList.clear();
        StringBuilder sb = new StringBuilder();
        AssetManager am = getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sb.delete(0, sb.length());
        }

        try {
            JSONArray jsonArray = new JSONArray(sb.toString().trim());
            if (jsonArray.length() > 0) {
                Gson gson = new Gson();
                for (int i = 0; i < jsonArray.length(); i++) {
                    CarImagesBean carImagesBean = gson.fromJson(jsonArray.get(i).toString(), CarImagesBean.class);
                    CarImagesList.add(carImagesBean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //去重
    public List removeDuplicateWithOrder(List list) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        return newList;
    }

}
