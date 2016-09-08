package hr.nas2skupa.eleventhhour.ui;

import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;

@EActivity(R.layout.activity_category)
@OptionsMenu(R.menu.main)
public class CategoryActivity extends AppCompatActivity {


    public final static String EXTRA_TITLE_ID = "hr.nas2skupa.eleventhhour.TITLE_ID";
    public final static String EXTRA_ICON_ID = "hr.nas2skupa.eleventhhour.ICON_ID";
    public final static String EXTRA_COLOR_ID = "hr.nas2skupa.eleventhhour.COLOR_ID";

    void setToolbar(@ViewById(R.id.app_bar) AppBarLayout appBar, @ViewById(R.id.toolbar) Toolbar toolbar) {

        appBar.setBackgroundResource(getIntent().getIntExtra(EXTRA_COLOR_ID, R.color.colorPrimary));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @ViewById(R.id.txt_category_name)
    void setName(TextView textView) {
        textView.setText(getIntent().getIntExtra(EXTRA_TITLE_ID, -1));
    }

    @ViewById(R.id.img_category_icon)
    void setIcon(ImageView icon) {
        icon.setImageResource(getIntent().getIntExtra(EXTRA_ICON_ID, -1));
        icon.setBackgroundResource(getIntent().getIntExtra(EXTRA_COLOR_ID, R.color.colorPrimary));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
