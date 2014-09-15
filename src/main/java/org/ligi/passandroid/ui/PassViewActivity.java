package org.ligi.passandroid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.ligi.axt.AXT;
import org.ligi.passandroid.App;
import org.ligi.passandroid.R;
import org.ligi.passandroid.helper.PassVisualizer;
import org.ligi.passandroid.maps.PassbookMapsFacade;
import org.ligi.passandroid.model.Pass;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PassViewActivity extends PassViewActivityBase {

    @OnClick(R.id.barcode_img)
    void onBarcodeClick() {
        AXT.at(this).startCommonIntent().activityFromClass(FullscreenBarcodeActivity.class);
    }

    @InjectView(R.id.barcode_img)
    ImageView barcode_img;

    @InjectView(R.id.logo_img)
    ImageView logo_img;

    @InjectView(R.id.thumbnail_img)
    ImageView thumbnail_img;

    @InjectView(R.id.back_fields)
    TextView back_tv;

    @InjectView(R.id.main_fields)
    TextView front_tv;

    @InjectView(R.id.barcode_alt_text)
    TextView barcodeAlternatvieText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!optionalPass.isPresent()) {
            return;
        }

        AXT.at(this).disableRotation();

        final Pass pass = optionalPass.get();

        if (!pass.isValid()) { // don't deal with invalid passes
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.pass_problem))
                    .setTitle(getString(R.string.problem))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNeutralButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new ExportProblemPassToLigiAndFinishTask(PassViewActivity.this, pass.getId(), App.getShareDir(), "share.pkpass").execute();
                        }
                    })
                    .show();
            return;
        }

        final View contentView = getLayoutInflater().inflate(R.layout.activity_pass_view, null);
        setContentView(contentView);

        ButterKnife.inject(this);

        if (pass.getBarCode().isPresent()) {
            final int smallestSide = AXT.at(getWindowManager()).getSmallestSide();
            final Bitmap bitmap = pass.getBarCode().get().getBitmap(smallestSide / 3);
            setBitmapSafe(barcode_img, Optional.fromNullable(bitmap));
            if (pass.getBarCode().get().getAlternativeText().isPresent()) {
                barcodeAlternatvieText.setText(pass.getBarCode().get().getAlternativeText().get());
                barcodeAlternatvieText.setVisibility(View.VISIBLE);
            } else {
                barcodeAlternatvieText.setVisibility(View.GONE);
            }
        } else {
            setBitmapSafe(barcode_img, Optional.<Bitmap>absent());
        }

        setBitmapSafe(logo_img, pass.getLogoBitmap());

        logo_img.setBackgroundColor(pass.getBackGroundColor());

        setBitmapSafe(thumbnail_img, pass.getThumbnailImage());

        if (findViewById(R.id.map_container) != null) {
            if (!(pass.getLocations().size() > 0 && PassbookMapsFacade.init(this))) {
                findViewById(R.id.map_container).setVisibility(View.GONE);
            }
        }

        if (pass.getType() != null) {
            String front_str = "";
            front_str += PassVisualizer.getFieldListAsString(pass.getHeaderFields());
            front_str += PassVisualizer.getFieldListAsString(pass.getPrimaryFields());
            front_str += PassVisualizer.getFieldListAsString(pass.getSecondaryFields());
            front_str += PassVisualizer.getFieldListAsString(pass.getAuxiliaryFields());

            front_tv.setText(Html.fromHtml(front_str));
        }
        String back_str = "";

        if (App.isDeveloperMode()) {
            back_str += getPassDebugInfo(pass);
        }

        back_str += PassVisualizer.getFieldListAsString(pass.getBackFields());

        back_tv.setText(Html.fromHtml(back_str));

        Linkify.addLinks(back_tv, Linkify.ALL);
        PassVisualizer.visualize(this, pass, contentView);
    }

    private static void setBitmapSafe(ImageView imageView, Optional<Bitmap> bitmapOptional) {

        if (bitmapOptional.isPresent()) {
            imageView.setImageBitmap(bitmapOptional.get());
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

    }

    private String getPassDebugInfo(Pass pass) {

        String result = ""; // TODO bring back sth like passbook.getPlainJsonString();

        for (File f : new File(pass.getId()).listFiles()) {
            result += f.getName() + "<br/>";
        }

        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean res = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_map).setVisible((optionalPass.isPresent() && optionalPass.get().isValid() && optionalPass.get().getLocations().size() > 0));
        return res;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            final Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities();
                finish();
            } else {
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

}
