package org.masonry;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class MasonryActivity extends Activity {
	
	private static final int[] ITEM_IDS = {
		R.layout.small_item, 
		R.layout.small_item,
		R.layout.small_item,
		R.layout.small_item,
		R.layout.small_item,
		R.layout.wide_item,
		R.layout.wide_item, 
		R.layout.long_item,
		R.layout.long_item,
		R.layout.large_item
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masonry);
        findViewById(R.id.toggle).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
        final Masonry masonry = (Masonry) findViewById(R.id.masonry);
        findViewById(R.id.prepend).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				masonry.addView(itemView(masonry), 0);
			}
		});
        findViewById(R.id.append).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				masonry.addView(itemView(masonry));
				scrollView.postDelayed(new Runnable() {
					@Override
					public void run() {
						scrollView.smoothScrollTo(0, masonry.getHeight());
					}
				}, masonry.getDuration());
			}
		});
        findViewById(R.id.clear).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				masonry.removeAllViews();
			}
		});
    }
    
    private int item(){
    	return ITEM_IDS[Double.valueOf(Math.floor(Math.random() * ITEM_IDS.length)).intValue()];
    }
    
    private View itemView(ViewGroup masonry){
    	return getLayoutInflater().inflate(item(), masonry, false);
    }
    
}