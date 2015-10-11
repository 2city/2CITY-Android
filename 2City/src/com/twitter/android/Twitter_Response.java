package com.twitter.android;

import twitter4j.User;
import twitter4j.auth.AccessToken;

public interface Twitter_Response {
	public void getTwitterResponse(int code, String response);
	public void getTwitterUser(User user, AccessToken accessToken );
//	public void getAccessToken(AccessToken accessToken );
}
