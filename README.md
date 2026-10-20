# TeacherMatch Web — Java Spring Boot

Prototype web app for the TeacherMatch UML and supplied GUI references.

## Requirements
- IntelliJ IDEA
- JDK 17+
- Maven (IntelliJ can import the `pom.xml` and download dependencies)
- Internet is only needed the first time Maven downloads dependencies; Google Fonts are optional.

## Run in IntelliJ
1. Extract this ZIP.
2. Open the `TeacherMatchWeb` folder in IntelliJ as a Maven project.
3. Set Project SDK to Java 17 or newer.
4. Open `src/main/java/vn/teachermatch/TeacherMatchApplication.java`.
5. Click the green Run icon.
6. Open http://localhost:8080

## Demo accounts
| Role | Username | Password |
|---|---|---|
| System Admin | `admin` | `admin123` |
| Matching Staff | `staff` | `staff123` |
| Partner | `partner` | `partner123` |
| Teacher | `teacher` | `teacher123` |

## Features implemented
- Shared login and role-based dashboard routing.
- Admin: account creation and system activity log.
- Matching Staff: teacher pool, profile status changes, partner access grants, interview coordination and feedback tracking.
- Partner: view shared profiles, request interviews, submit results/feedback.
- Teacher: edit profile, upload CV/video/certificates, accept or decline interviews.
- Responsive UI inspired by the provided screenshots.
- In-memory seed data so the demo starts immediately.

## Notes / limitations
This is a runnable course-project prototype, not a production-ready deployment. Data is stored in memory and resets when the app restarts; uploaded files are stored in the local `uploads/` folder. Passwords are plain text for demo purposes. Before real deployment, add Spring Security, password hashing, database persistence, validation, file type/size checks, per-user authorization for every record, and a real meeting-link integration. Interview meeting URLs are placeholders. The profile model follows the UML concepts `UserAccount`, `TeacherProfile`, `MediaDocument`, `MatchStatusHistory`, `ShortlistDecision`, and `Interview`; this starter keeps the data layer deliberately lightweight so it is easy to run and extend in IntelliJ.
