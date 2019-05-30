package com.kumo.payplugin;
import android.os.Bundle;
import  android.os.Build;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

/**
 * 设置Activity
 */
public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fragment动态加载
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    /**
     * 通过使用XML文件来创建各个首选项的视图层级
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }

}
