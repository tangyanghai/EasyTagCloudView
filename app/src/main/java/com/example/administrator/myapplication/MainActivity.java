package com.example.administrator.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.myapplication.view.TagCloudView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TagCloudView mTagView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTagView = findViewById(R.id.tag_view);
        //配置数据
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            tags.add("第"+(i+1)+"条");
        }
        //生成adapter
        TagAdapter tagAdapter = new TagAdapter(tags);
        //设置adapter
        mTagView.setAdapter(tagAdapter);
    }


    /**
     *  标签云的adapter
     */
    class TagAdapter extends TagCloudView.TagAdapter<String,TagCloudView.ViewHolder>{

        public TagAdapter(List<String> mDatas) {
            super(mDatas);
        }

        @Override
        public TagCloudView.ViewHolder createViewHolder(int position) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_tag,null);
            return new TagCloudView.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TagCloudView.ViewHolder holder, final int position, String bean) {
            View item = holder.getItem();
            TextView tag = item.findViewById(R.id.tag);
            tag.setText(bean);
            tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeTag(position);
                }
            });
        }
    }


}
