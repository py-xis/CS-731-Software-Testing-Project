# CS731 Software Testing - Project Report

---

## Team Members

| Name            | Roll Number |
| --------------- | ----------- |
| Aryan Singhal   | IMT2022036  |
| Pranav Kulkarni | IMT2022053  |

---

## 1. Project Overview

**Project Title:** Two-Level Mutation Testing of Course Registration System

**Tech Stack:**
- Java 11
- Maven 3.8+
- JUnit 5 (Jupiter)
- PITest 1.14.2

---

## 2. AI Tool Acknowledgment

**AI Tool:** Claude Sonnet 4.5 (Thinking)

**AI Generated:**
- Production code architecture in `src/main/java/**`
- Basic code structure

**Human Written:**
- All test cases in `src/test/java/**`
- Test design and strategy
- Mutation testing configuration
- All assertions and test logic

**LLM Refined:**
- Documentation and reports

---

## 3. Individual Contributions

**Aryan Singhal (IMT2022036):**
- Modules: `repository`, `core`
- Test cases for above modules

**Pranav Kulkarni (IMT2022053):**
- Modules: `model`, `analytics`, `engine`
- Test cases for above modules

---

## 4. Test Design & Mutation Testing

### 4.1 Test Design Techniques

1. Boundary Value Analysis
2. Equivalence Partitioning
3. Decision Table Testing
4. State Transition Testing
5. Integration Testing

### 4.2 Mutation Testing Strategy

**Unit Level:**
- Target: Models, engines, repositories, analytics
- Focus: Statement-level mutations

**Integration Level:**
- Target: Registrar and engine orchestration
- Focus: Component interactions

### 4.3 Test Suite (191 Tests)

| Test Class               | Tests | Purpose                   |
| ------------------------ | ----- | ------------------------- |
| SeatAllocatorTest        | 43    | Seat allocation logic     |
| WaitlistManagerTest      | 38    | Waitlist queue operations |
| PrerequisiteEngineTest   | 27    | Prerequisite validation   |
| RegistrarTest            | 21    | Integration workflows     |
| EnrollmentRepositoryTest | 16    | Repository CRUD           |
| StudentRepositoryTest    | 12    | Student data management   |
| CourseTest               | 12    | Course model tests        |
| EnrollmentStatisticsTest | 10    | Analytics calculations    |
| EnrollmentTest           | 5     | Enrollment validation     |
| CourseRepositoryTest     | 5     | Course repository         |
| StudentTest              | 2     | Student model tests       |

### 4.4 Unit-Level Results (4 Operators)

| Operator              | Description            | Killed  | Total   | Kill Rate |
| --------------------- | ---------------------- | ------- | ------- | --------- |
| NEGATE_CONDITIONALS   | Negates conditionals   | 101     | 109     | 93%       |
| MATH                  | Changes math operators | 19      | 21      | 90%       |
| CONDITIONALS_BOUNDARY | Changes boundaries     | 17      | 29      | 59%       |
| INCREMENTS            | Changes increments     | 1       | 1       | 100%      |
| **Total**             |                        | **138** | **160** | **86%**   |

### 4.5 Integration-Level Results (6 Operators)

| Operator              | Description          | Killed  | Total   | Kill Rate |
| --------------------- | -------------------- | ------- | ------- | --------- |
| PRIMITIVE_RETURNS     | Returns 0            | 7       | 7       | 100%      |
| NON_VOID_METHOD_CALLS | Removes method calls | 159     | 182     | 87%       |
| FALSE_RETURNS         | Returns false        | 6       | 7       | 86%       |
| NULL_RETURNS          | Returns null         | 21      | 25      | 84%       |
| TRUE_RETURNS          | Returns true         | 18      | 26      | 69%       |
| EMPTY_RETURNS         | Returns empty        | 2       | 5       | 40%       |
| **Total**             |                      | **213** | **252** | **84%**   |

**Overall Mutation Score: 85.19%**

---

## 5. Tools Used

**JUnit 5:**
- Unit testing framework
- Assertions

**Maven:**
- Build automation
- Dependency management

**PITest 1.14.2:**
- Mutation testing
- Report generation

---

## 6. How to Run

**Prerequisites:**
```bash
java -version  # Verify Java 11+
mvn -version   # Verify Maven 3.8+
```

**Run All Tests:**
```bash
mvn clean test
```

**Unit Mutation Testing:**
```bash
mvn org.pitest:pitest-maven:mutationCoverage@unit-mutation-testing
```
Report: `target/pit-reports/unit/index.html`

**Integration Mutation Testing:**
```bash
mvn org.pitest:pitest-maven:mutationCoverage@integration-mutation-testing
```
Report: `target/pit-reports/integration/index.html`

---

## 7. Test Execution Screenshots

### 7.1 All Tests Passing

![Screenshot 2025-11-25 at 23.57.57.png](attachment:54f71668-b953-4859-a855-8401d2a2ca41:Screenshot_2025-11-25_at_23.57.57.png)

### 7.2 Unit-Level Mutation Results

![Screenshot 2025-11-25 at 23.58.19.png](attachment:b0f79e3c-5f23-45ba-8eef-1e6472e4a165:Screenshot_2025-11-25_at_23.58.19.png)

![Screenshot 2025-11-25 at 23.58.24.png](attachment:0661ba05-753f-4be8-9db1-fcf080535f6e:Screenshot_2025-11-25_at_23.58.24.png)

![Screenshot 2025-11-25 at 23.58.37.png](attachment:1b433a0f-3cb0-47f8-b33e-73c45be4d486:Screenshot_2025-11-25_at_23.58.37.png)

### 7.3 Integration-Level Mutation Results

![Screenshot 2025-11-25 at 23.58.58.png](attachment:304615e9-35f1-4fd2-90f6-c62c9309a5d1:Screenshot_2025-11-25_at_23.58.58.png)

![Screenshot 2025-11-25 at 23.59.05.png](attachment:51060a56-7d1d-4d4f-8055-038f34073763:Screenshot_2025-11-25_at_23.59.05.png)

![Screenshot 2025-11-25 at 23.59.12.png](attachment:d2c62f77-5668-4f7f-8a39-941d5adc3adf:Screenshot_2025-11-25_at_23.59.12.png)

---

## 8. Repository Structure

```
Project/
├── pom.xml                    # Maven config
├── REPORT.md                  # This file
├── README.md                  # How to run
└── src/
    ├── main/java/edu/courseregistration/
    │   ├── model/             # Domain models
    │   ├── engine/            # Business logic
    │   ├── repository/        # Data access
    │   ├── core/              # Integration
    │   ├── result/            # Result objects
    │   └── analytics/         # Analytics
    └── test/java/edu/courseregistration/
        ├── model/             # Model tests
        ├── engine/            # Engine tests
        ├── repository/        # Repository tests
        ├── core/              # Integration tests
        └── analytics/         # Analytics tests
```

---

## 9. Conclusion

This project demonstrates two-level mutation testing on a Course Registration System.

**Results:**
- Unit-level: 86% kill rate
- Integration-level: 84% kill rate
- Overall: 85.19% kill rate

**Achievement:**
- 191 hand-written test cases
- Exceeds industry standard (60-70%)
- All mutation operators properly configured

---

## 10. References

1. PIT Mutation Testing: http://pitest.org/
2. JUnit 5: https://junit.org/junit5/
3. Apache Maven: https://maven.apache.org/
