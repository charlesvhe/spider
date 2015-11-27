# Spider 接口说明 #


## 分布式下载系统对接接口 ##
爬虫系统向分布式下载系统发送下载请求的接口
-   URL：/分布式下载系统接口地址(待填)

- 提交数据格式说明
>提交方式: POST; KEY: "data"; value:

	{
		"taskId" : 123, // 任务编号
		"request" : [ // 请求组
			{
				"url" : "http://www.guahao.com/hospital/areahospitals?q=&pi=1&p=%E5%8C%97%E4%BA%AC", // 请求url, 必填
				"method" : "POST", // 请求方式, 不区分大小写, 选填, 默认GET
				"head" : [{key:value},...] // 请求头信息
			}, ...
		]
	}

- 返回数据格式说明
>返回数据格式
	
	{
		"success" : true,	// 请求成功状态, 必填
		"errorCode" : 0,	// 错误编码, 选填
		"message" : "",		// 消息, 选填
		"data" : [{}]		// 返回信息, 选填
	}

## 接收分布式下载结果接口 ##
分布式下载系统向爬虫系统返回下载信息的接口
-   URL：/task/{id}/response

- 提交数据格式说明
>提交方式: POST; KEY: "data"; value:

	{
		"response" : [ // 响应组
			{
				"url" : "http://www.guahao.com/hospital/areahospitals?q=&pi=1&p=%E5%8C%97%E4%BA%AC", // 请求url, 必填
				"head" : [{key:value},...] // 响应头信息
				"body" : "" // 响应内容主体
			}, ...
		]
	}

- 返回数据格式说明
>返回数据格式
	
	{
		"success" : true,	// 请求成功状态, 必填
		"errorCode" : 0,	// 错误编码, 选填
		"message" : "",		// 消息, 选填
		"data" : [{}]		// 返回信息, 选填
	}