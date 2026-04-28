# Bom Demais

An Android inventory management app for small shop owners, built with Kotlin and Firebase.

## Features

- **Authentication** — Secure login with Firebase Email/Password Auth
- **Inventory** — Create custom product categories, add products, and adjust stock quantities with +/− controls
- **Out-of-Stock Tracking** — Products that reach zero are automatically queued for transfer to a dedicated "Produtos em Falta" section when you leave the category
- **Notes** — Create and delete Firebase-backed notes for quick memos

## Tech Stack

- Kotlin
- Firebase Auth + Realtime Database
- Material Design 3
- RecyclerView
- Android SDK 24+

## Architecture

```
app/src/main/java/com/estoque/bomdemais/
├── categorias/     # Category list screen and adapter
├── data/           # Data models (Product, Note) and FirebaseHelper
├── notas/          # Notes screen and adapter
├── produtos/       # Product list screen and adapter
├── LoginActivity   # Firebase Auth login
└── MainActivity    # Home screen with navigation
```

## Setup

1. Clone the repository
   ```bash
   git clone https://github.com/GabrielaPeroni/BomDemais.git
   ```
2. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
3. Enable **Email/Password** authentication under **Authentication → Sign-in methods**
4. Download your `google-services.json` and place it inside the `app/` directory
5. Open the project in Android Studio and run it on a device or emulator (API 24+)

## Screenshots

_Coming soon_

## Contributors

- [GabrielaPeroni](https://github.com/GabrielaPeroni)
- Matheus Maciel Pereira Falcão
