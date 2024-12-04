# FreshLife

FreshLife is an Android app designed to help users track food items in their inventory and shopping list. With FreshLife, users can efficiently manage their food storage, reduce waste, and stay organized.

## Features

### User Authentication
- **Firebase Authentication**: Secure login and registration.
- **Google Sign-In**: Sign in with your Google account.
- **Forgot Password**: Reset your password with a single click.

### Inventory Management
- **Add, Edit, Delete Items**: Manage food items in your inventory.
- **Categorization**: Organize items by category, location (e.g., Fridge, Pantry), and expiration date.
- **Sorting Options**: Sort items alphabetically, by category, or by expiration date.

### Shopping List
- **Add Items to Shopping List**: Add items to your shopping list directly from the inventory.
- **Prioritization**: Mark items as checked/unchecked for easy tracking.

### Notifications
- **Expiration Reminders**: Receive notifications for food items nearing expiration.

### Multi-Device Sync
- **Firebase Integration**: All data is stored in the cloud and synced across devices.

## Tech Stack

### Frontend
- **Java**: Main language for Android development.
- **XML**: Layout and UI design.
- **Material Design**: Used for a modern, clean UI.

### Backend
- **Node.js**: Handles API requests.
- **Express.js**: Framework for the backend.
- **Firebase Admin SDK**: For token verification and user authentication.
- **MongoDB**: Database for storing user-specific data.

### Tools
- **Retrofit**: For making API calls to the backend.
- **SharedPreferences**: For local storage of authentication tokens.
- **Espresso**: For UI testing.
- **JUnit**: For unit testing.
