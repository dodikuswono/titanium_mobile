package org.appcelerator.titanium.module.ui;
/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
import java.util.ArrayList;

import org.appcelerator.titanium.TitaniumModuleManager;
import org.appcelerator.titanium.api.ITitaniumCompositeView;
import org.appcelerator.titanium.api.ITitaniumView;
import org.appcelerator.titanium.config.TitaniumConfig;
import org.appcelerator.titanium.module.ui.widgets.TitaniumCompositeLayout;
import org.appcelerator.titanium.module.ui.widgets.TitaniumCompositeLayout.TitaniumCompositeLayoutParams;
import org.appcelerator.titanium.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class TitaniumCompositeView extends TitaniumBaseView
	implements ITitaniumCompositeView
{
	private static final String LCAT = "TiCompositeView";
	private static final boolean DBG = TitaniumConfig.LOGD;

	private static final int MSG_ADD_VIEW = 500;

	ArrayList<ViewHolder> views;
	TitaniumCompositeLayout tiLayout;

	class ViewHolder {
		public ViewHolder(ITitaniumView view, TitaniumCompositeLayout.TitaniumCompositeLayoutParams params) {
			this.index = views.size(); // original add index needed for ordering w/ z
			this.view = view;
			this.params = params;
		}

		int index;
		ITitaniumView view;
		TitaniumCompositeLayout.TitaniumCompositeLayoutParams params;
	}

	public TitaniumCompositeView(TitaniumModuleManager tmm)
	{
		super(tmm);
		this.views = new ArrayList<ViewHolder>();
	}

	@Override
	protected void processLocalOptions(JSONObject o) throws JSONException
	{
		openViewAfterOptions = true;
		openViewDelay = 0;
	}

	@Override
	protected void doOpen()
	{
		Context context = getContext();
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setFocusable(true);
		setFocusableInTouchMode(true);
		setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

		tiLayout = new TitaniumCompositeLayout(context);
		tiLayout.setFocusable(false);
		tiLayout.setFocusableInTouchMode(false);
		tiLayout.setClickable(false);

		addView(tiLayout);
	}

	public boolean handleMessage(Message msg)
	{
		boolean handled = super.handleMessage(msg);
		if (!handled) {
			switch(msg.what) {
			case MSG_ADD_VIEW :
				doAddView((String) msg.obj, msg.getData().getString("layout"));
				handled = true;
				break;
			}
		}
		return handled;
	}

	public void addView(String key, String layout) {
		Message msg = handler.obtainMessage(MSG_ADD_VIEW, key);
		msg.getData().putString("layout", layout);
		msg.sendToTarget();
	}

	public void doAddView(String key, String layout)
	{
		ITitaniumView tv = findViewByKey(key);
		if (tv != null) {
			TitaniumCompositeLayoutParams params = new TitaniumCompositeLayout.TitaniumCompositeLayoutParams();
			try {
				JSONObject o = new JSONObject(layout);
				if (o.has("zIndex")) {
					params.optionZIndex = o.getInt("zIndex");
				}
				if (o.has("left")) {
					params.optionLeft = o.getInt("left");
				}
				if (o.has("top")) {
					params.optionTop = o.getInt("top");
				}
				if (o.has("right")) {
					params.optionRight = o.getInt("right");
				}
				if (o.has("bottom")) {
					params.optionBottom = o.getInt("bottom");
				}
				if (o.has("width")) {
					params.optionWidth = o.getInt("width");
				}
				if (o.has("height")) {
					params.optionHeight = o.getInt("height");
				}
			} catch (JSONException e) {
				Log.e(LCAT, "Error processing layout object: " + layout, e);
			}

			params.index = views.size();
			views.add(new ViewHolder(tv,params));
			tv.postOpen();

			View v = tv.getNativeView();
			v.setLayoutParams(params);
			tiLayout.addView(v);

		} else {
			Log.w(LCAT, "TitaniumView not found for key: " + key);
		}
	}

	@Override
	public void dispatchWindowEvent(String eventName, String eventData) {
		for(ViewHolder holder : views) {
			holder.view.dispatchWindowEvent(eventName, eventData);
		}
	}

	@Override
	protected View getContentView() {
		return null;
	}
}
