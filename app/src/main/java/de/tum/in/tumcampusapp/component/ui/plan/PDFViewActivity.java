package de.tum.in.tumcampusapp.component.ui.plan;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.IOException;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 *  Activity that shows the pdf given to it via Const.PDF_PATH .
 *  It shows the Title (Const.PDF_TITLE) in the ActionBar if a title is given.
 */
public class PDFViewActivity extends BaseActivity {
    private static final int MAX_RENDER_SIZE = 4000;

    public PDFViewActivity() {
        super(R.layout.activity_zoomable_image);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            showError();
            return;
        }

        String title = extras.getString(Const.PDF_TITLE);
        if(title != null && !title.isEmpty() && getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
        }

        String path = extras.getString(Const.PDF_PATH);
        if (path == null) {
            showError();
            return;
        }
        showPDF(new File(path));
    }

    /**
     * Renders and displays the pdf in the (zoomable) ImageView
     * @param pdf
     */
    public void showPDF(File pdf) {
        try {
            ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
            // create a new renderer
            PdfRenderer renderer = new PdfRenderer(descriptor);

            // let us just render all pages
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = createBitmap(page);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                PhotoView image = findViewById(R.id.zoomable_image);
                image.setImageBitmap(bitmap);
                image.setMaximumScale(10);
                // close the page
                page.close();
            }
            // close the renderer
            renderer.close();

        } catch (IOException e){
            showError();
        }
    }

    /**
     * Determines the size of the bitmap. Will return a high res image
     * but will try to prevent too large images for the phone to handle by setting the max size to 2000 px
     * @return empty Bitmap with 4 * pageSize, but max MAX_RENDER_SIZE.
     */
    private Bitmap createBitmap(PdfRenderer.Page page){
        int width, height;
        if(page.getHeight() > page.getWidth()){
            // portrait image
            height = Math.min(page.getHeight()*4, MAX_RENDER_SIZE);
            width = height * page.getWidth() / page.getHeight();
        } else {
            // landscape image
            width = Math.min(page.getWidth()*4, MAX_RENDER_SIZE);
            height = width * page.getHeight() / page.getWidth();
        }
        Utils.log(page.getWidth() + " x " + page.getHeight() + " -> " + width + " x " + height);
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    /**
     * Shows an error so the user doesn't just stare at a blank space if something is wrong
     */
    private void showError(){
        findViewById(R.id.zoomable_image).setVisibility(View.GONE);
        findViewById(R.id.error_retry).setVisibility(View.GONE); // we can't retry since we don't have the download information
    }
}
