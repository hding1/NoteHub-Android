package com.cs48.g15.notehub.SwipeListView;

/**
 * Created by peterding on 3/9/18.
 */

import android.view.View;

public interface OnSwipeListItemClickListener {
    public void OnClick(View view, int index);
    public boolean OnLongClick(View view, int index);
    public void OnControlClick(int rid,View view,int index);
}
