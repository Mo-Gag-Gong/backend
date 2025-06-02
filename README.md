# 모.각.공. 애플리케이션 API 명세서

## 1. 사용자 (User) - `/api/users`

### 1.1. 내 프로필 정보 조회

* **Endpoint**: `GET /api/users/profile`
* **설명**: 현재 로그인한 사용자의 프로필 정보를 조회합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.UserProfileDto`
        ```json
        {
          "profileId": "long",
          "userId": "long",
          "email": "string",
          "name": "string (nullable)",
          "gender": "string (nullable)",
          "phoneNumber": "string (nullable)",
          "birthYear": "integer (nullable)",
          "profileImage": "string (nullable)",
          "interests": [
            {
              "interestId": "long",
              "interestName": "string"
            }
          ],
          "onboardingCompleted": "boolean"
        }
        ```
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 프로필 정보를 찾을 수 없음

### 1.2. 특정 사용자 프로필 및 통계 조회

* **Endpoint**: `GET /api/users/{userId}/profile`
* **설명**: 특정 사용자의 공개 프로필 정보와 통계 정보를 조회합니다. 자신의 프로필인 경우 개인정보를 포함한 전체 정보를 반환합니다.
* **Path Variable**:
    * `userId` (long): 조회할 사용자의 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.UserProfileWithStatsDto`
        ```json
        {
          "userId": "long",
          "name": "string (nullable)",
          "email": "string (nullable, 자신의 프로필인 경우에만 포함)",
          "gender": "string (nullable, 자신의 프로필인 경우에만 포함)",
          "phoneNumber": "string (nullable, 자신의 프로필인 경우에만 포함)",
          "birthYear": "integer (nullable, 자신의 프로필인 경우에만 포함)",
          "profileImage": "string (nullable)",
          "interests": [
            {
              "interestId": "long",
              "interestName": "string"
            }
          ],
          "groupParticipationCount": "integer (nullable)",
          "attendanceRate": "double (nullable)",
          "totalMeetings": "integer (nullable)",
          "statsLastUpdated": "datetime (nullable)",
          "isOwnProfile": "boolean"
        }
        ```
* **Response Body (실패 시 - 404 Not Found)**: 사용자를 찾을 수 없음

### 1.3. 사용자 온보딩 상태 확인

* **Endpoint**: `GET /api/users/onboarding-status`
* **설명**: 현재 로그인한 사용자의 온보딩(프로필 추가 정보 입력) 완료 여부를 확인합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    ```json
    {
      "onboardingCompleted": "boolean",
      "role": "string" // 예: "ROLE_USER", "ROLE_GUEST"
    }
    ```
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

### 1.4. 사용자 프로필 수정 (온보딩 포함)

* **Endpoint**: `PUT /api/users/profile`
* **설명**: 현재 로그인한 사용자의 프로필 정보를 수정합니다. 온보딩 과정에서 사용될 수 있습니다. 모든 필수 정보 (이름, 성별, 출생년도, 관심사) 입력 시 `onboardingCompleted`가 `true`로 변경되고, 사용자의 `role`이 `ROLE_GUEST`에서 `ROLE_USER`로 변경될 수 있습니다.
* **Request Body**:
    * `com.mogacko.mogacko.dto.ProfileUpdateRequest`
        ```json
        {
          "name": "string",
          "gender": "string",
          "phoneNumber": "string (nullable)",
          "locationName": "string (nullable)",
          "birthYear": "integer",
          "interestIds": ["long"] // 관심사 ID 목록
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.UserProfileDto` (수정된 프로필 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 프로필 정보를 찾을 수 없음

### 1.5. 전체 관심사 목록 조회

* **Endpoint**: `GET /api/users/interests`
* **설명**: 시스템에 등록된 모든 활성 상태의 관심사 목록을 조회합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.InterestDto>`
        ```json
        [
          {
            "interestId": "long",
            "interestName": "string"
          }
        ]
        ```

### 1.6. 내 관심사 목록 조회

* **Endpoint**: `GET /api/users/my-interests`
* **설명**: 현재 로그인한 사용자가 등록한 관심사 목록을 조회합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.InterestDto>`
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

### 1.7. 관심사 추가

* **Endpoint**: `POST /api/users/interests/{interestId}`
* **설명**: 현재 로그인한 사용자의 프로필에 새 관심사를 추가합니다.
* **Path Variable**:
    * `interestId` (long): 추가할 관심사의 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 해당 관심사를 찾을 수 없음

### 1.8. 관심사 제거

* **Endpoint**: `DELETE /api/users/interests/{interestId}`
* **설명**: 현재 로그인한 사용자의 프로필에서 관심사를 제거합니다.
* **Path Variable**:
    * `interestId` (long): 제거할 관심사의 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 해당 관심사를 찾을 수 없음

## 2. 사용자 통계 (User Statistics) - `/api/users/statistics`

### 2.1. 내 통계 정보 조회

* **Endpoint**: `GET /api/users/statistics`
* **설명**: 현재 로그인한 사용자의 활동 통계 정보를 조회합니다. 통계 정보가 없으면 자동으로 생성합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.UserStatisticsDto`
        ```json
        {
          "statId": "long",
          "userId": "long",
          "groupParticipationCount": "integer", // 참여 중인 스터디 그룹 수
          "attendanceRate": "double", // 모임 출석률 (%)
          "totalMeetings": "integer", // 총 참여한 모임 수
          "lastUpdated": "datetime"
        }
        ```
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

### 2.2. 내 통계 정보 갱신

* **Endpoint**: `POST /api/users/statistics/refresh`
* **설명**: 현재 로그인한 사용자의 활동 통계 정보를 최신 데이터로 갱신합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.UserStatisticsDto` (갱신된 통계 정보)
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

## 3. 스터디 그룹 (Study Group) - `/api/groups`

### 3.1. 전체 스터디 그룹 목록 조회 (페이징)

* **Endpoint**: `GET /api/groups`
* **설명**: 모든 활성 상태의 스터디 그룹 목록을 페이지 단위로 조회합니다.
* **Query Parameters**:
    * `page` (int, optional, default: 0): 페이지 번호 (0부터 시작)
    * `size` (int, optional, default: 10): 페이지 크기
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `Page<com.mogacko.mogacko.dto.StudyGroupDto>`
        ```json
        // Page 객체 구조는 Spring Data Page 인터페이스 참조
        {
          "content": [
            {
              "groupId": "long",
              "creatorId": "long",
              "creatorName": "string",
              "title": "string",
              "interestName": "string",
              "description": "string",
              "locationName": "string (nullable)",
              "startDate": "date (YYYY-MM-DD)",
              "endDate": "date (YYYY-MM-DD)",
              "maxMembers": "integer",
              "currentMembers": "integer",
              "requirements": "string (nullable)",
              "isActive": "boolean",
              "createdAt": "datetime",
              "updatedAt": "datetime"
            }
          ],
          "pageable": { /* ... */ },
          "totalPages": "integer",
          "totalElements": "long",
          // ... 기타 Page 정보
        }
        ```

### 3.2. 카테고리별 스터디 그룹 목록 조회 (페이징)

* **Endpoint**: `GET /api/groups/interest/{interest}`
* **설명**: 특정 카테고리(관심사)의 활성 스터디 그룹 목록을 페이지 단위로 조회합니다.
* **Path Variable**:
    * `interest` (String): 스터디 그룹 카테고리(관심사 이름)
* **Query Parameters**:
    * `page` (int, optional, default: 0): 페이지 번호
    * `size` (int, optional, default: 10): 페이지 크기
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `Page<com.mogacko.mogacko.dto.StudyGroupDto>`

### 3.3. 스터디 그룹 검색 (페이징)

* **Endpoint**: `GET /api/groups/search`
* **설명**: 제목 또는 설명에 키워드가 포함된 스터디 그룹을 검색합니다.
* **Query Parameters**:
    * `keyword` (String): 검색 키워드
    * `page` (int, optional, default: 0): 페이지 번호
    * `size` (int, optional, default: 10): 페이지 크기
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `Page<com.mogacko.mogacko.dto.StudyGroupDto>`

### 3.4. 내가 그룹장인 스터디 그룹 목록 조회

* **Endpoint**: `GET /api/groups/my-owned-groups`
* **설명**: 현재 로그인한 사용자가 그룹장(생성자)인 모든 스터디 그룹 목록을 조회합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.StudyGroupDto>`
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

### 3.5. 내가 참여한 스터디 그룹 목록 조회

* **Endpoint**: `GET /api/groups/my-joined-groups`
* **설명**: 현재 로그인한 사용자가 참여자로 가입한 모든 스터디 그룹 목록을 조회합니다.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.StudyGroupDto>`
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

### 3.6. 스터디 그룹 상세 정보 조회

* **Endpoint**: `GET /api/groups/{groupId}`
* **설명**: 특정 스터디 그룹의 세부 정보를 조회합니다.
* **Path Variable**:
    * `groupId` (long): 조회할 스터디 그룹 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.StudyGroupDto`
* **Response Body (실패 시 - 403 Not Found)**: 스터디 그룹을 찾을 수 없음

### 3.7. 스터디 그룹 생성

* **Endpoint**: `POST /api/groups`
* **설명**: 새로운 스터디 그룹을 생성하고, 생성자는 자동으로 멤버(ACTIVE 상태)로 추가됩니다.
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupCreateRequest`
        ```json
        {
          "title": "string",
          "interest": "string", // 관심사 이름
          "description": "string",
          "locationName": "string (nullable)",
          "startDate": "date (YYYY-MM-DD)",
          "endDate": "date (YYYY-MM-DD)",
          "maxMembers": "integer",
          "requirements": "string (nullable)"
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.StudyGroupDto` (생성된 그룹 정보)
* **Response Body (실패 시 - 401 Unauthorized)**: 인증되지 않은 사용자

### 3.8. 스터디 그룹 정보 수정

* **Endpoint**: `PUT /api/groups/{groupId}`
* **설명**: 기존 스터디 그룹의 정보를 수정합니다. 그룹 생성자만 수정 가능합니다.
* **Path Variable**:
    * `groupId` (long): 수정할 스터디 그룹 ID
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupCreateRequest`
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.StudyGroupDto` (수정된 그룹 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 그룹 수정 권한 없음 (생성자가 아님)
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 3.9. 스터디 그룹 비활성화 (논리적 삭제)

* **Endpoint**: `DELETE /api/groups/{groupId}`
* **설명**: 스터디 그룹을 비활성화(논리적 삭제) 처리합니다. 그룹 생성자만 비활성화 가능합니다.
* **Path Variable**:
    * `groupId` (long): 비활성화할 스터디 그룹 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 그룹 비활성화 권한 없음 (생성자가 아님)
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 3.10. 스터디 그룹 멤버 목록 조회

* **Endpoint**: `GET /api/groups/{groupId}/members`
* **설명**: 특정 스터디 그룹의 모든 멤버 목록(상태 포함)을 조회합니다.
* **Path Variable**:
    * `groupId` (long): 스터디 그룹 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.GroupMemberDto>`
        ```json
        [
          {
            "membershipId": "long",
            "userId": "long",
            "userName": "string",
            "profileImage": "string (nullable)",
            "joinDate": "date (YYYY-MM-DD)",
            "status": "string" // 예: "ACTIVE", "PENDING", "INACTIVE", "KICKED"
          }
        ]
        ```
* **Response Body (실패 시 - 404 Not Found)**: 스터디 그룹을 찾을 수 없음

### 3.11. 스터디 그룹 가입 신청

* **Endpoint**: `POST /api/groups/{groupId}/apply`
* **설명**: 스터디 그룹에 가입 신청을 합니다. 멤버 상태는 `PENDING`으로 설정됩니다. 이미 가입 신청했거나, 활성 멤버이거나, 추방된 경우 신청 불가. 최대 인원 초과 시 신청 불가.
* **Path Variable**:
    * `groupId` (long): 가입 신청할 스터디 그룹 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `400 Bad Request`: 가입 신청 실패 (이미 가입됨, 최대 인원 초과, 추방된 멤버 등)
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 3.12. 가입 대기 멤버 목록 조회

* **Endpoint**: `GET /api/groups/{groupId}/pending-members`
* **설명**: 스터디 그룹의 가입 승인 대기 중인 멤버(`PENDING` 상태) 목록을 조회합니다. 그룹 생성자만 조회 가능합니다.
* **Path Variable**:
    * `groupId` (long): 스터디 그룹 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.GroupMemberDto>`
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 조회 권한 없음 (그룹 생성자가 아님)
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 3.13. 가입 신청 승인

* **Endpoint**: `POST /api/groups/{groupId}/members/{userId}/approve`
* **설명**: 스터디 그룹 가입 신청(`PENDING` 상태)을 승인하여 멤버 상태를 `ACTIVE`로 변경합니다. 그룹 생성자만 사용 가능합니다.
* **Path Variables**:
    * `groupId` (long): 스터디 그룹 ID
    * `userId` (long): 승인할 사용자의 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 메시지 문자열 (예: "Member approved successfully")
* **Response Body (실패 시)**:
    * `400 Bad Request`: 승인 실패 (최대 인원 초과 등)
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 권한 없음 (그룹 생성자가 아님)
    * `404 Not Found`: 그룹, 사용자 또는 가입 신청을 찾을 수 없음 (`MEMBER_NOT_PENDING` 포함)

### 3.14. 가입 신청 거절

* **Endpoint**: `POST /api/groups/{groupId}/members/{userId}/reject`
* **설명**: 스터디 그룹 가입 신청(`PENDING` 상태)을 거절합니다. 해당 `GroupMember` 레코드가 삭제될 수 있습니다. 그룹 생성자만 사용 가능합니다.
* **Path Variables**:
    * `groupId` (long): 스터디 그룹 ID
    * `userId` (long): 거절할 사용자의 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 메시지 문자열 (예: "Member rejected successfully")
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 권한 없음 (그룹 생성자가 아님)
    * `404 Not Found`: 그룹, 사용자 또는 가입 신청을 찾을 수 없음 (`MEMBER_NOT_PENDING` 포함)

### 3.15. 스터디 그룹 멤버 추방

* **Endpoint**: `DELETE /api/groups/{groupId}/members/{userId}`
* **설명**: 그룹 생성자가 특정 멤버를 스터디 그룹에서 추방합니다. 멤버 상태를 `KICKED`로 변경합니다. 그룹 생성자는 추방할 수 없습니다.
* **Path Variables**:
    * `groupId` (long): 스터디 그룹 ID
    * `userId` (long): 추방할 사용자의 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 메시지 문자열 (예: "Member kicked successfully")
* **Response Body (실패 시)**:
    * `400 Bad Request`: 멤버 추방 실패 (생성자는 추방 불가)
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 멤버 추방 권한 없음 (그룹 생성자가 아님)
    * `404 Not Found`: 그룹 또는 멤버를 찾을 수 없음

### 3.16. 스터디 그룹 탈퇴

* **Endpoint**: `POST /api/groups/{groupId}/leave`
* **설명**: 현재 로그인한 사용자를 스터디 그룹에서 탈퇴시킵니다. 멤버 상태를 `INACTIVE`로 변경합니다. 그룹 생성자는 탈퇴할 수 없습니다.
* **Path Variable**:
    * `groupId` (long): 탈퇴할 스터디 그룹 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `400 Bad Request`: 그룹 탈퇴 실패 (그룹 생성자는 탈퇴 불가, 이미 멤버가 아님 등)
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

## 4. 스터디 그룹 채팅 (Group Chat) - `/api/groups/{groupId}/chats`

* **공통 Path Variable**: `groupId` (long) - 스터디 그룹 ID
* **요구사항**: 요청하는 사용자는 해당 스터디 그룹의 `ACTIVE` 멤버여야 합니다.

### 4.1. 그룹 채팅 메시지 목록 조회 (페이징)

* **Endpoint**: `GET /api/groups/{groupId}/chats`
* **설명**: 스터디 그룹의 채팅 메시지 목록을 페이지 단위로 조회합니다. 최신 메시지 순으로 정렬됩니다.
* **Query Parameters**:
    * `page` (int, optional, default: 0): 페이지 번호
    * `size` (int, optional, default: 20): 페이지 크기
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `Page<com.mogacko.mogacko.dto.GroupChatDto>`
        ```json
        // Page 객체 구조
        {
          "content": [
            {
              "chatId": "long",
              "groupId": "long",
              "userId": "long",
              "userName": "string",
              "profileImage": "string (nullable)",
              "message": "string",
              "sentAt": "datetime"
            }
          ],
          // ... 기타 Page 정보
        }
        ```
* **Response Body (실패 시 - 404 Not Found)**: 스터디 그룹을 찾을 수 없음

### 4.2. 채팅 메시지 전송

* **Endpoint**: `POST /api/groups/{groupId}/chats`
* **설명**: 스터디 그룹에 새 채팅 메시지를 전송합니다.
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupChatCreateRequest`
        ```json
        {
          "message": "string"
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupChatDto` (전송된 메시지 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 메시지 전송 권한 없음 (그룹 멤버가 아님)
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

## 5. 스터디 그룹 목표 (Group Goal) - `/api/groups/{groupId}/goals`

* **공통 Path Variable**: `groupId` (long) - 스터디 그룹 ID
* **요구사항**: 목표 생성/수정/삭제/토글은 해당 스터디 그룹의 `ACTIVE` 멤버 또는 그룹 생성자만 가능합니다.

### 5.1. 그룹 목표 목록 조회

* **Endpoint**: `GET /api/groups/{groupId}/goals`
* **설명**: 스터디 그룹의 모든 학습 목표 목록을 조회합니다. 종료일 기준 내림차순 정렬.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.GroupGoalDto>`
        ```json
        [
          {
            "goalId": "long",
            "groupId": "long",
            "creatorId": "long",
            "creatorName": "string",
            "title": "string",
            "pointValue": "integer (nullable)",
            "endDate": "date (YYYY-MM-DD)",
            "details": [
              {
                "detailId": "long",
                "goalId": "long",
                "description": "string",
                "isCompleted": "boolean"
              }
            ],
            "completedCount": "integer", // 완료된 세부 목표 수
            "totalCount": "integer"      // 전체 세부 목표 수
          }
        ]
        ```
* **Response Body (실패 시 - 404 Not Found)**: 스터디 그룹을 찾을 수 없음 (빈 리스트 반환될 수도 있음)

### 5.2. 특정 목표 세부 정보 조회

* **Endpoint**: `GET /api/groups/{groupId}/goals/{goalId}`
* **설명**: 특정 학습 목표의 세부 정보와 세부 목표 항목을 조회합니다.
* **Path Variable**:
    * `goalId` (long): 목표 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupGoalDto`
* **Response Body (실패 시 - 404 Not Found)**: 해당 그룹의 목표를 찾을 수 없음

### 5.3. 학습 목표 생성

* **Endpoint**: `POST /api/groups/{groupId}/goals`
* **설명**: 새로운 학습 목표와 세부 목표 항목을 생성합니다.
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupGoalCreateRequest`
        ```json
        {
          "title": "string",
          "pointValue": "integer (nullable)",
          "endDate": "date (YYYY-MM-DD)",
          "details": ["string"] // 세부 목표 설명 목록
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupGoalDto` (생성된 목표 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 목표 생성 권한 없음 (그룹 멤버가 아니거나 생성자가 아님)
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 5.4. 학습 목표 수정

* **Endpoint**: `PUT /api/groups/{groupId}/goals/{goalId}`
* **설명**: 기존 학습 목표와 세부 목표 항목을 수정합니다. 목표 작성자 또는 그룹 생성자만 수정 가능. 기존 세부 목표는 모두 삭제 후 새로 추가됩니다.
* **Path Variable**:
    * `goalId` (long): 수정할 목표 ID
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupGoalCreateRequest`
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupGoalDto` (수정된 목표 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 목표 수정 권한 없음
    * `404 Not Found`: 해당 그룹의 목표를 찾을 수 없음

### 5.5. 학습 목표 삭제

* **Endpoint**: `DELETE /api/groups/{groupId}/goals/{goalId}`
* **설명**: 학습 목표와 관련된 세부 목표 항목을 모두 삭제합니다. 목표 작성자 또는 그룹 생성자만 삭제 가능.
* **Path Variable**:
    * `goalId` (long): 삭제할 목표 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 목표 삭제 권한 없음
    * `404 Not Found`: 해당 그룹의 목표를 찾을 수 없음

### 5.6. 세부 목표 완료 상태 토글

* **Endpoint**: `POST /api/groups/{groupId}/goals/{goalId}/details/{detailId}/toggle`
* **설명**: 세부 목표 항목의 완료 상태를 변경합니다 (완료 <-> 미완료). 그룹 멤버 누구나 가능.
* **Path Variables**:
    * `goalId` (long): 목표 ID
    * `detailId` (long): 세부 목표 항목 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 상태 토글 권한 없음 (그룹 멤버가 아님)
    * `404 Not Found`: 해당 그룹의 목표 또는 세부 목표를 찾을 수 없음

## 6. 스터디 그룹 공지 (Group Notice) - `/api/groups/{groupId}/notices`

* **공통 Path Variable**: `groupId` (long) - 스터디 그룹 ID
* **요구사항**: 공지 작성/수정/삭제는 해당 스터디 그룹의 `ACTIVE` 멤버 또는 그룹 생성자만 가능합니다.

### 6.1. 그룹 공지사항 목록 조회 (페이징)

* **Endpoint**: `GET /api/groups/{groupId}/notices`
* **설명**: 스터디 그룹의 공지사항 목록을 페이지 단위로 조회합니다. 최신순 정렬.
* **Query Parameters**:
    * `page` (int, optional, default: 0): 페이지 번호
    * `size` (int, optional, default: 10): 페이지 크기
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `Page<com.mogacko.mogacko.dto.GroupNoticeDto>`
        ```json
        // Page 객체 구조
        {
          "content": [
            {
              "noticeId": "long",
              "groupId": "long",
              "creatorId": "long",
              "creatorName": "string",
              "title": "string",
              "content": "string",
              "createdAt": "datetime",
              "updatedAt": "datetime"
            }
          ],
          // ... 기타 Page 정보
        }
        ```
* **Response Body (실패 시 - 404 Not Found)**: 스터디 그룹을 찾을 수 없음

### 6.2. 특정 공지사항 세부 내용 조회

* **Endpoint**: `GET /api/groups/{groupId}/notices/{noticeId}`
* **설명**: 특정 공지사항의 세부 내용을 조회합니다.
* **Path Variable**:
    * `noticeId` (long): 공지사항 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupNoticeDto`
* **Response Body (실패 시 - 404 Not Found)**: 해당 그룹의 공지사항을 찾을 수 없음

### 6.3. 공지사항 작성

* **Endpoint**: `POST /api/groups/{groupId}/notices`
* **설명**: 스터디 그룹에 새로운 공지사항을 작성합니다.
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupNoticeCreateRequest`
        ```json
        {
          "title": "string",
          "content": "string"
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupNoticeDto` (생성된 공지사항 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 공지사항 작성 권한 없음
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 6.4. 공지사항 수정

* **Endpoint**: `PUT /api/groups/{groupId}/notices/{noticeId}`
* **설명**: 기존 공지사항의 내용을 수정합니다. 공지 작성자 또는 그룹 생성자만 수정 가능.
* **Path Variable**:
    * `noticeId` (long): 수정할 공지사항 ID
* **Request Body**:
    * `com.mogacko.mogacko.dto.GroupNoticeCreateRequest`
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.GroupNoticeDto` (수정된 공지사항 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 공지사항 수정 권한 없음
    * `404 Not Found`: 해당 그룹의 공지사항을 찾을 수 없음

### 6.5. 공지사항 삭제

* **Endpoint**: `DELETE /api/groups/{groupId}/notices/{noticeId}`
* **설명**: 스터디 그룹의 공지사항을 삭제합니다. 공지 작성자 또는 그룹 생성자만 삭제 가능.
* **Path Variable**:
    * `noticeId` (long): 삭제할 공지사항 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 공지사항 삭제 권한 없음
    * `404 Not Found`: 해당 그룹의 공지사항을 찾을 수 없음

## 7. 모임 (Meeting) - `/api/groups/{groupId}/meetings`

* **공통 Path Variable**: `groupId` (long) - 스터디 그룹 ID
* **요구사항**: 모임 생성은 해당 스터디 그룹의 `ACTIVE` 멤버만 가능. 참가/출석체크는 해당 스터디 그룹의 `ACTIVE` 멤버이면서 모임 참가자여야 함.

### 7.1. 모임 목록 조회

* **Endpoint**: `GET /api/groups/{groupId}/meetings`
* **설명**: 스터디 그룹의 모임 목록을 조회합니다. 모임 날짜 기준 내림차순 정렬.
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `List<com.mogacko.mogacko.dto.MeetingDto>`
        ```json
        [
          {
            "meetingId": "long",
            "groupId": "long",
            "title": "string",
            "location": "string",
            "latitude": "bigdecimal (nullable)",
            "longitude": "bigdecimal (nullable)",
            "meetingDate": "datetime",
            "maxParticipants": "integer (nullable)",
            "description": "string (nullable)",
            "createdBy": "long", // 생성자 User ID
            "creatorName": "string", // 생성자 이름
            "participantCount": "integer", // 현재 참가자 수
            "createdAt": "datetime"
          }
        ]
        ```
* **Response Body (실패 시 - 404 Not Found)**: 스터디 그룹을 찾을 수 없음 (빈 리스트 반환될 수도 있음)

### 7.2. 모임 상세 조회

* **Endpoint**: `GET /api/groups/{groupId}/meetings/{meetingId}`
* **설명**: 특정 모임의 상세 정보를 조회합니다.
* **Path Variable**:
    * `meetingId` (long): 모임 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.MeetingDto`
* **Response Body (실패 시 - 404 Not Found)**: 해당 그룹의 모임을 찾을 수 없음

### 7.3. 모임 생성

* **Endpoint**: `POST /api/groups/{groupId}/meetings`
* **설명**: 새로운 모임을 생성합니다. 생성자는 자동으로 모임에 참가자로 등록됩니다 (`REGISTERED` 상태).
* **Request Body**:
    * `com.mogacko.mogacko.dto.MeetingCreateRequest`
        ```json
        {
          "title": "string",
          "location": "string",
          "latitude": "bigdecimal (nullable)",  // 예: 37.5665
          "longitude": "bigdecimal (nullable)", // 예: 126.9770
          "meetingDate": "datetime",          // 예: "2025-07-15T17:00:00"
          "maxParticipants": "integer (nullable)",
          "description": "string (nullable)"
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.MeetingDto` (생성된 모임 정보)
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `403 Forbidden`: 모임 생성 권한 없음 (그룹 멤버가 아님)
    * `404 Not Found`: 스터디 그룹을 찾을 수 없음

### 7.4. 모임 참가 신청

* **Endpoint**: `POST /api/groups/{groupId}/meetings/{meetingId}/join`
* **설명**: 모임에 참가 신청을 합니다. 참가자 상태는 `REGISTERED`로 설정됩니다. 그룹 멤버여야 하고, 이미 참가했거나 최대 인원 초과 시 실패.
* **Path Variable**:
    * `meetingId` (long): 참가할 모임 ID
* **Request Body**: 없음
* **Response Body (성공 시 - 200 OK)**: 없음
* **Response Body (실패 시)**:
    * `400 Bad Request`: 참가 실패 (이미 참가, 최대 인원 초과, 그룹 멤버 아님 등)
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 해당 그룹의 모임을 찾을 수 없음

### 7.5. 출석 체크

* **Endpoint**: `POST /api/groups/{groupId}/meetings/{meetingId}/checkin`
* **설명**: 모임에 출석 체크를 합니다. 참가자 상태를 `ATTENDED`로 변경. 모임 참가자여야 하며, 모임 시간(±2시간) 및 지정된 위치(100m 이내)에서만 가능.
* **Path Variable**:
    * `meetingId` (long): 출석 체크할 모임 ID
* **Request Body**:
    * `com.mogacko.mogacko.dto.CheckInRequest`
        ```json
        {
          "latitude": "bigdecimal",  // 현재 위도
          "longitude": "bigdecimal" // 현재 경도
        }
        ```
* **Response Body (성공 시 - 200 OK)**:
    * `com.mogacko.mogacko.dto.CheckInResponse`
        ```json
        {
          "success": "boolean",
          "message": "string" // 예: "출석체크가 완료되었습니다.", "모임 장소에서 너무 멀리 떨어져 있습니다."
        }
        ```
* **Response Body (실패 시)**:
    * `401 Unauthorized`: 인증되지 않은 사용자
    * `404 Not Found`: 해당 그룹의 모임을 찾을 수 없음
    * `CheckInResponse`의 `success: false` 와 함께 실패 메시지 반환 (참가자가 아님, 시간/장소 조건 미충족 등)


