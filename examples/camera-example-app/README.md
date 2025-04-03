# Camera Example Application

This is a simple example application that utilizes the Tauri camera plugin API to initialize the camera and take a picture. The application is built using Svelte and Vite, providing a modern frontend experience.

## Project Structure

```
camera-example-app
├── src
│   ├── App.svelte          # Main application component
│   ├── main.js             # Entry point for the application
│   ├── style.css           # Styles for the application
│   ├── vite-env.d.ts       # TypeScript definitions for Vite and Svelte
│   └── lib
│       └── Camera.svelte   # Component for interacting with the camera
├── public
│   ├── svelte.svg          # Svelte logo
│   ├── tauri.svg           # Tauri logo
│   └── vite.svg            # Vite logo
├── src-tauri
│   ├── .gitignore          # Tauri backend ignore file
│   ├── build.rs            # Build script for Tauri
│   ├── Cargo.toml          # Rust project configuration
│   ├── tauri.conf.json     # Tauri application configuration
│   ├── capabilities
│   │   └── default.json     # Default permissions for the application
│   ├── icons               # Directory for application icons
│   └── src
│       ├── lib.rs          # Rust code for Tauri backend
│       └── main.rs         # Entry point for Tauri application
├── .gitignore              # Project ignore file
├── index.html              # Main HTML file
├── jsconfig.json           # JavaScript project configuration
├── package.json            # npm configuration file
└── vite.config.js          # Vite configuration file
```

## Getting Started

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd camera-example-app
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Run the application**:
   ```bash
   npm run dev
   ```

4. **Build the application**:
   ```bash
   npm run build
   ```

## Features

- Take pictures using the device camera.
- Simple and intuitive user interface.
- Built with Svelte and Vite for a fast development experience.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.