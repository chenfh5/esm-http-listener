define({ "api": [
  {
    "type": "DELETE",
    "url": "shell/esm",
    "title": "cancel specified task",
    "group": "ESIM",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "id",
            "description": ""
          }
        ]
      },
      "examples": [
        {
          "title": "Request-Example:",
          "content": "shell/esm?id=7947866",
          "type": "String"
        }
      ]
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "remove task with id=7947866, size=3",
          "type": "String"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/scala/io/github/chenfh5/handler/ShellHandler.scala",
    "groupTitle": "ESIM",
    "name": "DeleteShellEsm"
  },
  {
    "type": "GET",
    "url": "shell/esm",
    "title": "get running tasks",
    "description": "<p>thread-name format is [type_timestamp_params.hashcode], e.g., countQueueSize_1542095780885_1468583655</p>",
    "group": "ESIM",
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "get health is success, trigger at 2018-11-13 16:11:21 Tue, runningTasks=Set(countQueueSize_1542095780885_1468583655, countQueueSize_1542095777669_-525499890)",
          "type": "String"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/scala/io/github/chenfh5/handler/ShellHandler.scala",
    "groupTitle": "ESIM",
    "name": "GetShellEsm"
  },
  {
    "type": "POST",
    "url": "shell/esm",
    "title": "create specified task",
    "group": "ESIM",
    "parameter": {
      "examples": [
        {
          "title": "Request-Example:",
          "content": "{\n\"srcHost\": \"localhost\",\n\"srcPort\": \"8080\",\n\"destHost\": \"localhost\",\n\"destPort\": \"8082\",\n\"authUser\": \"Ymx1ZWtpbmc=\",\n\"authPW\": \"Ykx1RWtpbkdAMjAxOA==\",\n\"srcIndexName\": \"src_index_name\",\n\"srcTypeName\": \"src_index_type_name\",\n\"destIndexName\": \"dest_index_name\",\n\"scrollSize\": \"10000\",\n\"concurrentRequests\": \"10\"\n}",
          "type": "json"
        }
      ]
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "blocking the request until response, but you can close the session whose task had runned in backgroud.\nIf you want to kill it, using `GET` to find the id, and then using `DELETE` to cancel it",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/scala/io/github/chenfh5/handler/ShellHandler.scala",
    "groupTitle": "ESIM",
    "name": "PostShellEsm"
  }
] });
