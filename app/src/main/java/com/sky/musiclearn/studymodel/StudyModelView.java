package com.sky.musiclearn.studymodel;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.sky.musiclearn.R;
import com.sky.musiclearn.ui.MidiPianoBottomLayout;
import com.sky.musiclearn.ui.MidiPianoTopLayout;
import com.sky.musiclearn.utils.MusicLearnConfig;

/**
 * Created by Sky000 on 2016/8/26.
 */
public class StudyModelView extends FrameLayout{
    Context context;
    VisualizerView visualizerView;
    MidiPianoBottomLayout bottomLayout;
    MidiPianoTopLayout topLayout;
    public StudyModelView(Context context){
        super(context);
        this.context = context;
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setBackgroundResource(R.drawable.personal_model_bg);

        /*topLayout = new MidiPianoTopLayout(context);
        LayoutParams topLayoutParams = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        topLayout.setLayoutParams(topLayoutParams);
        addView(topLayout);*/

        visualizerView = new VisualizerView(context);
        LayoutParams visualizerViewParams = new LayoutParams( LayoutParams.MATCH_PARENT, MusicLearnConfig.getResolutionValue(300));
        visualizerViewParams.topMargin = MusicLearnConfig.getResolutionValue(100);
        visualizerView.setLayoutParams(visualizerViewParams);
        //将频谱View添加到布局
        addView(visualizerView);

        bottomLayout = new MidiPianoBottomLayout(context);
        LayoutParams bottomLayoutParams = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
        bottomLayoutParams.topMargin = MusicLearnConfig.getResolutionValue(1080-300-24);
        bottomLayout.setLayoutParams(bottomLayoutParams);
        addView(bottomLayout);
    }

    public VisualizerView getVisualizerView(){
        return visualizerView;
    }

}
