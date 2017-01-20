package com.presto.p2048.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.presto.p2048.R;
import com.presto.p2048.modal.SavedGameEntity;
import com.presto.p2048.util.SavedGameHelper;

import java.util.List;

public class SavedGamesActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "SavedGamesActivity";
    private List<SavedGameEntity> savedGameList;
    private ArrayAdapter<SavedGameEntity> aa;
    private ListView listView;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);
        listView = (ListView) findViewById(R.id.listItems);
        loadingText = (TextView) findViewById(R.id.loadingText);
        initList();
    }

    private void initList() {
        savedGameList = new SavedGameHelper(this).getSavedGames();
        if (savedGameList.size() == 0) {
            loadingText.setText(getString(R.string.no_saved_game));
            return;
        }
        aa = new ArrayAdapter<SavedGameEntity>(this, android.R.layout.simple_list_item_1, savedGameList);
        listView.setAdapter(aa);
        listView.setOnItemClickListener(this);
        loadingText.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // SavedGamesActivity.this.listView.setSelection(position);
        //view.setSelected(true);
        Object obj = listView.getItemAtPosition(position);

        if ((obj instanceof SavedGameEntity)) {
            SavedGameEntity savedGame = (SavedGameEntity) obj;
            launchNewGame(savedGame.prefName);
        } else {
            Log.w(TAG, "obj in onItemClick is not of type SavedGameEntity obj = " + obj);
        }
    }

    private void launchNewGame(String sharedPrefName) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.SHARED_PREF_ID, sharedPrefName);
        startActivity(intent);
    }
}
