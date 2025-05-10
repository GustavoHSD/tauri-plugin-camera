use tauri::{ plugin::{ Builder, TauriPlugin }, Manager, Runtime };

#[cfg(mobile)]
mod mobile;

mod error;
mod models;

pub use models::*;
pub use error::{ Error, Result };

#[cfg(target_os = "android")]
const PLUGIN_IDENTIFIER: &str = "app.tauri.camera";

#[cfg(mobile)]
use mobile::Camera;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the camera APIs.
pub trait CameraExt<R: Runtime> {
    fn camera(&self) -> &Camera<R>;
}

impl<R: Runtime, T: Manager<R>> crate::CameraExt<R> for T {
    fn camera(&self) -> &Camera<R> {
        self.state::<Camera<R>>().inner()
    }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::<R>
        ::new("camera")
        .setup(|app, api| {
            #[cfg(target_os = "android")]
            let handle = api.register_android_plugin(PLUGIN_IDENTIFIER, "CameraPlugin")?;

            app.manage(Camera(handle));
            Ok(())
        })
        .build()
}
