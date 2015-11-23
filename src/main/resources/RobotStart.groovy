import com.alibaba.fastjson.*;
JSONObject json = JSON.parseObject(task);
return json.getString("response");