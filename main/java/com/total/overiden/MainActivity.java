package com.total.overiden;

import android.media.SoundPool;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.total.overide.OVMtfReader;
import com.total.overiden.databinding.ActivityMainBinding;
import com.total.scenario.ScenarioDB;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private SoundPool soundPool;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public static MainActivity currentActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OVMtfReader.readWeapons(getResources().openRawResource(R.raw.ovweapons));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        currentActivity = this;
        ActionBar bar = getSupportActionBar();
        if (bar!=null){
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayShowHomeEnabled(false);   //disable back button
            bar.setHomeButtonEnabled(false);
        }

        soundPool = (new SoundPool.Builder()).build();
        loadGameSounds();
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            rehide = true;
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });
    }
private boolean rehide = false;
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (rehide) {
            View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        View decorView = getWindow().getDecorView();
//// Hide both the navigation bar and the status bar.
//// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//// a general rule, you should design your app to hide the status bar whenever you
//// hide the navigation bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//    }

    public enum Sounds{
        LASER,
        PPC,
        CANNON,
        MISSILE,
        FALL,
        LOSTARM,
        WITHDRAW,
        GAUSS,
        PULSE,
        COCKED,
        BOOM,
        ALPHA,
        CLICK;
    }
    private int laser = -1, ppc = -1,cannon = -1, missile = -1, boom = -1, fall = -1,
            lostArm = -1, withdraw = -1, gauss = -1, pulse = -1, cocked = -1, alpha = -1,
    click = -1;
    private void loadGameSounds(){
        laser = soundPool.load(this,R.raw.laser_shot,1);
        pulse = soundPool.load(this,R.raw.pulse_laser,1);
        ppc = soundPool.load(this,R.raw.ppc_s,1);
        cannon = soundPool.load(this,R.raw.cannon_s,1);
        missile = soundPool.load(this,R.raw.missile_s,1);
        boom = soundPool.load(this,R.raw.boom_s,1);
        fall = soundPool.load(this,R.raw.mechdown,1);
        lostArm = soundPool.load(this,R.raw.armsoff,1);
        withdraw = soundPool.load(this,R.raw.runaway,1);
        gauss = soundPool.load(this,R.raw.railgun,1);
        cocked = soundPool.load(this,R.raw.cocked,1);
        alpha = soundPool.load(this,R.raw.alpha,1);
        click = soundPool.load(this,R.raw.click,1);
    }
    public void playSound(Sounds pick){
        switch (pick){
            case LASER:
                if (laser>0)soundPool.play(laser,1,1,1,0,1f);
                break;
            case PPC:
                if (ppc>0)soundPool.play(ppc,1,1,1,0,1f);
                break;
            case CANNON:
                if (cannon > 0) soundPool.play(cannon, 1, 1, 1, 0, 1f);
                break;
            case MISSILE:
                if (missile > 0) soundPool.play(missile, 1, 1, 1, 0, 1f);
                break;
            case BOOM:
                if (boom > 0) soundPool.play(boom, 1, 1, 1, 0, 1f);
                break;
            case FALL:
                if (fall > 0) soundPool.play(fall, 1, 1, 1, 0, 1f);
                break;
            case LOSTARM:
                if (lostArm > 0) soundPool.play(lostArm, 1, 1, 1, 0, 1f);
                break;
            case WITHDRAW:
                if (withdraw > 0) soundPool.play(withdraw, 1, 1, 1, 0, 1f);
                break;
            case GAUSS:
                if (gauss > 0) soundPool.play(gauss, 1, 1, 1, 0, 1f);
                break;
            case PULSE:
                if (pulse > 0) soundPool.play(pulse, 1, 1, 1, 0, 1f);
                break;
            case COCKED:
                if (cocked > 0) soundPool.play(cocked, 1, 1, 1, 0, 1f);
                break;
            case ALPHA:
                if (alpha > 0) soundPool.play(alpha, 1, 1, 1, 0, 1f);
                break;
                case CLICK:
                if (click > 0) soundPool.play(click, 1, 1, 1, 0, 1f);
                break;
        }
    }

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.ingamemenu, menu);
        setOptionsMenu(false);
        return true;
    }

    public void setOptionsMenu(boolean inGame) {
        if (menu != null) {
            menu.findItem(R.id.end_game).setVisible(inGame);
            menu.findItem(R.id.resync_turn).setVisible(inGame);
            menu.findItem(R.id.sound).setVisible(inGame);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.end_game) {
            // get model and end game - update the DB
            TurnViewModel model = new ViewModelProvider(this).get(TurnViewModel.class);
            model.getGame().endGame(this);
            Navigation.findNavController(this, R.id.nav_host_fragment_content_main).popBackStack();
            return true;
        } else if (id == R.id.resync_turn){
            TurnViewModel model = new ViewModelProvider(this).get(TurnViewModel.class);
            if (model.getGame().getComms()!=null){
                // there is another device available to sync with
                UpdatePlayerActions.syncFullTurn(model.getGame());
            }
        } else if (id == R.id.sound){
            Game.current.setSoundEffects(!Game.current.isSoundEffects());
            item.setChecked(Game.current.isSoundEffects());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateScreenContents(){
        //called primarily when a bluetooth data packet arrives to update the current screen with
        // new data
        for (Fragment currentFrag : getSupportFragmentManager().getFragments()) {
            if (currentFrag instanceof IBluetoothMessage)
                ((IBluetoothMessage) currentFrag).updateScreen();
            else if (currentFrag instanceof NavHostFragment){
                if (currentFrag.isAdded()) {
                    for (Fragment child : currentFrag.getChildFragmentManager().getFragments()) {
                        if (child instanceof IBluetoothMessage) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    ((IBluetoothMessage) child).updateScreen();
                                }
                            });
                        }
                    }
                } else System.out.println("FAILED as currentFrag is not attached");
            }
        }
    }
}