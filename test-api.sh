#!/bin/bash
# LinkX IM API 测试脚本
# 使用前请先启动: MySQL, Redis, Spring Boot服务

BASE_URL="http://localhost:8080"

echo "=== 1. 测试注册 ==="
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","nickname":"测试用户","password":"123456"}' | python -m json.tool

echo ""
echo "=== 2. 测试登录 ==="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}')
echo $LOGIN_RESP | python -m json.tool

# 提取token
TOKEN=$(echo $LOGIN_RESP | python -c "import sys, json; print(json.load(sys.stdin)['data']['accessToken'])")
USER_ID=$(echo $LOGIN_RESP | python -c "import sys, json; print(json.load(sys.stdin)['data']['userId'])")

echo ""
echo "=== 3. 测试获取我的资料 ==="
curl -s -X GET "$BASE_URL/api/user/me" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool

echo ""
echo "=== 4. 测试修改资料 ==="
curl -s -X PUT "$BASE_URL/api/user/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nickname":"新昵称","signature":"这是我的签名","gender":1,"region":"北京"}' | python -m json.tool

echo ""
echo "=== 5. 测试搜索用户 ==="
curl -s -X GET "$BASE_URL/api/user/search?keyword=test" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool

echo ""
echo "=== 6. 测试注册第二个用户 ==="
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser2","nickname":"测试用户2","password":"123456"}' | python -m json.tool

echo ""
echo "=== 测试完成 ==="
