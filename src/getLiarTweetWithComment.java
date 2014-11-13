import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import weibo4j.Comments;
import weibo4j.Timeline;
import weibo4j.model.Comment;
import weibo4j.model.CommentWapper;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.User;
import weibo4j.model.WeiboException;
import weibo4j.org.json.JSONObject;

public class getLiarTweetWithComment {
	private final String access_token = "2.00jrobWBe3dgkC4b30b8e7d358ktQB";
	private final static String host = "10.108.192.165";
	private final static String dbName = "AlexYoung";
	private final static String username = "ittcdb";
	private final static String password = "ittc706706";
	private final static String collectionName = "CreditStatusWithComment";
	private static DBCollection Collection;
	private String path = "./data/bigLiarMid.txt";

	public getLiarTweetWithComment() throws UnknownHostException {
		this.Collection = getMongoDBCollection.getMongoDBColl(host,
				dbName, username, password, collectionName);
	}

	public void getLine() throws IOException {
		InputStreamReader iReader = new InputStreamReader(new FileInputStream(
				new File(path)));
		BufferedReader bReader = new BufferedReader(iReader);
		String line;
		while ((line = bReader.readLine()) != null) {
			getIdFromMid(line);
		}
		bReader.close();
	}

	public void getIdFromMid(String mid) {
		Timeline tm = new Timeline();
		tm.client.setToken(access_token);
		try {
			JSONObject object = tm.QueryId(mid, 1, 1);
			String id = object.optString("id");
			getTweetById(id, mid);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}

	public void getTweetById(String id, String mid) throws WeiboException {
		Comments cm = new Comments();
		cm.client.setToken(access_token);
		CommentWapper temp = cm.getCommentById(id);
		long number = temp.getTotalNumber();
		long pagenumber = number / 50;
		for (int pagenum = 1; pagenum <= pagenumber + 1; pagenum++) {
			Paging page = new Paging(pagenum);
			CommentWapper comment = cm.getCommentById(id, page, 0);
			for (Comment c : comment.getComments()) {
				getComment(c, mid);
			}
		}
	}

	public void getComment(Comment comment, String mid) {
		CommentWithStatusData cData = new CommentWithStatusData();
		cData.setCommentDate(comment.getCreatedAt());
		cData.setCommentID(comment.getId());
		cData.setcommentText(comment.getText());
		User user = comment.getUser();
		cData.setReviewerID(user.getId());
		cData.setReviewerName(user.getName());
		cData.setReviewrFollowersCount(user.getbiFollowersCount());
		cData.setReviewrFriendsCount(user.getFriendsCount());
		cData.setReviewrStatusesCount(user.getStatusesCount());
		cData.setReviewrBiCount(user.getBiFollowersCount());
		Status status = comment.getStatus();
		cData.setTweerID(status.getId());
		cData.setTweetMid(mid);
		cData.setTweetDate(status.getCreatedAt());
		cData.setTweerText(status.getText());
		cData.setCommentsCount(status.getCommentsCount());
		User author = status.getUser();
		cData.setAuthorID(author.getId());
		cData.setAuthorName(author.getName());
		cData.setAuthorFollowersCount(author.getFollowersCount());
		cData.setAuthorFriendsCount(author.getFriendsCount());
		cData.setAuthorStatusesCount(author.getStatusesCount());
		cData.setAuthorBiCount(author.getBiFollowersCount());
		updateToDb(cData, comment.getId());
	}

	public void updateToDb(CommentWithStatusData cData, long commentID) {
		DBObject query = new BasicDBObject("commentID", commentID);
		DBObject object = new BasicDBObject();
		object.put("CommentDate", cData.getCommentDate());
		object.put("CommentID", cData.getCommentID());
		object.put("CommentText", cData.getcommentText());
		object.put("ReviewerID", cData.getReviewrID());
		object.put("ReviewerName", cData.getReviewrName());
		object.put("ReviewrFollowersCount", cData.getReviewrFollowersCount());
		object.put("ReviewrFriendsCount", cData.getReviewrFriendsCount());
		object.put("ReviewrStatusesCount", cData.getReviewrStatusesCount());
		object.put("ReviewrBiCount", cData.getReviewrBiCount());
		object.put("TweerID", cData.getTweerID());
		object.put("TweetMid", cData.getTweetMid());
		object.put("TweetDate", cData.getTweetDate());
		object.put("TweerText", cData.getTweerText());
		object.put("CommentsCount", cData.getCommentsCount());
		object.put("AuthorID", cData.getAuthorID());
		object.put("AuthorName", cData.getAuthorName());
		object.put("AuthorFollowersCount", cData.getAuthorFollowersCount());
		object.put("AuthorFriendsCount", cData.getAuthorFriendsCount());
		object.put("AuthorStatusesCount", cData.getAuthorStatusesCount());
		object.put("AuthorBiCount", cData.getAuthorBiCount());
		DBObject updateSetValue = new BasicDBObject("$set", object);
		Collection.update(query, updateSetValue, true, false);
	}
}
