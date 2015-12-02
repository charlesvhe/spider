import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

JSONObject taskJson = JSONObject.parseObject(task);
JSONArray responseArray = JSONObject.parseArray(taskJson.getString("response"));
JSONObject responseJson = responseArray.getJSONObject(0);
String body = responseJson.getString("body");

Document doc = Jsoup.parse(body);
Elements divInfo = doc.select("div.info");

JSONObject output = new JSONObject();
output.put("hospital", divInfo.select("div.detail.word-break>h1>strong>a").text());
output.put("address", divInfo.select("div.address>span").text());
output.put("tel", divInfo.select("div.tel>span").text());
output.put("website", divInfo.select("div.website>span").text().replace("\u00A0",""));  // 替换&nbsp;(\u00A0, 160空格)
output.put("introduction", doc.select("div.introduction-content").text());

return output.toJSONString();