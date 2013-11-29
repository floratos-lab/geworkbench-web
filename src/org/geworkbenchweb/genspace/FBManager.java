package org.geworkbenchweb.genspace;

import java.util.Date;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;
import com.restfb.types.User;

public class FBManager {
	
	private String token;
	
	private FacebookClient fbManager;
	
	private Connection<Post> myFeeds;
	
	private GenSpaceLogin_1 login;
	
	private User me;
	
	private String workbench = "http://wiki.c2b2.columbia.edu/workbench/index.php/Home";
	
	public FBManager(String token, GenSpaceLogin_1 login2) {
		this.token = token;
		this.login = login2;
	}
	
	public boolean connect() {
		if (this.token == null || this.token.isEmpty()) {
			return false;
		}
		
		try {
			this.fbManager = new DefaultFacebookClient(this.token);
			this.me = this.fbManager.fetchObject("me", User.class);
			this.myFeeds = this.fbManager.fetchConnection("me/feed", Post.class);
			return true;
		} catch (FacebookOAuthException e) {
			return false;
		}
	}
	
	public void publishAnalysisResult(String searchString, String comment) {

		Post searchPost = this.searchExistingPost(searchString);
		if (searchPost == null) {
			FacebookType publishMsg = this.fbManager.publish("me/feed", FacebookType.class, Parameter.with("message", searchString), Parameter.with("link", workbench));
			
			if (!comment.isEmpty() || comment != null) {
				this.fbManager.publish(publishMsg.getId() + "/comments", String.class, Parameter.with("message", comment));
			}
			
		} else {
			/*System.out.println("The post exists already!");
			System.out.println(searchPost.getMessage());*/
			
			if (!comment.isEmpty() || comment != null) {
				this.fbManager.publish(searchPost.getId() + "/comments", String.class, Parameter.with("message", comment));
			}
		}
	}
	
	public static String generateSearchString(String analysisName, String dataSetName, List<AnalysisEventParameter> aepList, Date createdAt) {
		StringBuilder result = new StringBuilder();
		result.append("Analysis Tool: " + analysisName + "\n");
		result.append("Dataset Name: " + dataSetName + "\n");
		result.append("Parameters:\n");
		
		for (AnalysisEventParameter param: aepList) {
			result.append(param.getParameterKey() + ": " + param.getParameterValue() + "\n");
		}
		result.append("Date: " + createdAt.toString() + "\n");
		
		return result.toString();
	}
	
	public User getMe() {
		return this.me;
	}
	
	public Post searchExistingPost(String resultString) {
		this.myFeeds = this.fbManager.fetchConnection("me/feed", Post.class);
		List<Post> feedList = this.myFeeds.getData();
		
		resultString = resultString.replaceAll(" |\n|\t", "");

		String feed = "";
		for (Post myPost: feedList) {
			feed = myPost.getMessage().replaceAll(" |\n|\t", "");
			
			if (resultString.equalsIgnoreCase(feed)) {
				return myPost;
			}
		}
		return null;
	}
	
	public void getComments() {
		
	}

}
