package com.bigrun.assortdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigrun.assortdemo.R;
import com.bigrun.assortdemo.common.DataManager;
import com.bigrun.assortdemo.model.CarImagesBean;

import java.util.List;

public class CarBrandsAdapter extends BaseExpandableListAdapter {
    // 所有指示标记集合
    private List<String> strList;
    //所有子类
    private List<List<CarImagesBean>> childs;

    private Context context;
    private LayoutInflater inflater;

    DataManager dataManager;

    public CarBrandsAdapter(Context context, List<String> strList, List<List<CarImagesBean>> childs) {
        super();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.strList = strList;
        this.childs = childs;
        dataManager = DataManager.getInstance(context);
    }

    /**
     * 针对父级
     *
     * @param group
     * @return
     */
    public Object getGroup(int group) {
        // TODO Auto-generated method stub
        return strList.get(group);
    }

    public int getGroupCount() {
        // TODO Auto-generated method stub
        return strList.size();
    }

    public long getGroupId(int group) {
        // TODO Auto-generated method stub
        return group;
    }

    public View getGroupView(int group, boolean arg1, View contentView, ViewGroup arg3) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.assort_group_item, null);
            contentView.setClickable(true);
        }
        TextView textView = (TextView) contentView.findViewById(R.id.name);
        textView.setText(strList.get(group));
        return contentView;
    }

    /**
     * 针对儿子
     *
     * @param group
     * @param child
     * @return
     */
    public Object getChild(int group, int child) {
        // TODO Auto-generated method stub
        return childs.get(group).get(child);
    }

    public int getChildrenCount(int group) {
        // TODO Auto-generated method stub
        return childs.get(group).size();
    }

    public long getChildId(int group, int child) {
        // TODO Auto-generated method stub
        return child;
    }

    public View getChildView(int group, int child, boolean arg2, View contentView, ViewGroup arg4) {
        // TODO Auto-generated method stub
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.carbrands_item, null);
        }

        TextView textView = (TextView) contentView.findViewById(R.id.carbrand_name);
        textView.setText(childs.get(group).get(child).getDDValue());
        ImageView imageView = (ImageView) contentView.findViewById(R.id.carbrand_img);
        Bitmap bitmap = dataManager.getImageFromAssetsFile("carimgs/" + childs.get(group).get(child).getDDCode() + ".png");
        if (bitmap!=null){
            imageView.setImageBitmap(bitmap);
        }else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        return contentView;
    }

    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return true;
    }

}
