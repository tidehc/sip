# 通用说明

## Restful风格

> 如请求路径`/user/get/{id}`中的`{}`为需要的请求参数，此路径中的请求参数是`id`，实际发起的请求例如`/user/get/123`，Restful风格中的参数均为必填参数

## 请求头

| 名称            | 必选   | 描述                  |
| :------------ | :--- | :------------------ |
| Authorization | 否    | 需要授权才可以使用的接口需要添加此参数 |

## 分页请求参数

| 名称       | 类型     | 必选(Y/N) | 默认值  | 描述     |
| :------- | ------ | :------ | ---- | :----- |
| pageNum  | Number | N       | 1    | 页数     |
| pageSize | Number | N       | 20   | 每页显示条数 |

## 响应参数

| 名称      | 类型           | 描述                               |
| :------ | :----------- | :------------------------------- |
| code    | Int          | 状态码，参考通用状态码说明                    |
| rid     | String       | 请求成功的唯一标识符                       |
| msg     | String       | 返回的文字消息                          |
| data    | Object/Array | 返回的数据，参见每个接口具体说明，每个接口只解释data下的数据 |
| success | Boolean      | 请求是否成功                           |

## 返回带分页响应示例

```json
{
  "code": 0,
  "rid": "997cad15-90a4-43ee-ab04-f4cfe28ecd08",
  "msg": "执行成功",
  "data": {
    "list":[
      {"a":1},
      {"b":2},
    ],
    "page":{
      "pageNum":1,
      "pageSize":20,
      "total":2,
    }
  },
  "success": true
}
```

## 返回数组响应示例

```json
{
  "code": 0,
  "rid": "997cad15-90a4-43ee-ab04-f4cfe28ecd08",
  "msg": "执行成功",
  "data": [
      {"a":1},
      {"b":2},
    ],
  "success": true
}
```

## 添加、更新、删除响应示例

```json
{
  "code": 0,
  "rid": "997cad15-90a4-43ee-ab04-f4cfe28ecd08",
  "msg": "操作成功n条数据",
  "data": {},
  "success": true
}
```

## 状态码

| 状态码  | 描述   |
| :--- | :--- |
| 0    | 成功   |
| 其他   | 错误   |