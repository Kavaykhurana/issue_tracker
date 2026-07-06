#!/bin/bash

# Color utility definitions
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
BLUE='\033[0;34m'

BASE_URL="http://localhost:8080"
echo -e "${BLUE}=== E2E Integration Verification Suite for Issue Tracker API ===${NC}"

# 1. Register User
echo -e "\n${BLUE}1. Registering user 'verifyuser'...${NC}"
REG_RES=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "verifyuser",
    "email": "verifyuser@example.com",
    "password": "verifypassword",
    "displayName": "Verify Test User"
  }')
echo "Response: $REG_RES"

# 2. Login User and Extract JWT
echo -e "\n${BLUE}2. Logging in 'verifyuser'...${NC}"
LOGIN_RES=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "verifyuser",
    "password": "verifypassword"
  }')
echo "Response: $LOGIN_RES"

TOKEN=$(echo "$LOGIN_RES" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
  echo -e "${RED}Failed to extract JWT Token. Make sure the Spring Boot application is running on port 8080.${NC}"
  exit 1
fi
echo -e "${GREEN}Extracted JWT Token: ${TOKEN:0:20}...[truncated]${NC}"

# 3. Fetch Current Profile
echo -e "\n${BLUE}3. Fetching /auth/me...${NC}"
curl -s -X GET "$BASE_URL/api/v1/auth/me" \
  -H "Authorization: Bearer $TOKEN"

# 4. Create Project
echo -e "\n${BLUE}4. Creating Project 'Alpha'...${NC}"
PROJECT_RES=$(curl -s -X POST "$BASE_URL/api/v1/projects" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Project Alpha",
    "key": "ALPHA",
    "description": "Verification test project"
  }')
echo "Response: $PROJECT_RES"

PROJECT_ID=$(echo "$PROJECT_RES" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
if [ -z "$PROJECT_ID" ]; then
  echo -e "${RED}Failed to retrieve created Project ID.${NC}"
  exit 1
fi
echo -e "${GREEN}Created Project ID: $PROJECT_ID${NC}"

# 5. Add Member
echo -e "\n${BLUE}5. Adding member 'dev1' (ID: 2) as MEMBER...${NC}"
curl -s -X POST "$BASE_URL/api/v1/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "role": "MEMBER"
  }'

# 6. Create Issue
echo -e "\n${BLUE}6. Creating Issue inside project...${NC}"
ISSUE_RES=$(curl -s -X POST "$BASE_URL/api/v1/projects/$PROJECT_ID/issues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Auth validation error",
    "description": "Double check standard options handling",
    "status": "TODO",
    "priority": "HIGH",
    "type": "BUG",
    "assigneeId": 2
  }')
echo "Response: $ISSUE_RES"

ISSUE_ID=$(echo "$ISSUE_RES" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
if [ -z "$ISSUE_ID" ]; then
  echo -e "${RED}Failed to retrieve created Issue ID.${NC}"
  exit 1
fi
echo -e "${GREEN}Created Issue ID: $ISSUE_ID${NC}"

# 7. Transition Status
echo -e "\n${BLUE}7. Transitioning Issue Status: TODO -> IN_PROGRESS...${NC}"
curl -s -X PUT "$BASE_URL/api/v1/issues/$ISSUE_ID/status" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "IN_PROGRESS"
  }'

# 8. Create Label
echo -e "\n${BLUE}8. Creating Project Label 'high-pri'...${NC}"
LABEL_RES=$(curl -s -X POST "$BASE_URL/api/v1/projects/$PROJECT_ID/labels" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "high-pri",
    "color": "#FF5733"
  }')
echo "Response: $LABEL_RES"

LABEL_ID=$(echo "$LABEL_RES" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# 9. Add Label to Issue
if [ ! -z "$LABEL_ID" ]; then
  echo -e "\n${BLUE}9. Adding label to issue...${NC}"
  curl -s -X POST "$BASE_URL/api/v1/issues/$ISSUE_ID/labels/$LABEL_ID" \
    -H "Authorization: Bearer $TOKEN"
fi

# 10. Add Comment
echo -e "\n${BLUE}10. Adding Comment...${NC}"
curl -s -X POST "$BASE_URL/api/v1/issues/$ISSUE_ID/comments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Reproduced and starting on local fix."
  }'

# 11. Fetch Project Stats
echo -e "\n${BLUE}11. Fetching Project Stats...${NC}"
curl -s -X GET "$BASE_URL/api/v1/projects/$PROJECT_ID/stats" \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n${GREEN}=== E2E Verification Script Completed ===${NC}"
