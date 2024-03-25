# Mechanical Management System
**[Mechanical Management System](#mechanical-management-system)**
- [Overview of the Application](#overview-of-the-application)
  - [Features and Characteristics](#features-and-characteristics)
- [Technological Stack](#technological-stack)
- [Installation Guide](#installation-guide)
- [User Guide](#user-guide)
- [Further Development Opportunities](#further-development-opportunities)
- [Expansion of Application Features](#expansion-of-application-features)
- [Contact](#contact)

# Overview of the Application

**The application I have developed is a comprehensive and versatile enterprise management system that enables companies to
manage and track their workflow processes more efficiently. The application offers various functionalities, including
management of companies, machines, workforce, and workflow processes, as well as invoice generation and related
operations.**

## Features and Characteristics

1. **Registration and Login:**  
    - Users can register with their email and password and then log in to the system with their credentials.

2. **Company Management:**
    - Create, modify, and delete companies.
    - Manage company data such as name, description, country, address... and registration details.

3. **Machine Management:**
    - Add, edit, and delete machines.
    - Upload and manage images related to machines.
    - Adding and removing components, configuring taxes, and calculating profit based on the numbers.

4. **Workforce Management:**
    - Assign employees to companies.
    - Handle job applications.
    - Manage employees' permissions and roles.

5. **Workflow Management:**
    - Create, modify, and delete workflow processes.

6. **Invoice Generation:**
    - Generate invoices for completed work or sold products.

7. **Error Handling and Security:**
    - Includes various error handling mechanisms and security measures.
    - Implements exception handling and access control to prevent unauthorized access and unexpected behavior.

## Technological Stack

- **Application Type:** Web Application
- **Language:** Java 17
- **Framework:** SpringBoot 3.1.0
- **Database Management:** MySQL 8.0.33
- **User Interface:** HTML, CSS, Bootstrap, JavaScript
- **ORM:** Spring Data JPA
- **Library:** Lombok
- **Template Engine:** Thymeleaf
- **Design Pattern:** MVC
- **Testing:** Mockito, JUnit 5
- **Coverage results:**
<img src="https://github.com/sznikolas/mechanical-management-system-project/assets/48528872/9ba78e31-a83d-40af-ad8f-74faf76863a9" alt="test_results" style="width: 60%; height: auto;">

The service layer has been tested with unit tests (the controller layer testing, Integration and UI testing are currently not implemented).


## Installation Guide
1. **Clone the GitHub Repository:**
   - Clone the project to your local machine from the GitHub repository using the following command in the command prompt or terminal
   in your IDE: **git clone <GitHub_repository_URL>**

2. **Application Configuration:**
   - The project has a **default** profile and a **test** profile. For the default profile, the `profile` section (in the application.yml) should be left empty.
   - When using the default profile, you can log in with the email `"admin@gmail.com"` and password `"admin"` on the `http://localhost:8080/system/login` page, 
   but there won't be any stored data in the database. 
     - It's predefined in the Main class, but You can use your own email address or register a new account and grant yourself administrator rights 
     with the `"admin@gmail.com"` account.<br><br>
   - When using the **"test"** profile, the database is populated with predefined data from the `test-data.sql` file. 
   - There are two predefined users: `"admin@gmail.com"` with the password `"admin"`, and `"user@gmail.com"` with the password `"user"`. 
   - The passwords are not encrypted to facilitate easier testing (config package - TestConfiguration). 
   - The admin has administrator privileges, while the user has regular user privileges.
   - Select the desired profile in the application.yml file with the setting "profiles: active:". 
   - Both profiles use a MySQL database.

3. **Database Creation and Initialization:**
   - Create a database with your own settings, then set the **URL**, **username**, and **password** in the application.yml file. 
   - If the application successfully connects to the database, the schema is automatically generated.

4. **Email Configuration:**
   - If you want to use email sending for registration, password change, etc. (recommended), set your email address in the application.yml file 
   from which the system will send notifications to users. 
   - If you're using Gmail, use your own email address for the username and a generated password from Gmail for the password. 
     - You can find more information about Gmail passwords [here](https://medium.com/@seonggil/send-email-with-spring-boot-and-gmail-27c14fc3d859).

5. **Starting the Application:**
   - Once all configurations are set, start the application and test its functionality.

6. **Testing:**
   - Ensure that the application functions properly. If you encounter any issues or have questions, you can find my contact information in the contacts section!

## User Guide
- [Images about the application and User Guide](https://github.com/sznikolas/mechanical-management-system-project/blob/main/GUIDE.md)

## Further Development Opportunities

1. ### Enhancement of User Interface:
   - Implement responsive and visually appealing design using front-end technologies.
   - Add new features such as multi-language support.

2. ### Performance Optimization:
   - Utilize caching for frequently requested data.
   - Store images externally and optimize client-side performance to improve Lighthouse test scores.

3. ### Add Unit and Integration Tests:
   - Improve code stability and quality by adding tests and increasing test coverage.

4. ### Security Enhancements:
   - Strengthen protection against vulnerabilities and ensure compliance with data protection regulations (e.g., GDPR).
   - Address any security vulnerabilities present in the codebase.

## Expansion of Application Features
- Introduce a warehouse section for managing component inventory.
- Implement functionality to save invoices to the database.
- Add additional registration and login methods (e.g., OAuth2).
- When applying to the company, the company owner should receive some form of notification.
- Incorporate additional user data fields (e.g., phone numbers).


## Contact
For any inquiries or feedback regarding the application, feel free to contact me:
- **Name:** Nikolas Szőcs
- **GitHub:** [github.com/sznikolas](https://github.com/sznikolas)
- **LinkedIn:** [linkedin.com/in/nikolas-szocs-6a202a201/](https://www.linkedin.com/in/nikolas-szocs-6a202a201/)
