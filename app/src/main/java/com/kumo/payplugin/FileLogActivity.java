package com.kumo.payplugin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
/**
 * 日志Activity
 */
public class FileLogActivity extends AppCompatActivity {
        private TextView mTextView;
        private Toolbar myToolbar;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.action_log);
                initView();
        }

        private void initView(){
                myToolbar= (Toolbar) findViewById(R.id.my_toolbar);
                setSupportActionBar(myToolbar);
                mTextView = (TextView) findViewById(R.id.tv_log);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.log, menu);
                return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.action_clearlog:
                                return true;
                        default:
                                return super.onOptionsItemSelected(item);
                }
        }
        @Override
        protected void onResume() {
                super.onResume();
        }

}
