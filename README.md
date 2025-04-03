# Tauri Camera Plugin

This project is a Tauri plugin that allows Tauri applications to access the Android camera for taking pictures and recording videos. It exposes APIs in both Rust and TypeScript, enabling seamless integration with Tauri apps.

## Features

- Access the device camera to take pictures and record videos.
- Expose Rust APIs that act as a proxy to the Kotlin camera implementation.
- Provide TypeScript APIs for easy interaction within Tauri applications.

## Project Structure

- **android/src**: Contains the Android-specific implementation of the camera plugin.
  - **androidTest/java**: Instrumented tests for the camera plugin.
  - **main/java**: Contains the main classes for the camera plugin.
    - `CameraPlugin.kt`: Entry point for the Tauri plugin on Android.
    - `CameraHandler.kt`: Manages camera operations and file references.
  - **test/java**: Unit tests for the camera plugin.

- **guest-js/index.ts**: TypeScript APIs for interacting with the camera plugin.

- **src**: Contains the Rust implementation of the Tauri plugin.
  - `commands.rs`: Defines commands exposed to the consumer app.
  - `desktop.rs`: Desktop-specific implementations.
  - `error.rs`: Error handling for the plugin.
  - `lib.rs`: Main entry point for the Rust code.
  - `mobile.rs`: Mobile-specific implementations.
  - `models.rs`: Data models used in the plugin.

- **package.json**: Configuration file for npm, listing dependencies and build scripts.

- **Cargo.toml**: Configuration file for Rust's package manager, Cargo.

- **build.rs**: Build script that configures the Tauri plugin.

## Installation

To install the plugin, add it to your Tauri project as a dependency in your `Cargo.toml` and `package.json` files.

## Usage

### TypeScript API

Import the camera plugin in your TypeScript code and use the provided functions to interact with the camera.

### Rust API

Use the exposed commands in your Rust code to manage camera operations.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.