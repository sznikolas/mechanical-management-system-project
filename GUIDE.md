# User Guide
**[User Guide](#user-guide)**
- [Login](#login)
- [Join Company](#join-company)
  - [Company details](#company-details)
- [Company(specific company)](#companyspecific-company)
- [Users](#users)
- [Profile](#profile)
- [Machines](#machines)
  - [Add new](#add-new)
- [Machine's Details](#machines-details)
- [Invoice](#invoice)
- [Company page](#company-page)
- [Here are additional important pieces of information about the application](#here-are-additional-important-pieces-of-information-about-the-application)

## Login
- Open the application in your browser at the following address: `localhost:8080/system`
- Here you can log in with the email address `admin@gmail.com` and the associated password `admin`.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/913a54ee-3db7-4903-8ddd-fa2eb339deee" alt="login" style="width: 80%; height: auto;">

## Join Company
- After logging in, you will be directed to the `Join Company` page, where you can see the list of companies.
  - You can apply to companies if you wish.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/1a427335-04a9-4cd5-aac6-130958d2d9f0" alt="joinCompany" style="width: 80%; height: auto;">

### Company details
- You can view the **details** of the companies by clicking on the `Details` button.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/17d7ec7b-d791-4ade-b049-63d41d2039af" alt="companyDetails" style="width: 80%; height: auto;">

## Company(specific company)
- On the `Company` page, if you have the appropriate **permissions**, you can modify the details of the selected company.
- Here, you can manage both the applicants applying to the company and the employees already working there.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/a6283d47-70c1-4f71-ba3a-72ae167175f4" alt="company01" style="width: 80%; height: auto;">

## Users
- On the `Users` page, you can view the **details** of the selected user.
- You can also **block** the user's account and modify their **access rights**.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/3d1c041d-612a-4020-97f4-136f30e0f8eb" alt="userDetails" style="width: 80%; height: auto;">

## Profile
- On the `Profile` page, you can see the details of the **currently logged-in** user.
- You can change your **first and last name** and request a **password change**, which you can do **through your registered email address**.
- Below the user data, you will see the list of **companies** where you have been **accepted**. Here, you can leave companies by clicking the `QUIT` button.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/838f87a3-153c-4f96-8e07-6045b88ad544" alt="profile" style="width: 80%; height: auto;">

## Machines
- The `Machines` page is one of the **most important** sections.
- Here, you can select the **year**, **month**, and **companies** to retrieve the **machines**.
- In the table, you can choose how many machines to **display per page** and perform specific **searches** among the machines in the `search` section.
- You can also click on the headers of the table to **sort** the data in **ascending** and **descending** order.
- The default setting is to always display the **most recently registered machine at the top**. <br><br>
- Below the table, there is a **statistics** section about the selected companies, where you can also see the monthly **profit**.
- Only users with appropriate permissions can view this. <br><br>
- By clicking on the **plus button**, you will be directed to the details page of the selected machine.
- Clicking on the **trash can icon** will **deactivate** the machine's status, and it will be moved to the `Deleted machines` section, where you can **permanently delete it** from the system.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/7044711e-ec52-4a5b-999c-0c09da25988b" alt="machines" style="width: 80%; height: auto;">

### Add new
- By clicking on the `Add new` button, you can add **new machines** to the selected company.
- When you add them, they are saved with the current date and time in the system.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/799e5834-845e-4067-b74b-3a3ac1f1f00b" alt="addNewMachine" style="width: 80%; height: auto;">

## Machine's Details
- Clicking on the selected machine will take you to the `Machine's Details` page.
- Here, you can modify the machine's **information** and **generate an invoice**.
- Below the machine's details, you can **add components**, which will appear on the invoice as well.
- You can set the **tax rate** if desired (between 0 and 100), and the values will be updated proportionally with this percentage.
- Additionally, you can add **pictures** of the selected machine if necessary.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/3b72aaf1-003f-4c5d-9936-8ade70e15fb8" alt="machineDetails" style="width: 80%; height: auto;">

## Invoice
- The generated **invoice** includes the following details:
  - **the issuance date**,
  - **the name of the company**,
  - **the associated data related to the company to which the machine belongs**,
  - **the name and description of the machine**,
  - **the payment details**,
  - **the registration date of the machine**,
  - **the components associated with the machine that needed repair/replacement and their prices**,
  - **and finally, the total amount to be paid and the name of the employee who issued the invoice**.
- This can be **printed** by clicking on the `print` button (bottom right corner), which is handed over to the owner of the machine.
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/3c366a85-8752-48d7-89b5-cdf57bfece65" alt="invoice" style="width: 80%; height: auto;">

## Company page
- On the `Company page`, you can see the list of companies.
- Here, you can also **register** new companies if desired (for example, if you have multiple businesses/companies).
- This requires appropriate permissions (**admin** rights).
<img src="https://github.com/sznikolas/mechanical-management-system/assets/48528872/e08811ce-8d7c-4e79-bb18-adcbe925822d" alt="companies" style="width: 80%; height: auto;">

<br><br>
# Here are additional important pieces of information about the application

- By default, two roles are created: `admin` and `user`. An admin user has access to **everything**, while a user has access only to **basic** functionalities.
- When registering a new company in the system, a **new role** is automatically generated for the company.
- A user with the `user` role can access company-related data, such as **companies**, **machines**, and **invoices**, only after(!) joining the company **successfully**. Otherwise, if they attempt to access data they don't have permission for, they will be redirected to the `Access Denied` page.
- Only the `owner` can **delete** companies, and only the owner can **permanently delete machines**.
- In companies, we can set up a **deputy manager/leader** who can control the company's data and employees.
- If the deputy manager **leaves** the company, the **owner** will take their place by default, but the owner can choose not to appoint anyone as a deputy.
- When **dismissing** a deputy manager, remember to update the deputy manager role in the company. Their rights will be revoked, but their name will remain.
- There are conditions that must be met when creating companies and machines, and users will be notified of these requirements.
- Only users with the `admin` role can access user-related functionalities (**it is not recommended to grant `admin` privileges unless trusted**).
- If the **database** and **email sending functionalities** are successfully set up as described on the app. information page, users will receive notifications about **registration**, **email confirmation**, **forgotten password**, **password change request**, and **successful password change** (so about everything).
- Users will receive **notifications** on the user interface if there are any issues during **login or registration**.
- For unconfirmed users, we can **request a new email confirmation**.
- **Only one session per user can be active at a time**.
- After a few minutes of inactivity, the system **logs out** the user.
- The system has both **dark and light modes**, where colors change to enhance user experience.
- **This is a portfolio project, and its further development opportunities can be found in the project description**.




   
