[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/HpD0QZBI)

# PeerPrep - Collaborative Coding Platform
## Group: G13

A modern peer programming platform that connects programmers for real-time collaborative coding sessions on algorithmic problems.

---

## Features

### Smart Matching
- Find coding partners with a single click
- Instant pairing based on your skill level and preferences
- Choose from available coding problems to solve together

### Real-time Code Collaboration
- Live code editor that syncs instantly with your partner
- See your partner's cursor and code changes in real-time
- Both partners can edit simultaneously without conflicts

### Extensive Problem Library
- Curated collection of coding problems by difficulty level
- Problems organized by topic (arrays, graphs, dynamic programming, etc.)
- Detailed problem statements with examples and explanations

### User Profiles & Roles
- Create your profile and collaborate with others
- Admins can manage the platform and user accounts
- Question masters can curate and update the problem library
- View other programmers' profiles and experience levels

### Dashboard
- See your recent coding sessions and partners
- Track problems solved and time spent coding
- Quick access to start matching or browse problems
- View your achievements and progress

### Secure & Easy Authentication
- Sign up with a username and password
- Secure login to access your profile and history
- Your sessions and data are protected

---

## How to Use

### For Regular Users

1. **Sign Up / Login**
   - Create an account or login with your credentials
   - Access your personal dashboard

2. **Browse Problems**
   - Go to Questions section
   - Filter by difficulty or topic
   - Click on any problem to see details

3. **Find a Partner**
   - Select a problem you want to solve
   - Choose a difficulty level
   - Click "Match" to queue
   - Get paired with another user automatically
   - Start collaborating in real-time

4. **Collaborate**
   - Edit code together in the shared editor
   - Discuss solutions via the collaboration space
   - Both partners have full edit access

### For Question Managers

- Create new problems with detailed statements
- Edit and update existing problems
- Organize problems by difficulty and topics
- Manage the problem library

### For Admins

- Manage user roles (User, Question Manager, Admin)
- Monitor platform usage and statistics
- System configuration and maintenance

---

## Matching Logic

The matching system in PeerPrep intelligently pairs users for collaborative coding sessions:

### How Matching Works

1. **Queue System**
   - When you click "Match", the system initiates a search for compatible partners
   - If no immediate match is found, your profile is added to a waiting queue

2. **Pairing Algorithm**
   - Users are matched based on multiple levels of compatibility:
     - **Regular Match**: Same topic and difficulty level
     - **Loose Match**: 
       - Prioritize same topic but lower difficulty level
       - Then same topic but higher difficulty level
       - Then different topic but same difficulty level
       - Then different topic but lower difficulty level
       - Then different topic but higher difficulty level
   
3. **Match Found**
   - Once a compatible partner is found, both users are notified
   - A collaboration session is created automatically
   - Both users are directed to the shared editor

4. **Session Duration**
   - Sessions last until either user chooses to end it
   - Either user can click "End Session" to finish collaborating

### Matching States

- **Waiting**: Your profile is in the queue, system is searching for a match
- **Matched**: A partner found! You're about to enter the collaboration room
- **In Session**: You're actively collaborating with your partner
- **Session Ended**: Collaboration is complete, feedback is collected

### Tips for Faster Matching

- **Choose Popular Topics**: More users practicing the same topic = faster matches
- **Be Flexible with Difficulty**: Select difficulty levels that have more users
- **Off-Peak Hours**: Matching might be slower during low-traffic periods

---

## Pages & Features

| Page | Feature | Who Can Access   |
|------|---------|------------------|
| **Dashboard** | Overview of recent sessions, stats, quick actions | All Users        |
| **Browse Questions** | Search and filter coding problems | All Users        |
| **Question Details** | View problem statement, examples, attempt solution | All Users        |
| **Matching** | Find and match with other users | All Users        |
| **Collaboration Room** | Real-time code editing with partner | During Session   |
| **Create Question** | Add new problems to library | Question Masters |
| **Edit Question** | Modify existing problems | Question Masters |
| **Admin Panel** | Manage users and system settings | Admins           |

---

## User Roles

### Regular User
- Browse and solve problems
- Find partners and collaborate
- View profile and history
- Rate and review problem difficulty

### Question Master
- All regular user permissions
- Create and edit problems
- Manage problem categories and difficulty levels
- Curate the problem library

### Admin
- All permissions
- User account management
- System monitoring and analytics
- Platform configuration and maintenance

---

## Workflow Example

**John wants to practice coding:**
1. John signs up and logs in
2. He goes to the matching page
3. He selects a topic he wants to practice (e.g., "Dynamic Programming") and a difficulty level (e.g., "Medium")
4. He clicks "Match"
5. System pairs him with Sarah who also wants to solve the same type of problem
6. They're taken to a collaboration room with a shared code editor
7. John and Sarah work together, both editing the code in real-time
8. John and Sarah discuss their approach and solve the problem together
9. They finish the session and John clicks "End Session"

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| **Frontend** | React with Vite |
| **UI Components** | Mantine |
| **Backend Services** | Spring Boot (Java) + Node.js |
| **Real-time Sync** | WebSocket + Yjs |
| **Database** | PostgreSQL |
| **Caching** | Redis |
| **Message Queue** | RabbitMQ |
| **Deployment** | Docker Compose |

---

## Common Questions

### How does the matching work?
When you join the matching queue, you'll be paired with another user who selected the same problem and is also waiting. The system prioritizes based on availability.

### Can I work alone on a problem?
You can view and understand any problem in the library. But collaborative sessions require two users. For solo practice, just read the problem details.

### What if my internet connection drops?
Your session will be preserved if the other user is still connected. If you reconnect within a short period, you can resume the session with the same link.

### What happens if both me and my partner disconnect?
Your session will be preserved for a short period. Try refreshing the page to reconnect to the same session. If it expires, you'll need to start a new match.

---

## Contributors

Nguyen Tran Thanh Minh
Vu Hoang Quoc Bao
Chu Duong Huy Phuoc 
