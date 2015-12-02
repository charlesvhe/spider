import com.alibaba.fastjson.JSONObject;

JSONObject taskJson = JSONObject.parseObject(task);
return taskJson.getString("response");