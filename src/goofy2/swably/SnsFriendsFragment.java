package goofy2.swably;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import goofy2.swably.R;
import goofy2.swably.fragment.CloudUsersFragment;
import goofy2.utils.JSONUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SnsFriendsFragment extends CloudUsersFragment {
	OnInviteListener mCallback;
	protected View btnFollowAll;
	protected View btnHeaderInvite;
	protected View btnInvite;
//	protected View viewFollow;
	protected View bottomLine;
	String mSnsId;

    // Container Activity must implement this interface
    public interface OnInviteListener {
        public void onInvite();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnInviteListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnInviteListener");
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSnsId = Utils.getCurrentUser(a()).optString("signup_sns");
        super.onCreate(savedInstanceState);
  }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        btnFollowAll = v.findViewById(R.id.btnFollowAll);
	    btnFollowAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				followAll();
			}
		});
        bind(v);
        return v;
    }
    
    @Override
    protected void setContent(){
	    setContentView(R.layout.sns_users_fragment);
    }

    protected void bind(View v){
//	    viewFollow = v.findViewById(R.id.viewFollow);
//	    btnFollow = v.findViewById(R.id.btnFollow);
//	    btnFollow.setOnClickListener(new OnClickListener_btnFollow());
	    
//	    String name = getIntent().getStringExtra("name");
//	    TextView tv = (TextView) findViewById(R.id.txtTitle);
//    	final String sns_id = getIntent().getDataString();

//    	int iconId = (Integer) Utils.getSnsResource(sns_id, "icon");
//    	Drawable d = this.getResources().getDrawable(iconId);
//    	d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
//    	tv.setCompoundDrawables(d, null, null, null);

	    TextView tv;
    	String name = (String) Utils.getSnsResource(mSnsId, "name");

    	tv = (TextView) v.findViewById(R.id.txtZero);
	    tv.setText(String.format(getString(R.string.zero_friend), name));
	    
        OnClickListener oc = new OnClickListener(){
			@Override
			public void onClick(View arg0) {
//				startActivity(new Intent(SnsFriendsFragment.this, Invite.class));
				mCallback.onInvite();
			}
        };
        btnInvite = v.findViewById(R.id.btnInvite);
        btnInvite.setOnClickListener(oc);
//        btnHeaderInvite = this.findViewById(R.id.btnHeaderInvite);
//        if(btnHeaderInvite != null) btnHeaderInvite.setOnClickListener(oc);

//        TextView txtInvitesLeft = (TextView)this.findViewById(R.id.txtInvitesLeft);
//        txtInvitesLeft.setText(Utils.getCurrentUser(this).optString("invites_left"));
        
    }

	protected String getUrl() {
		return Const.HTTP_PREFIX + "/connections/find_friends/"+mSnsId+"?format=json&"+ca().getLoginParameters()+"&"+ca().getClientParameters();
//		return Const.HTTP_PREFIX + "/connections/invite_friends/"+id+"?format=json&"+getLoginParameters();
	}

    static public String getContactsEmails(Context context)
    {
    	Log.v(Const.APP_NAME, Const.APP_NAME + " SnsFriends getContactsEmails: start");
		ContentResolver cr = context.getContentResolver();
		String[] mProjection =
		{
			ContactsContract.CommonDataKinds.Email.DATA
		};
		Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, mProjection, null, null, null);
		StringBuilder sb = new StringBuilder();
		int count = 0;
//		while (cursor.moveToNext() && count < 2000)   
		while (cursor.moveToNext())   
		{
			String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			sb.append(email);
			sb.append(",");
			count ++;
		}
    	Log.v(Const.APP_NAME, Const.APP_NAME + " SnsFriends getContactsEmails: " + cursor.getCount());
//    	Log.v(Const.APP_NAME, Const.APP_NAME + " SnsFriends getContactsEmails: " + sb.toString());
    	cursor.close();
    	
		return sb.toString();    	
    }
    
    String mContactsEmails = null;
    @Override
	protected String loadStream(String url, String lastId) {
		Log.d(Const.APP_NAME, Const.APP_NAME + " SnsFriendsFragment loadStream: " + lastId);
    	if(lastId != null)	url += "&max_id=" + lastId;
		String err = null;
		String strResult = null;
		try{
	    	if(mContactsEmails == null) mContactsEmails = getContactsEmails(a());
			HttpPost httpReq = new HttpPost(url);
			List <NameValuePair> params = new ArrayList <NameValuePair>();
			params.add(new BasicNameValuePair("contacts", mContactsEmails));
			httpReq.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, Const.HTTP_TIMEOUT);
			httpReq.setParams(httpParameters);
			HttpResponse httpResp = new DefaultHttpClient().execute(httpReq);
			strResult = EntityUtils.toString(httpResp.getEntity());
			int code = httpResp.getStatusLine().getStatusCode(); 
			if( code == 200){
				Log.d(Const.APP_NAME, Const.APP_NAME + " SnsFriendsFragment loadStream ok: " + lastId);
				mData = strResult;
				//mListData = JSONUtils.appendArray(mListData, getListArray(strResult));
				JSONArray loaded = getListArray(strResult);
				mLastLoaded = loaded.length();
		    	if(lastId == null){
		    		mLoadingImages.clear();
		    		mListData = loaded;
		    	}else{
					mListData = JSONUtils.appendArray(mListData, loaded);
		    	}
			}else{
				onHttpError(strResult, code);
			}
		}catch (Exception e){
//	    	Utils.alertTitle(this, getString(R.string.err_no_network_title), e.getMessage());
			err = e.getMessage();
			Log.e(Const.APP_NAME, Const.APP_NAME + " SnsFriendsFragment loadStream err: " + err);
		}
		return err;
	}

	@Override
	protected void onClickHeader() {
		// TODO Auto-generated method stub

	}

	@Override
	protected View getListHeader() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
	protected void loadMore(){
		// disable auto loading
	}	
	@Override
    protected void loadedMore(boolean succeeded){
		//txtHeader.setText(String.format(getString(R.string.app_count), mListData.length()));
		View viewZero = getView().findViewById(R.id.viewZero);
//		View viewButtonBar = findViewById(R.id.viewButtonBar);
		if(mListData.length() == 0){
//if(mListData.length() != 0){
			viewZero.setVisibility(View.VISIBLE);
//			viewFollow.setVisibility(View.GONE);
			if(btnHeaderInvite != null) btnHeaderInvite.setVisibility(View.GONE);
			mList.setVisibility(View.GONE);
//			if(viewButtonBar != null) viewButtonBar.setVisibility(View.GONE);
		}else{
			viewZero.setVisibility(View.GONE);
//			viewFollow.setVisibility(View.VISIBLE);
			if(btnHeaderInvite != null) btnHeaderInvite.setVisibility(View.VISIBLE);
			mList.setVisibility(View.VISIBLE);
//			if(viewButtonBar != null) viewButtonBar.setVisibility(View.VISIBLE);
		}
			
    }

	@Override
	protected CharSequence getListTitle() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
	public long getCacheExpiresIn(){
		return cacheExpiresIn(); 
	}

    static public long cacheExpiresIn(){
		return 3600*1000; 
    }

    @Override
    public String getCacheId(){
    	return cacheId();
    }

    static public String cacheId(){
    	return SnsFriendsFragment.class.getName();
    }

}
