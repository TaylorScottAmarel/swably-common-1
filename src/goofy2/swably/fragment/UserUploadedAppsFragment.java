package goofy2.swably.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class UserUploadedAppsFragment extends UserAppsFragment {

	@Override
	protected String getAPI() {
		return "/users/uploadees/";
	}
	
	@Override
	protected String getIdName(){
		return "share_id";
	}

}