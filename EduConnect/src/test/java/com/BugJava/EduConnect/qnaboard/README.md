# QnA Board 통합 테스트

QnA Board 기능의 통합 테스트 코드입니다.

## 테스트 구성

### 1. 핵심 통합 테스트 (`QnaQuestionsIntegrationTest.java`)
- **CRUD 기능 테스트**: 질문 등록, 조회, 삭제
- **검색 및 필터링 테스트**: 트랙별 검색, 키워드 검색, 복합 검색
- **권한 관리 테스트**: JWT 인증, 권한 검증, 보안 테스트

### 2. 성능 및 엣지 케이스 테스트 (`QnaSearchPerformanceTest.java`)
- **성능 테스트**: 대용량 데이터 검색, 페이징 성능
- **엣지 케이스**: 특수문자, 긴 키워드, 빈 키워드 처리
- **동시 요청 처리 테스트**

### 3. 지원 유틸리티
- `QnaTestDataBuilder.java`: 테스트 데이터 빌더 패턴
- `QnaTestConfig.java`: 테스트 환경 설정
- `application-test.properties`: 테스트용 프로퍼티

## 테스트 실행 방법

### 전체 테스트 실행
```bash
./gradlew test
```

### QnA 테스트만 실행
```bash
./gradlew test --tests "com.BugJava.EduConnect.qnaboard.*"
```

### 특정 테스트 클래스 실행
```bash
# 통합 테스트
./gradlew test --tests "QnaQuestionsIntegrationTest"

# 성능 테스트
./gradlew test --tests "QnaSearchPerformanceTest"
```

### 특정 테스트 메서드 실행
```bash
./gradlew test --tests "QnaQuestionsIntegrationTest.createQuestion_Student_Success"
```

## 테스트 범위

### ✅ 완전 테스트 커버리지
- 질문 CRUD 작업 (등록, 조회, 삭제)
- 트랙별 질문 검색
- 키워드 기반 검색
- 복합 검색 (트랙 + 키워드)
- JWT 인증 및 권한 검증
- 페이징 및 정렬
- Soft Delete 동작
- 에러 처리 (404, 401, 403)

### ⚠️ 부분 테스트 (미구현 기능)
- 질문 수정 기능 (QuestionUpdateRequest 미구현)
- 답변/댓글 시스템 (서비스 로직 미구현)

## 테스트 데이터

### 사용자 유형
- `STUDENT`: 일반 학생 사용자
- `INSTRUCTOR`: 강사 사용자
- 다양한 트랙: `BACKEND`, `FRONTEND`, `FULLSTACK`, `TRACK_INSTRUCTOR`

### 테스트 시나리오
1. **정상 케이스**: 권한 있는 사용자의 정상적인 CRUD 작업
2. **권한 에러**: 권한 없는 사용자의 작업 시도
3. **데이터 검증**: 잘못된 입력값 처리
4. **성능 테스트**: 대용량 데이터에서의 검색 성능
5. **엣지 케이스**: 특수문자, 빈 값, 극단적 케이스

## 테스트 환경 설정

### H2 인메모리 데이터베이스
- 테스트 실행 시마다 초기화
- 테스트 격리 보장

### JWT 테스트 토큰
- 테스트용 시크릿 키 사용
- 각 테스트마다 새로운 토큰 생성

### Spring Security 테스트
- `@WithMockUser` 대신 실제 JWT 토큰 사용
- 실제 인증/인가 플로우 테스트

## 주요 테스트 패턴

### Given-When-Then 패턴
```java
// given - 테스트 조건 설정
QuestionCreateRequest request = createSampleRequest();

// when & then - 실행 및 검증
mockMvc.perform(post("/api/qna/questions")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
```

### 빌더 패턴 활용
```java
Users student = QnaTestDataBuilder.aBackendStudent()
    .email("test@example.com")
    .name("테스트학생")
    .build();
```

## 성능 기준

### 응답 시간 기준
- 단일 조회: < 500ms
- 검색 (1000건): < 5초
- 페이징 (마지막 페이지): < 3초
- 복합 검색: < 2초

### 동시성 테스트
- 5개 동시 요청 처리 가능
- 데이터 일관성 보장

## 문제 해결

### 테스트 실패 시 체크리스트
1. H2 데이터베이스 초기화 확인
2. JWT 토큰 생성 및 유효성 확인
3. 테스트 데이터 설정 확인
4. Spring Security 설정 확인
5. JSON 응답 구조 확인