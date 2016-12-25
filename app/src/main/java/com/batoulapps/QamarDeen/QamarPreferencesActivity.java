package com.batoulapps.QamarDeen;


import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.batoulapps.QamarDeen.ui.fragments.QamarPreferencesFragment;

public class QamarPreferencesActivity extends AppCompatActivity {
  private QamarPreferencesFragment fragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.preferences);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setTitle(R.string.settings_menu);
    }

    final FragmentManager fm = getFragmentManager();
    fragment = (QamarPreferencesFragment) fm.findFragmentById(R.id.content);
    if (fragment == null) {
      fragment = new QamarPreferencesFragment();
      fm.beginTransaction()
          .replace(R.id.content, fragment)
          .commit();
    }
  }

  @Override
  public void onBackPressed() {
    leave();
  }

  private void leave() {
    if (fragment != null && fragment.needsRestart()) {
      Intent i = new Intent(this, QamarDeenActivity.class);
      i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(i);
      finish();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      leave();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
