// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\src\error.rs
pub use thiserror::Error;

#[derive(Error, Debug)]
pub enum CameraError {
    #[error(transparent)] Io(#[from] std::io::Error),

    #[cfg(mobile)] #[error(transparent)] PluginInvoke(
        #[from] tauri::plugin::mobile::PluginInvokeError,
    ),

    #[error("Camera not available")]
    CameraNotAvailable,

    #[error("Failed to access the camera")]
    AccessDenied,

    #[error("Failed to capture image")]
    CaptureImageError,

    #[error("Failed to record video")]
    RecordVideoError,

    #[error("Unknown error occurred: {0}")] Unknown(String),
}

pub type Result<T> = std::result::Result<T, CameraError>;
