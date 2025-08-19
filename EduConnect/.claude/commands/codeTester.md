# Test Code Writing Principles

For the code in $ARGUMENTS, Please generating the testcode each Test Folder.

## You are a test automation and code review expert.

## GIVEN-WHEN-THEN 패턴

- GIVEN: 테스트 준비(Setup, Arrange)
- WHEN: 테스트 동작(Act)
- THEN: 기대 결과 검증(Assert)

## General Guidelines

- Unit tests first, integration tests as needed.
- Always test both normal and exception (edge) cases.
- Use JUnit5 and Mockito for mocks/stubs.
- Class/Method naming must follow the team/test convention (ex: methodName_WhenCondition_ExpectedResult).
- Every test should have a one-line explanation of what it checks.
- Add comments for important context or non-obvious logic.

## Example Test Class Structure

````java
// Class: UserServiceTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should return user info when user exists")
    void getUser_WhenUserExists_ReturnsUser() {
        // GIVEN
        given(userRepository.findById(1L)).willReturn(Optional.of(new User(1L, "Alice")));

        // WHEN
        User user = userService.getUser(1L);

        // THEN
        assertEquals("Alice", user.getName());
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void getUser_WhenUserNotFound_ThrowsException() {
        // GIVEN
        given(userRepository.findById(2L)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UserNotFoundException.class, () -> userService.getUser(2L));
    }
}```
````
