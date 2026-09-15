# Madrassa — EduNoor
## Master Product Specification

This document is the governing product direction for Madrassa — EduNoor.
Future engineering stages must preserve these requirements unless explicitly revised.

## 1. V6 Visual Foundation
Preserve the approved V6 landing-page composition, Islamic visual identity,
parchment/walnut/clay/gold palette, professional typography, subtle architecture,
MADRASSA + إدارة header, Qur'anic welcome, role cards, EDU NOOR identity,
and bottom utility bar.

## 2. Islamic Education Photography
Use approximately three clean, professional visual assets:
- Madrassa students
- Qur'an / Islamic books
- Islamic education / Ustadh environment

Images must support the interface without overwhelming the V6 design.
Different image arrangements may be used for Parent and Ustadh experiences.

## 3. Separate Parent and Ustadh Gateways
Parent and Ustadh must have distinct authentication and workflows.

## 4. Parent Authentication
Parent login uses registered phone number and password.
Forgot-password/reset and event-based OTP must be supported.
OTP must not be unnecessarily sent on every login.

## 5. Multi-Child Parent Account
One parent account may be linked to multiple students.
After login, the parent selects the student whose information is displayed.
A parent must only access students explicitly linked to that account.

## 6. Ustadh Authentication
Ustadh login supports:
- Madrassa name
- Phone number
- Password
- Forgot password / Reset
- Register Madrassa / Sajili Madrassa

## 7. Madrassa Registration and Approval
Registration collects appropriate:
- Madrassa name
- Madrassa type/administration
- Ustadh information
- Head Ustadh
- Phone numbers
- Email
- Region
- District
- Ward
- Area/street
- Nearby Masjid / landmark
- Masjid information where applicable

Registration has an approval workflow before full activation.

## 8. Ustadh-Created Students
Students are registered by authorized Ustadh/admin users.
Student records include:
- Full student name
- Parent/guardian name
- Main phone
- Secondary/emergency phone
- Class
- Qur'an level
- Hifz level
- Other programmes/books

## 9. Configurable Academic Structure
Madrassa setup defines classes and programmes.
Qur'an is included by default.
Tauwhid and Fiqh are initial default programmes.
Ustadhs can add additional programmes/books.
Student admission uses selections from configured programmes.

## 10. Initial Parent Activation
New parent credentials may use an initial activation password.
On first successful login, the parent must be guided to create a new password.
Password rules must require at least six characters and include letters and numbers.

## 11. Branded Login Initialization
After successful authentication, show a short professional EduNoor
initialization/welcome transition.
It should eventually represent actual initialization state rather than
a fake prolonged loading delay.
For parents, the Madrassa name should be prominently presented.

## 12. Ustadh Dashboard
Ustadhs need a professional management dashboard covering:
- Students
- Parents
- Classes
- Attendance
- Fees
- Qur'an
- Hifz
- Books/programmes
- SMS
- Reports
- Settings

## 13. SMS Management and Business Model
SMS is an important initial EduNoor business service.
Support:
- OTP
- Bulk SMS
- Class/group messaging
- Fee reminders
- Announcements
- Emergency messages
- Templates
- Delivery status
- Delayed/failed message monitoring
- Secondary provider/routing fallback where appropriate

Provider integration must be abstracted so EduNoor is not locked to one provider.

## 14. Manual Fee Contributions
The system supports:
- Tuition
- Other fees
- Contributions
- Balances
- Deadlines
- Payment history

Manual contribution workflow:
Parent submits contribution.
Ustadh sees it as pending.
Ustadh confirms after receiving/verifying funds.
A correction/unconfirmation window of approximately 30 minutes may be
provided before the record becomes locked.

## 15. Payment Gateway Architecture
Payment gateway integration is part of the future architecture but is not
required for the initial release.
The architecture must allow:
- Mobile-money gateway payments
- Card payments
- Tuition subscriptions
- Automatic recurring tuition
- Payment confirmation
- Receipts
- Payment failure handling

Gateway credentials must never be embedded in the APK.

## 16. Early Tuition Notifications
Madrassas can configure tuition deadlines and reminder timing.
Parents can receive early SMS notifications before tuition becomes due,
as well as appropriate overdue reminders.

## 17. Support and Contact
Help/support information must be easily accessible.
Madrassa-specific contacts and EduNoor support channels can be configured.

## 18. Controlled Advertising
Advertising must remain subtle and non-disruptive.
Avoid inappropriate, sexual, gambling, exploitative, or anti-Islamic
advertising.
Core educational and management workflows should remain clean.

## 19. Security and Privacy
Use least-privilege access.
Parent access must be restricted to linked students.
Ustadh/admin operations require appropriate authorization.
Student deletion should use archive/soft-delete and recovery before
permanent deletion.
Sensitive credentials and payment secrets must not be stored in the APK.

## 20. Backup, Sync and Cost Control
Prefer local-first operation where appropriate.
Cloud services should be used efficiently for authentication, synchronization,
security, messaging triggers, payment webhooks and other necessary services.
Backups should be designed so Madrassa-owned records can be recovered.
Operating costs must be controlled so SMS/service revenue is not consumed
by unnecessary infrastructure.

## 21. Progressive Engineering
Major development stages follow:

Engineer
→ Build APK
→ Install on second Android device
→ Test
→ Approve
→ Git checkpoint
→ Next stage

Do not build the entire backend at once.
First establish the user experience and data model, then introduce the
real local database/authentication, then synchronization/security,
messaging, payments and advanced services.

## Language Requirements
Supported from the beginning:
1. Kiswahili — default
2. English
3. Arabic — proper RTL

## Product Principle
Madrassa — EduNoor must feel like a serious Islamic education management
system rather than a generic chatbot or generic school-management template.
It should remain lightweight, professional, explainable, secure and
economically sustainable.
