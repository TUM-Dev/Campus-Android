package de.tum.in.tumcampusapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import de.tum.in.tumcampusapp.R;

public class PdfViewFragment extends Fragment {
    private LayoutInflater inflater;
    private PDFView pdfView;
    private File pdf;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = this.inflater.inflate(R.layout.fragment_pdf_view, container, false);
        pdfView = (PDFView) view.findViewById(R.id.pdf_view);
        pdfView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                pdfView.fitToWidth();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        pdfView.fromFile(pdf).load();
    }

    public void setPdf(File pdf){
        this.pdf = pdf;
    }
}
