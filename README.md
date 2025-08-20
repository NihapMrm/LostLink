# LostLink - Lost & Found Tracking Application

## 📱 Overview

**LostLink** is a comprehensive Android application designed to help users report and find lost items in their community. The app provides a platform where users can report lost items, find items that others have found, and communicate through an integrated chat system to facilitate the reunion of lost items with their owners.

## 🚀 Features

### Core Functionality
- **Lost Item Reporting**: Users can report items they have lost with detailed descriptions, images, and location data
- **Found Item Reporting**: Users can report items they have found to help reunite them with their owners
- **Interactive Map Integration**: Google Maps integration showing locations of lost and found items
- **Real-time Chat System**: Built-in messaging system for users to communicate about lost/found items
- **User Authentication**: Secure Firebase Authentication system
- **Profile Management**: Complete user profile management with edit capabilities
- **Notification System**: Push notifications for new messages and relevant updates

### Advanced Features
- **Location-based Search**: Radius-based search functionality (100m, 200m, 500m)
- **Image Upload**: Photo upload capability for better item identification
- **Report Management**: Users can view, edit, and manage their own reports
- **Swipe-to-Refresh**: Modern UI with pull-to-refresh functionality
- **Onboarding Experience**: Guided introduction for new users
- **Settings & Support**: Comprehensive settings and support system

## 🏗️ Architecture

### Technology Stack
- **Platform**: Android (Java)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 15)
- **Backend**: Firebase (Authentication, Firestore, Storage, Messaging)
- **Maps**: Google Maps API
- **Build System**: Gradle with Kotlin DSL
- **Architecture Pattern**: Fragment-based with ViewBinding

### Project Structure
```
app/
├── src/main/
│   ├── java/com/nihap/lostlink/
│   │   ├── onboarding/          # Onboarding flow
│   │   ├── fragments/           # UI fragments
│   │   ├── adapters/           # RecyclerView adapters
│   │   ├── models/             # Data models
│   │   ├── services/           # Background services
│   │   └── utils/              # Utility classes
│   ├── res/
│   │   ├── layout/             # XML layouts
│   │   ├── drawable/           # Vector drawables and images
│   │   ├── values/             # Colors, strings, styles
│   │   ├── menu/               # Navigation menus
│   │   └── anim/               # Animations
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## 📋 Core Components

### Activities
1. **SplashScreen**: App launch screen with 2-second delay
2. **OnboardActivity**: User onboarding with ViewPager2
3. **Login**: Firebase Authentication login
4. **Register**: User registration with validation
5. **Home**: Main activity with bottom navigation
6. **ChatRoomActivity**: Individual chat conversations

### Fragments
1. **HomeFragment**: Main dashboard with map and reports tabs
2. **ReportFragment**: Report management (Lost/Found tabs)
3. **ChatFragment**: Chat room listing
4. **ProfileFragment**: User profile display and management
5. **SettingsFragment**: App settings and logout
6. **LostFragment**: Lost items display
7. **FoundFragment**: Found items display
8. **MyReportsFragment**: User's own reports
9. **EditProfileFragment**: Profile editing
10. **SupportFragment**: Help and support

### Data Models
- **ReportDataClass**: Core data structure for lost/found reports
- **Message**: Chat message data structure
- **ChatRoom**: Chat room information
- **User**: User profile data

### Key Services
- **ChatNotificationService**: Firebase Cloud Messaging for chat notifications
- **NotificationManager**: Local notification management
- **ChatManager**: Chat functionality management

## 🔧 Dependencies

### Core Android Libraries
```kotlin
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("com.google.android.material:material:1.12.0")
implementation("androidx.activity:activity:1.10.1")
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
implementation("androidx.navigation:navigation-fragment:2.6.0")
implementation("androidx.navigation:navigation-ui:2.6.0")
```

### Firebase Services
```kotlin
implementation("com.google.firebase:firebase-auth:23.2.1")
implementation("com.google.firebase:firebase-firestore:25.1.4")
implementation("com.google.firebase:firebase-storage:21.0.2")
implementation("com.google.firebase:firebase-messaging:23.4.0")
```

### Google Services
```kotlin
implementation("com.google.android.gms:play-services-maps:19.0.0")
implementation("com.google.android.libraries.places:places:3.3.0")
```

### UI & Utility Libraries
```kotlin
implementation("com.github.bumptech.glide:glide:4.16.0")
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
implementation("com.google.android.flexbox:flexbox:3.0.0")
implementation("de.hdodenhof:circleimageview:3.1.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.work:work-runtime:2.9.0")
```

## 🔐 Permissions

The app requires the following permissions:

### Location Services
- `ACCESS_COARSE_LOCATION`: For approximate location
- `ACCESS_FINE_LOCATION`: For precise location

### Network & Storage
- `INTERNET`: For Firebase and API communication
- `ACCESS_NETWORK_STATE`: For network status monitoring
- `READ_EXTERNAL_STORAGE`: For image selection

### Notifications
- `POST_NOTIFICATIONS`: For push notifications (Android 13+)
- `VIBRATE`: For notification vibration

## 🎨 UI/UX Features

### Design Elements
- **Custom Fonts**: Agency FB font family for branding
- **Material Design**: Following Material Design 3 guidelines
- **Responsive Layout**: Adaptive layouts for different screen sizes
- **Dark Mode Support**: Theme-aware UI components
- **Animation Support**: Smooth transitions and animations

### Navigation
- **Bottom Navigation**: 5-tab navigation (Home, Reports, Chat, Profile, Settings)
- **TabLayout**: Swipe-able tabs for Lost/Found reports
- **Fragment Navigation**: Seamless fragment transitions

### Interactive Elements
- **SwipeRefreshLayout**: Pull-to-refresh functionality
- **FloatingActionButton**: Quick access to add reports
- **Bottom Sheets**: Modal dialogs for detailed views
- **Map Integration**: Interactive Google Maps with markers

## 🔥 Firebase Integration

### Authentication
- Email/Password authentication
- Automatic login persistence
- Secure user session management

### Firestore Database Structure
```
users/
├── {userId}/
│   ├── name: string
│   ├── email: string
│   ├── phone: string
│   └── profileImageUrl: string

reports/
├── {reportId}/
│   ├── reportType: "Lost" | "Found"
│   ├── itemName: string
│   ├── location: string
│   ├── geoPoint: GeoPoint
│   ├── radius: integer
│   ├── description: string
│   ├── imageUrl: string
│   ├── userId: string
│   ├── userName: string
│   └── timestamp: Timestamp

chatRooms/
├── {chatRoomId}/
│   ├── participants: [userId1, userId2]
│   ├── lastMessage: string
│   ├── lastMessageTime: Timestamp
│   └── messages/
│       └── {messageId}/
│           ├── senderId: string
│           ├── senderName: string
│           ├── message: string
│           ├── messageType: string
│           ├── timestamp: Timestamp
│           └── isRead: boolean
```

### Cloud Storage
- User profile images
- Report item images
- Automatic image compression and optimization

### Cloud Messaging
- Real-time chat notifications
- Report update notifications
- Background message handling

## 📍 Google Maps Integration

### Features
- Interactive map display on home screen
- Location picker for reports
- Marker display for lost/found items
- Radius-based search visualization
- Current location detection

### API Configuration
- Google Maps Android API
- Places API for location search
- Geocoding for address conversion

## 💬 Chat System

### Features
- **Real-time Messaging**: Instant message delivery
- **Chat Rooms**: Organized conversations between users
- **Message Types**: Text messages with future support for images
- **Read Status**: Message read indicators
- **Notifications**: Push notifications for new messages
- **User Interface**: Modern chat UI with sent/received message layouts

### Architecture
- Firebase Firestore for message storage
- Real-time listeners for instant updates
- Efficient pagination for message history
- Automatic chat room creation between users

## 👤 User Management

### Profile Features
- Complete profile management
- Profile image upload and display
- Editable user information (name, phone, email)
- User report history
- Settings and preferences

### Authentication Flow
1. Splash screen (2-second delay)
2. Onboarding (for new users)
3. Login/Register
4. Main application

## 📊 Report Management

### Report Types
- **Lost Items**: Items that users have lost
- **Found Items**: Items that users have found

### Report Features
- **Detailed Information**: Name, description, location, image
- **Location Services**: GPS coordinates and address
- **Search Radius**: Configurable search area (100m, 200m, 500m)
- **Image Upload**: Photo attachment for better identification
- **Edit/Delete**: Full CRUD operations on user's own reports
- **Time Tracking**: Automatic timestamp recording

### Report Display
- **Map View**: Visual representation on Google Maps
- **List View**: Scrollable list with detailed information
- **Filter Options**: Lost/Found categorization
- **Search Functionality**: Text-based search capabilities

## 🔔 Notification System

### Types
- **Chat Notifications**: New message alerts
- **Report Notifications**: Relevant lost/found item alerts
- **System Notifications**: App updates and information

### Features
- **Push Notifications**: Firebase Cloud Messaging
- **Notification Badge**: Unread message counter
- **Sound & Vibration**: Customizable notification alerts
- **Background Processing**: Notifications work when app is closed

## 🛠️ Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK API 24+
- Firebase project setup

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd LostLink
   ```

2. **Firebase Configuration**
   - Create a new Firebase project
   - Add Android app to Firebase project
   - Download `google-services.json`
   - Place in `app/` directory
   - Enable Authentication, Firestore, Storage, and Cloud Messaging

3. **Google Maps Setup**
   - Enable Maps SDK for Android
   - Enable Places API
   - Create API key
   - Add to `strings.xml`:
     ```xml
     <string name="map_api_key">YOUR_API_KEY_HERE</string>
     ```

4. **Build Configuration**
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

5. **Run the Application**
   - Connect Android device or start emulator
   - Run from Android Studio or:
     ```bash
     ./gradlew installDebug
     ```

### Environment Setup
- Ensure all dependencies are properly synced
- Configure Firebase project with proper security rules
- Test Google Maps API key functionality
- Verify push notification setup

## 🏃‍♂️ Running the Application

### Development Mode
1. Open project in Android Studio
2. Sync Gradle files
3. Run on emulator or physical device
4. Test all features including location and notifications

### Production Build
```bash
./gradlew assembleRelease
```

### Testing
- Unit tests: `./gradlew test`
- Instrumentation tests: `./gradlew connectedAndroidTest`

## 🔒 Security Features

### Data Protection
- Firebase security rules for data access control
- User authentication required for all operations
- Input validation and sanitization
- Secure image upload with Firebase Storage

### Privacy
- Location data encrypted and secured
- User data accessible only to authenticated users
- No sensitive information stored locally
- Compliance with Android privacy guidelines

## 🎯 User Flow

### New User Journey
1. **Splash Screen** → Brief app loading
2. **Onboarding** → Feature introduction (3 screens)
3. **Registration** → Account creation
4. **Home Screen** → Main application interface

### Existing User Journey
1. **Splash Screen** → Auto-login check
2. **Home Screen** → Direct access (if logged in)

### Core Workflows

#### Reporting a Lost Item
1. Navigate to Reports tab
2. Tap "Add Report" FAB
3. Select "Lost" type
4. Fill in item details
5. Select location on map
6. Upload optional image
7. Submit report

#### Finding an Item
1. Navigate to Home tab
2. View map with markers
3. Browse Lost/Found tabs
4. Tap on relevant item
5. View details in bottom sheet
6. Contact item owner via chat

#### Communication
1. Tap "Contact" on item
2. Auto-create chat room
3. Send messages
4. Receive real-time responses
5. Coordinate item return

## 🔧 Customization

### Theme Customization
- Colors defined in `values/colors.xml`
- Styles in `values/styles.xml`
- Support for day/night themes

### Feature Configuration
- Notification settings in preferences
- Location accuracy settings
- Chat notification preferences
- Search radius options

## 🐛 Troubleshooting

### Common Issues
1. **Location Not Working**
   - Ensure location permissions granted
   - Check GPS is enabled
   - Verify Google Maps API key

2. **Firebase Connection Issues**
   - Verify `google-services.json` is present
   - Check internet connectivity
   - Ensure Firebase project is active

3. **Push Notifications Not Working**
   - Check notification permissions (Android 13+)
   - Verify FCM token generation
   - Test on physical device

4. **Map Not Loading**
   - Verify Maps API key
   - Check API quotas and billing
   - Ensure Maps SDK is enabled

### Performance Optimization
- Image compression for uploads
- Efficient RecyclerView implementations
- Proper lifecycle management
- Memory leak prevention

## 📈 Future Enhancements

### Planned Features
- **Advanced Search**: Filter by item category, date range
- **Image Recognition**: AI-powered item matching
- **Social Features**: User ratings and reviews
- **Offline Mode**: Cached data for offline viewing
- **Multi-language Support**: Internationalization
- **Analytics Dashboard**: Usage statistics and insights

### Technical Improvements
- **Kotlin Migration**: Convert from Java to Kotlin
- **Architecture Components**: ViewModel, LiveData integration
- **Dependency Injection**: Dagger/Hilt implementation
- **Testing**: Comprehensive test coverage
- **CI/CD Pipeline**: Automated build and deployment

## 👥 Contributing

### Development Guidelines
- Follow Android coding standards
- Use meaningful commit messages
- Write comprehensive documentation
- Include unit tests for new features
- Follow Material Design principles

### Code Style
- Java 11+ features where applicable
- Consistent naming conventions
- Proper commenting and documentation
- Error handling and edge cases

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Developer Information

- **Developer**: Nihap
- **Email**: nihapmrm@gmail.com
- **Package**: com.nihap.lostlink
- **Version**: 1.0 (Version Code: 1)

## 🙏 Acknowledgments

- Firebase for backend services
- Google Maps for location services
- Material Design for UI guidelines
- Open source community for libraries and tools

## 📞 Support

For support, bug reports, or feature requests:
- Create an issue in the repository
- Contact: nihapmrm@gmail.com
- Use the in-app Support feature

---

**LostLink** - Connecting lost items with their owners through technology and community collaboration.
