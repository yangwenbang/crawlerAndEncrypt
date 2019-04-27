package com.example.demo.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import com.mongodb.BasicDBObject;


public class GetCSDNInfo {
	
	/**
	 * csdn的url结构为：https://blog.csdn.net/用户名/article/list/页数
	 * 
	 */
	public String userName = "qq_24598601";
	public static String url = "https://blog.csdn.net/";
	
	public GetCSDNInfo(String url, String userName) {
		super();
		if("".equals(userName) || userName == null) {
			System.out.println("用户名不能为空");
		} else if("".equals(url) || !url.equals(url)) {
			System.out.println("url不正确");
		}
		this.userName = userName;
	}


	/**
	 * 描述：获取要获取信息的链接
	 * @author 欧阳
	 * @serialData 20180728
	 * @param useName
	 * @return
	 */
	public void getUrl() {
		
		System.out.println("**********开始获取" + userName + "发布的博文信息**********");
		
		//记录页数
		int pageNum = 1;
		int count = 0; //记录爬取博文条数
		
		while(pageNum > 0) {
			String urlStr = url + userName + "/article/list/" + String.valueOf(pageNum);
			
	        try {
	        	//获取网页信息
				HttpResponse httpResponse = GetHttpResponse.getHttpClient(urlStr);
				
				//将网页内容进去转码
				String html = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				
				//通过Jsoup将页面转成Document对象进行解析
				Document documents = Jsoup.parse(html);
				
				Elements elements = documents.select(".article-list");
				
				/*
				 * 如果获取到的Elements为空，则已经没有博文了
				 */
				if(elements.isEmpty()) {
					System.out.println("已经没有博文了！");
					break;
				} else {
					pageNum++; //设置获取下一页
				}
				
				//获取当前页的博文条数
				Elements contents = documents.select("h4  a");
				
				List<org.bson.Document> listD = new ArrayList<org.bson.Document>();
				for(Element e : contents) {
					org.bson.Document documet = new org.bson.Document();
					if(!StringUtils.isEmpty(e.text()) && e.text().split(" ").length > 1) {
						documet.put("is", e.text().split(" ")[0]);
						documet.put("title", e.text().split(" ")[1]);
						documet.put("url", e.attr("href"));
						documet.put("status", "0");
						
						listD.add(documet);
						count++;  //计数
					}
				}
				
				Boolean flag = MongoDBJDBC.getMongoDBJDBC().inSert("crawler", "blog", listD);
				System.out.print("url=" + urlStr + "--");
				if(flag) {
					System.out.println(listD.size() + "条存入数据库");
				} else {
					System.out.println("存入数据库失败！");
				}
				
				//清空list中的数据，准备下一下存放
				listD.clear();
				
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("发生未知错误!");
			}
		}
		
		System.out.println("成功找到并存入数据库：" + count + "条博文");
		System.out.println("***********结束获取" + userName + "发布的博文信息*********");
	}
	
	/**
	 * 从数据库中获取每一条博文的链接并通过链接获取详情信息
	 * @author 欧阳
	 * @serialData 20180728
	 */
	public void getBlogInfo() {
		System.out.println("*******************开始获取博文信息******************");
		//添加条件，status为0表示未被访问过
		String[] key = {"status"};
		Object[] value = {"0"}; 
		
		//从数据库中获取未被访问的节点
		List<org.bson.Document> documents = MongoDBJDBC.getMongoDBJDBC()
					.find("crawler", "blog", key, value, 10);
		
		//直到数据库中的节点都被访问才结束
		while(documents.size() > 0) {
			for(org.bson.Document document : documents) {
				String url = document.getString("url");  //获取链接
				ObjectId _id = document.getObjectId("_id");  //获取id
				
		        try {
		        	//获取网页信息
					HttpResponse httpResponse = GetHttpResponse.getHttpClient(url);
					
					//将网页内容进去转码
					String html = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
					
					//通过Jsoup将页面转成Document对象进行解析
					Document docs = Jsoup.parse(html);
					
					Elements creatTime = docs.select(".time"); //发布时间
					Elements readNum = docs.select(".read-count");  //阅读数
					
					List<org.bson.Document> listD = new ArrayList<org.bson.Document>();
					org.bson.Document documet = new org.bson.Document();
					documet.put("pkid", _id);
					documet.put("creatTime", creatTime.get(0).text());
					documet.put("readNum", readNum.get(0).text().split("：")[1]);
					listD.add(documet);
					
					Boolean flag = MongoDBJDBC.getMongoDBJDBC().inSert("crawler", "blogInfo", listD);
					if(flag) {
						/*
						 * 修改已经访问过的数据的状态
						 */
						
						//添加修改时查询的条件
						BasicDBObject whereDoc = new BasicDBObject();
						whereDoc.put("_id", _id);
						
						//添加修改后的值
						BasicDBObject updateDoc = new BasicDBObject();
						updateDoc.put("status", "1");
						
						//修改
						MongoDBJDBC.getMongoDBJDBC().updateOne("crawler", "blog", whereDoc, updateDoc);
					} else {
						System.out.println("存入数据库失败！");
					}
					
					//清空list中的数据，准备下一下存放
					listD.clear();
					
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("发生未知错误!");
				}
			}
			
			//再次从数据库中获取未被访问的节点
			documents = MongoDBJDBC.getMongoDBJDBC()
					.find("crawler", "blog", key, value, 10);
		}
		
		System.out.println("所有节点都已经被访问！");
		System.out.println("*******************结束获取博文信息******************");
	}
}

