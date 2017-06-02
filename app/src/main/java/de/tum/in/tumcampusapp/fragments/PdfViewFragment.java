package de.tum.in.tumcampusapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import com.github.barteksc.pdfviewer.PDFView;

import de.tum.in.tumcampusapp.R;

public class PdfViewFragment extends Fragment {
    private LayoutInflater inflater;
    private PDFView pdfView;
    private File pdf;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = this.inflater.inflate(R.layout.fragment_pdf_view, container, false);
        pdfView = (PDFView) view.findViewById(R.id.pdf_view);
        pdfView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){
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

    public boolean setPdf(File pdf){
        if (isFilePdf(pdf)){
            this.pdf = pdf;
            return true;
        }
        return false;
    }

    private boolean isFilePdf(File file){
        BufferedReader bfr = null;
        try{
            bfr = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        int curByte;
        try {
            String firstFourChars = "";
            for (int i = 0; ((curByte = bfr.read()) != -1) && i < 4; i++){
                firstFourChars += (char) curByte;
            }
            return firstFourChars.equals("%PDF");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
