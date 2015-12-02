import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.jzt.spider.entity.Task
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

JSONObject taskJson = JSONObject.parseObject(task);
JSONArray responseArray = JSONObject.parseArray(taskJson.getString("response"));
JSONObject responseJson = responseArray.getJSONObject(0);
String body = responseJson.getString("body");

Document doc = Jsoup.parse(body);
Elements elements = doc.select("ul.hos_ul>li>a.cover-bg");
JSONObject output = new JSONObject();
List<Task> subTask = new ArrayList<>();
for (Element element : elements) {
    String url = element.attr("href").replace("/hospital/", "/hospital/introduction/");
    subTask.add(new Task(
            url,
            3L, // 处理详情页面 robot
            "",
            "[{\"url\" : \"" + url + "\"}]",
            "",
            "",
            Task.STATUS_NOT_START
    ));
}
output.put("subTask", subTask);

return output.toJSONString();