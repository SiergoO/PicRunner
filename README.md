# PicRunner

# An android application for tracking views while walking, hiking, running :

- Every 100 meters photos are fetched from Flickr API based on user's location;
- If no fresh data was fetched search radius increase;
- Pictures are shown in a scrollable list;
- Start/stop button controls over the process;
- Once the start button clicked - the previous session is being deleted;
- Data is collected in any phone state, even if the app is closed, until user will stop it manually;
- Control over network state is also supporting;

# Approaches and technologies used:
- Kotlin 100%
- Clean architecture
- MVVM pattern
- Dependency Injection
- Kotlin coroutines + Flow
- Database

# Other libraries used: 
- Kotlin Coroutines
- Kotlin Flow
- Kotlin Serialization
- Retrofit
- OkHttp
- Glide
- GMS

# Jetpack Components used:
- Fragment
- ViewModel
- Room
- Hilt
- Lifecycle Service
- View Binding
- Navigation
- Location
- Permission

# What I've learnt:
- Some Flow usage cases
- Some new Flow operators
- Foreground service behaviour
- Hilt pluses and minuses over Dagger
- Some gradle.kts features
- If you use database create data module as app library!
- How to request permissions over classic version
